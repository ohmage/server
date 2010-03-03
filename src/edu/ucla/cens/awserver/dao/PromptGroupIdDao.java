package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Utility DAO for checking if a group_id exists for a campaign. The group_id is used to group prompts so campaigns may have 
 * multiple packets of prompts uniquely represented by a group_id. Note that the group_id sent by the phone is not the primary
 * key on the campaign_prompt_group table.
 * 
 * @author selsky
 */
public class PromptGroupIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PromptGroupIdDao.class);
	
	private final String _selectSql = "select campaign_prompt_group.id from campaign_prompt_group, campaign" +
			                          " where campaign_prompt_group.group_id = ?" +
			                          " and campaign_prompt_group.campaign_prompt_version_id = ?" +
			                          " and campaign.subdomain = ? " +
			                          " and campaign.id = campaign_prompt_group.campaign_id;";
	
	/**
	 * Creates an instance of this class that will use the supplied DataSource for data retrieval.
	 */
	public PromptGroupIdDao(DataSource dataSource) {
		super(dataSource);
	}
	
	public void execute(AwRequest awRequest) {
		_logger.info("looking up campaign_prompt_group.id for phone config group id " + awRequest.getGroupId() + " in campaign " +
				awRequest.getSubdomain());
		
		try {
			
//			int campaignPromptGroupId = getJdbcTemplate().queryForInt(
//				_selectSql, new Object[]{awRequest.getAttribute("groupId"), 
//						                 awRequest.getAttribute("campaignPromptVersionId"), 
//						                 awRequest.getAttribute("subdomain")}
//		    );
			
			int campaignPromptGroupId = getJdbcTemplate().queryForInt(
					_selectSql, new Object[]{awRequest.getGroupId(), 
							                 awRequest.getCampaignPromptVersionId(), 
							                 awRequest.getSubdomain()}
			);

			// Push the id into the request because it will be used by future queries
			awRequest.setCampaignPromptGroupId(campaignPromptGroupId);
			
		} catch (IncorrectResultSizeDataAccessException irse) { // this exception is thrown if the queryForInt call returns
			                                                    // anything else but one row
			
			_logger.error("caught IncorrectResultSizeDataAccessException (one row was expected to be returned, but the actual " +
				"size was " + irse.getActualSize() + ") when running SQL '" +  _selectSql + "' with the following parameters: " +
				awRequest.getGroupId() + ", " +  awRequest.getCampaignPromptVersionId() + ", " + awRequest.getSubdomain());
			
			throw new DataAccessException(irse);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" +  _selectSql + "' with the following parameters: " +
					awRequest.getGroupId() + ", " +  awRequest.getCampaignPromptVersionId() + ", " + awRequest.getSubdomain());
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
