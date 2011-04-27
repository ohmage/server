package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MobilityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MobilityQueryAwRequest;

/**
 * Queries the db for one day's worth of mobility data. 
 * 
 * @author selsky
 */
public class MobilityQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MobilityQueryDao.class);
	
	private String _modeOnlySql = "SELECT msg_timestamp, phone_timezone, location_status, location, mode "
		                          + "FROM mobility_mode_only m, user u " 
		                          + "WHERE u.login_id = ? "
		                          + "AND u.id = m.user_id "
		                          + "AND date(msg_timestamp) = ? "
		                          + "ORDER BY msg_timestamp";
	
	private String _extendedSql = "SELECT msg_timestamp, phone_timezone, location_status, location, mode "
                                  + "FROM mobility_extended m, user u " 
                                  + "WHERE u.login_id = ? "
                                  + "AND u.id = m.user_id "
                                  + "AND date(msg_timestamp) = ? "
                                  + "ORDER BY msg_timestamp";
	
	public MobilityQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		
		try {
			MobilityQueryAwRequest req = (MobilityQueryAwRequest) awRequest;
			
			List<Object> params = new ArrayList<Object>();
			params.add(req.getUserNameRequestParam());
			params.add(req.getStartDate());
			
			List<?> results = getJdbcTemplate().query(_modeOnlySql, params.toArray(), 
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						MobilityQueryResult result = new MobilityQueryResult();
						result.setTimestamp(rs.getString(1));
						result.setTimezone(rs.getString(2));
						result.setLocationStatus(rs.getString(3));
						result.setLocation(rs.getString(4));
						result.setValue(rs.getString(5));
						return result;
					}
			});
			
			int a = results.size();
			_logger.info("found " + a + " results from mobility_mode_only");
			
			results.addAll(
				 getJdbcTemplate().query(_extendedSql, params.toArray(), 
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							MobilityQueryResult result = new MobilityQueryResult();
							result.setTimestamp(rs.getString(1));
							result.setTimezone(rs.getString(2));
							result.setLocationStatus(rs.getString(3));
							result.setLocation(rs.getString(4));
							result.setValue(rs.getString(5));
							return result;
						}
				})
			);
			
			_logger.info("found " + (results.size() - a) + " results from mobility_extended");
			req.setResultList(results);
		
		} catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("database problem occurred when running mobility query", dae);
			throw new DataAccessException(dae);
		}
	}
}
