package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Utility DAO for checking a version_id exists for a campaign. The version_id is used to group all of the groups of prompts for a
 * particular campaign. The idea is that over time a campaign may need to change it's prompt structure. A version_id allows for 
 * data historicity.
 * 
 * @author selsky
 */
public class PromptVersionIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PromptVersionIdDao.class);
	
	private final String _selectSql = "select campaign_prompt_version.id from campaign_prompt_version, campaign" +
			                          " where campaign_prompt_version.version_id = ?" +
			                          " and campaign.subdomain = ? " +
			                          " and campaign.id = campaign_prompt_version.id;";
	
	/**
	 * Creates an instance of this class that will use the supplied DataSource for data retrieval.
	 */
	public PromptVersionIdDao(DataSource dataSource) {
		super(dataSource);
	}
	
	public void execute(AwRequest request) {
		_logger.info("looking up prompt version_id");
		
		JdbcTemplate template = new JdbcTemplate(getDataSource());
		
		try {
			
			template.queryForInt(_selectSql, new Object[]{request.getAttribute("versionId"), request.getAttribute("subdomain")});
			
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
