package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Utility DAO for checking if a group_id exists for a campaign. The group_id is used to group prompts so campaigns may have 
 * multiple packets of prompts uniquely represented by a group_id. Note that the group_id sent by the phone is not the primary
 * key on the campaign_prompt_group table. The group_id sent by the phone is shared with the phone's config file.
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
	
	public void execute(AwRequest request) {
		_logger.info("looking up prompt group_id for phone group id " + request.getAttribute("groupId") + " in campaign " +
				request.getAttribute("subdomain"));
		
		try {
			
			int campaignPromptGroupId = getJdbcTemplate().queryForInt(
				_selectSql, new Object[]{request.getAttribute("groupId"), 
						                 request.getAttribute("campaignPromptVersionId"), 
						                 request.getAttribute("subdomain")}
		    );

			// Push the id into the request because it will be used by future queries
			request.setAttribute("campaignPromptGroupId", campaignPromptGroupId);
			
		} catch (IncorrectResultSizeDataAccessException irse) { // this exception is thrown if the queryForInt call returns
			                                                    // anything else but one row
			
			request.setFailedRequest(true);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
