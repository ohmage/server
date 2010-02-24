package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.domain.PromptType;

/**
 * DAO for returning prompt type validation restrictions for an uploaded response group. The SQL that runs here assumes that the 
 * version_id and group_id have already been validated against the campaign. Note that the prompt_id passed as part of the upload
 * message is the prompt_id that is shared with the phone configuration, not that actual prompt id in the db. 
 * 
 * @author selsky
 */
public class PromptResponseGroupDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PromptResponseGroupDao.class);
	
	private final String _countSql = "select count(*) from prompt, prompt_type " +
									 " where prompt.campaign_prompt_group_id = ?" +
									 " and prompt.campaign_prompt_version_id = ?" +
									 " and prompt.prompt_type_id = prompt_type.id" +
									 " and prompt_type.type != 'null'";
	
	// This query has a custom substitution (the {$..} strings) because using variables in both the WHERE and IN clauses
	// causes the PreparedStatement implementation in the MySQL JDBC connector to throw exceptions (BadGrammarException) 
	// after the bind variables are set.
	private String _selectSql = "select prompt.prompt_config_id, prompt_type.type, prompt_type.restriction " +
			                    " from prompt, prompt_type" +
			                    " where prompt.campaign_prompt_group_id = {$campaignPromptGroupId}" +
	                            " and prompt.campaign_prompt_version_id = {$campaignPromptVersionId}" +
	                            " and prompt.prompt_type_id = prompt_type.id" + 
	                            " and prompt_type.type != 'null'" +
	                            " and prompt.prompt_config_id in"; // the IN clause is dynamic depending on the prompts 
                                                                   // in the response
	private String _orderBy = "order by prompt_config_id";
	
	/**
	 * Creates an instance of this class that will use the supplied DataSource for data retrieval.
	 */
	public PromptResponseGroupDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Retrieves the prompt type and restrictions on the type based on the promptIdArray attribute found in the AwRequest.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("looking up prompt restrictions for prompts " + 
			Arrays.toString(awRequest.getPromptIdArray()) + " for campaign " + 
			awRequest.getSubdomain() + ", campaign prompt group " + awRequest.getCampaignPromptGroupId() +
			", and campaign prompt version " + awRequest.getCampaignPromptVersionId()
		);
			
		int promptGroupId = awRequest.getCampaignPromptGroupId();
		int promptVersionId = awRequest.getCampaignPromptVersionId();
		
		// _logger.info("pgid=" + promptGroupId + " pvid=" + promptVersionId);
		
		// dynamically generate the SQL in clause based on the idArray
		final int[] idArray = awRequest.getPromptIdArray();
		int size = idArray.length;
		int numberOfPromptsInGroup = 0;
		
		// First, check whether the number of entries in the array represents the correct number of prompts for the group
		try {
		
			numberOfPromptsInGroup = getJdbcTemplate().queryForInt(_countSql, new Object[]{promptGroupId, promptVersionId});
		
		}
		catch (IncorrectResultSizeDataAccessException irse) {
			
			_logger.error("caught IncorrectResultSizeDataAccessException (one row expected, but " + irse.getActualSize() + 
				" returned) when running SQL '" + _countSql + "' with the following parameters: " + promptGroupId + ", " + 
				promptVersionId); 
			
			throw new DataAccessException(irse);
			
		}
		catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" + _countSql + "' with the following paramters: " + 
				promptGroupId + ", " + promptVersionId);
			
			throw new DataAccessException(dae);
			
		}
		
		if(size != numberOfPromptsInGroup) {
			_logger.info("incorrect number of prompts for group. prompts received = " + size + ". prompts expected = " 
				+ numberOfPromptsInGroup);
			awRequest.setFailedRequest(true);
			return;
		}
		
		// If the number of prompt responses is correct for the group, grab the prompt restrictions
		StringBuilder builder = new StringBuilder(_selectSql + " (");
		for(int i = 0; i < size; i++) {
			builder.append("?");
			if(i < size - 1) {
				builder.append(",");
			}
		}
		builder.append(") ");
		builder.append(_orderBy);
		
		final String sql = 
			builder.toString()
			  .replace("{$campaignPromptGroupId}", String.valueOf(promptGroupId))
              .replace("{$campaignPromptVersionId}", String.valueOf(promptVersionId));
		
		try {
			@SuppressWarnings("unchecked") // the anonymous RowMapper below explicitly creates PromptType objects so this is safe
			List<PromptType> list = getJdbcTemplate().query( 
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(sql);
						for(int j = 0; j < idArray.length; j++) {
							ps.setInt(j + 1, idArray[j]);
						}
						return ps;
					}
				},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						PromptType pt = new PromptType();
						pt.setPromptConfigId(rs.getInt(1));
						pt.setType(rs.getString(2));
						pt.setRestriction(rs.getString(3));
						return pt;
					}
				}
			);
		
			awRequest.setPromptTypeRestrictions(list);
			
		}  catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL + '" + sql + "' with the following parameters: " +
				Arrays.toString(idArray));
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
