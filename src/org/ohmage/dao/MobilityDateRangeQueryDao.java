/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.MobilityQueryResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MobilityQueryAwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author selsky
 */
public class MobilityDateRangeQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MobilityQueryDao.class);
	
	private String _modeOnlySql = "SELECT msg_timestamp, phone_timezone, location_status, location, mode "
		                          + "FROM mobility_mode_only m, user u " 
		                          + "WHERE u.username = ? "
		                          + "AND u.id = m.user_id "
		                          + "AND date(msg_timestamp) BETWEEN ? and ? " 
		                          + "ORDER BY msg_timestamp";
	
	private String _extendedSql = "SELECT msg_timestamp, phone_timezone, location_status, location, mode "
                                  + "FROM mobility_extended m, user u " 
                                  + "WHERE u.username = ? "
                                  + "AND u.id = m.user_id "
                                  + "AND date(msg_timestamp) BETWEEN ? and ? "
    	                          + "ORDER BY msg_timestamp";
	
	public MobilityDateRangeQueryDao(DataSource dataSource) {
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
			params.add(req.getEndDate());
			
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
