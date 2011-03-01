package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.DataPointFunctionQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class CompletedSurveysDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CompletedSurveysDao.class);
	
	private String _sql = "SELECT sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id "
			              + "FROM survey_response sr, user u, campaign_configuration cc, campaign c "
                          + "WHERE sr.user_id = u.id "
                          + "AND u.login_id = ? "
                          + "AND c.name = ? "
                          + "AND c.id = cc.campaign_id "
                          + "AND cc.version = ? "
                          + "AND cc.id = sr.campaign_configuration_id "
                          + "AND date(msg_timestamp) BETWEEN ? and ?";
	 
	public CompletedSurveysDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<Object> params = new ArrayList<Object>();
		params.add(awRequest.getUserNameRequestParam());
		params.add(awRequest.getCampaignName());
		params.add(awRequest.getCampaignVersion());
		params.add(awRequest.getStartDate());
		params.add(awRequest.getEndDate());
		
		try {
			
			awRequest.setResultList(
				getJdbcTemplate().query(_sql, params.toArray(), 
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							DataPointFunctionQueryResult result = new DataPointFunctionQueryResult();
							result.setTimestamp(rs.getString(1));
							result.setTimezone(rs.getString(2));
							result.setLocationStatus(rs.getString(3));
							result.setLocation(rs.getString(4));
							result.setValue(rs.getString(5));
							return result;
						}
					}
				)
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("an exception occurred running the sql '" + _sql + "' with the following parameters " + params.toString(), dae);
			throw new DataAccessException(dae);
		}
	}
}
