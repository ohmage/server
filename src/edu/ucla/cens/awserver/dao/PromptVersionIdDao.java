package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Utility DAO for checking if a version_id exists for a campaign. The version_id is used to group all of the groups of prompts for
 * a particular campaign. The idea is that over time a campaign may need to change it's prompt structure. A version_id allows for 
 * data historicity. Note that the version_id sent by the phone for data uploads is not the primary key for the campaign_prompt_version
 * table. The version_id sent by the phone is shared with the phone's config file.
 * 
 * @author selsky
 */
public class PromptVersionIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PromptVersionIdDao.class);
	
	private final String _selectSql = "select campaign_prompt_version.id from campaign_prompt_version, campaign" +
			                          " where campaign_prompt_version.version_id = ?" +
			                          " and campaign.subdomain = ? " +
			                          " and campaign.id = campaign_prompt_version.campaign_id;";
	
	/**
	 * Creates an instance of this class that will use the supplied DataSource for data retrieval.
	 */
	public PromptVersionIdDao(DataSource dataSource) {
		super(dataSource);
	}
	
	public void execute(AwRequest awRequest) {
		_logger.info("looking up prompt version_id for phone version id " + awRequest.getVersionId() + " in campaign " +
				awRequest.getSubdomain());
				
		try {
			
			int campaignPromptVersionId = getJdbcTemplate().queryForInt(
				_selectSql, new Object[]{awRequest.getVersionId(), awRequest.getSubdomain()}
		    );
			// Push the id into the request because it will be used by future queries
			awRequest.setCampaignPromptVersionId(campaignPromptVersionId);
			
		} catch (IncorrectResultSizeDataAccessException irse) { // this exception is thrown if the queryForInt call returns
			                                                    // anything else but one row
			
			_logger.error("caught IncorrectResultSizeDataAccessException (one row was expected to be returned, but the actual " +
					"size was " + irse.getActualSize() + ") when running SQL '" +  _selectSql + "' with the following parameters: " +
					awRequest.getVersionId() + ", " +  awRequest.getSubdomain());
			
			throw new DataAccessException(irse);
			
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" +  _selectSql + "' with the following parameters: " +
					awRequest.getVersionId() + ", " +  awRequest.getSubdomain());
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
