package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserStatsQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;

/**
 * @author selsky
 */
public class MostRecentSurveyActivityDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MostRecentSurveyActivityDao.class);
	
	private String _sql = "SELECT upload_timestamp"
		                 + " FROM survey_response sr"
                         + " WHERE sr.upload_timestamp =" 
						 + " (SELECT MAX(upload_timestamp)"
						 +	" FROM survey_response sr, campaign_configuration cc, campaign c, user u"
						 +	" WHERE u.id = sr.user_id "
						 +	 " AND sr.campaign_configuration_id = cc.id "
						 +	 " AND cc.campaign_id = c.id "
						 +   " AND c.name = ?"
						 +   " AND u.login_id = ?)";
	
	public MostRecentSurveyActivityDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the most recent survey activity time for awRequest.getUserNameRequestParam().
	 */
	@Override
	public void execute(AwRequest awRequest) {
		UserStatsQueryAwRequest req = (UserStatsQueryAwRequest) awRequest;
		UserStatsQueryResult userStatsQueryResult = null;
		
		final String userId = req.getUserNameRequestParam();
		final String campaignName = req.getCampaignName();
		
		if(null == req.getUserStatsQueryResult()) {
			userStatsQueryResult = new UserStatsQueryResult();
			req.setUserStatsQueryResult(userStatsQueryResult);
		} else {
			userStatsQueryResult = req.getUserStatsQueryResult();
		}
		
		try {
			
			List<?> results = getJdbcTemplate().query(_sql, new Object[] {campaignName, userId}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getTimestamp(1).getTime();
				}
			});
			
			if(0 != results.size()) { 
				userStatsQueryResult.setMostRecentSurveyUploadTime((Long) results.get(0));
			}
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + campaignName + ", " +userId);
			throw new DataAccessException(dae.getMessage());
		}
	}
}
