package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MobilityActivityQueryResult;
import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.domain.PromptActivityQueryResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for looking up the most recent prompt response and mobility uploads for a particular user.
 * 
 * @author selsky
 */
public class SingleUserMostRecentActivityQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MultiUserMostRecentActivityQueryDao.class);
	
	private String _promptResponseSql = "select max(prompt_response.time_stamp), prompt_response.phone_timezone" +
									    " from prompt_response, prompt, campaign_prompt_group" +
									    " where prompt_response.prompt_id = prompt.id" +
									    " and prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
									    " and campaign_id = ?" +
									    " and prompt_response.user_id = ?" +
									    " group by prompt_response.user_id" +
									    " order by prompt_response.user_id";
	
	private String _mobilityUploadSql = "select max(time_stamp), phone_timezone" +
							            " from mobility_mode_only_entry" +
							            " where user_id = ?" +
							            " group by user_id" +
							            " order by user_id";
	
	public SingleUserMostRecentActivityQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<MostRecentActivityQueryResult> results = new ArrayList<MostRecentActivityQueryResult>();
		User user = awRequest.getUser();
		results.add(executeSqlForSingleUser(Integer.parseInt(user.getCurrentCampaignId()), user.getId(), user.getUserName()));
		awRequest.setResultList(results);
	}
	
	protected MostRecentActivityQueryResult executeSqlForSingleUser(int campaignId, int userId, final String userName) {
		String currentSql = null;
		
		try {
			
			MostRecentActivityQueryResult result = new MostRecentActivityQueryResult();
			currentSql = _promptResponseSql;
			
			List<?> results = 
				getJdbcTemplate().query(_promptResponseSql, new Object[] {campaignId, userId}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						PromptActivityQueryResult result = new PromptActivityQueryResult(); 
						result.setUserName(userName);
						result.setPromptTimestamp(rs.getTimestamp(2));
						result.setPromptTimezone(rs.getString(3));
						return result;
						
					}
				}
			);
			
			if(results.size() != 0) {
				
				result.setPromptActivityQueryResult((PromptActivityQueryResult) results.get(0));
			}
			
			
			currentSql = _mobilityUploadSql;
			
			results = getJdbcTemplate().query(_mobilityUploadSql, new Object[] {userId}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MobilityActivityQueryResult result = new MobilityActivityQueryResult(); 
						result.setUserName(userName);
						result.setMobilityTimestamp(rs.getTimestamp(2));
						result.setMobilityTimezone(rs.getString(3));
						return result;
						
					}
				}
			);
			
			if(results.size() != 0) {
				
				result.setMobilityActivityQueryResult((MobilityActivityQueryResult) results.get(0));
			}
			
			if(null == result.getMobilityActivityQueryResult() && null == result.getPromptActivityQueryResult()) {
				result.setUserName(userName);
			}
			
			return result;
			
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + currentSql + "' Params: " + campaignId + ", " + userId);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
