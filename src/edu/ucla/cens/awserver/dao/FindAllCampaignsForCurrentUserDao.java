package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;

/**
 * Near duplicate of FindAllCampaignsForUserDao except this class is to be used in a multi-user (new data point query API) scenario. 
 * 
 * @author selsky
 */
public class FindAllCampaignsForCurrentUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllCampaignsForCurrentUserDao.class);
	
	private String _sql = "SELECT c.urn " +
			              "FROM campaign c, user_role_campaign urc, user u " +
			              "WHERE urc.campaign_id = c.id AND urc.user_id = u.id AND u.login_id = ?";
	
	public FindAllCampaignsForCurrentUserDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// Hack continued.
		String user;
		if(awRequest instanceof NewDataPointQueryAwRequest) {
			user = ((NewDataPointQueryAwRequest) awRequest).getCurrentUser();
		}
		else if(awRequest instanceof UserStatsQueryAwRequest) {
			user = ((UserStatsQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else if(awRequest instanceof DataPointFunctionQueryAwRequest) {
			user = ((DataPointFunctionQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else if(awRequest instanceof MediaQueryAwRequest) {
			user = ((MediaQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else {
			throw new DataAccessException("Invalid AwRequest for this DAO.");
		}
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] { user },
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter"
				+ ((NewDataPointQueryAwRequest) awRequest).getCurrentUser(), dae);
			throw new DataAccessException(dae);
		}
	}
}
