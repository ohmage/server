package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignDeletionAwRequest;

/**
 * Checks to make sure that the campaign exists.
 * 
 * @author John Jenkins
 */
public class CampaignExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignExistsDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM campaign " +
									  "WHERE urn=?";
	
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public CampaignExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the campaign in question exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String campaignUrn;
		try {
			campaignUrn = (String) awRequest.getToProcessValue(CampaignDeletionAwRequest.KEY_CAMPAIGN_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("No campaign urn exists in the toProcess map.");
			throw new DataAccessException(e);
		}
		
		try {
			int count = getJdbcTemplate().queryForInt(SQL, new Object[] { campaignUrn });
			
			if(count == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + campaignUrn, dae);
			throw new DataAccessException(dae);
		}
	}

}
