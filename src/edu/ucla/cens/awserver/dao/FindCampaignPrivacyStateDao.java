package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author Joshua Selsky
 */
public class FindCampaignPrivacyStateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignPrivacyStateDao.class);
	
	private String _sql = "SELECT privacy_state"
		  	             + " FROM campaign c"
		  	             + " WHERE urn = ?";
	
	public FindCampaignPrivacyStateDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the running_state for the campaign URN in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] {awRequest.getCampaignUrn()},
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ awRequest.getCampaignUrn(), dae);
			throw new DataAccessException(dae);
		}
	}
}
