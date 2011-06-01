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
import org.ohmage.domain.DataPointFunctionQueryResult;
import org.ohmage.request.AwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author selsky
 */
public class CompletedSurveysDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CompletedSurveysDao.class);
	
	private String _sql = "SELECT sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id "
			              + "FROM survey_response sr, user u, campaign c "
                          + "WHERE sr.user_id = u.id "
                          + "AND u.login_id = ? "
                          + "AND c.urn = ? "
                          + "AND c.id = sr.campaign_id "
                          + "AND date(msg_timestamp) BETWEEN ? and ?";
	 
	public CompletedSurveysDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<Object> params = new ArrayList<Object>();
		params.add(awRequest.getUserNameRequestParam());
		params.add(awRequest.getCampaignUrn());
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
