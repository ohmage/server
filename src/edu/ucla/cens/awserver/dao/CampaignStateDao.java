package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyUploadAwRequest;

/**
 * Finds the running_state for a campaign.
 * 
 * @author joshua selsky
 */
public class CampaignStateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignStateDao.class);
	
	private static final String SQL = "SELECT running_state " +
									  "FROM campaign " +
									  "WHERE urn=?";
	
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public CampaignStateDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// hack for now until toProcess is utilized
		SurveyUploadAwRequest req = null;
		try {
			req = (SurveyUploadAwRequest) awRequest;
		} catch (ClassCastException e) {
			_logger.error("Checking campaign running state on a non-SurveyUploadAwRequest object.");
			throw new DataAccessException("Invalid request.");
		}
		
		try {
			req.setCampaignRunningState(
				(String) getJdbcTemplate().queryForObject(SQL, new Object[] { req.getCampaignUrn() }, new SingleColumnRowMapper()));
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + req.getCampaignUrn());
			throw new DataAccessException(dae);
		}
	}

}
