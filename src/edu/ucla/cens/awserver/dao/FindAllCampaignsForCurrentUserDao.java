package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

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
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] { ((NewDataPointQueryAwRequest) awRequest).getCurrentUser() },
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
