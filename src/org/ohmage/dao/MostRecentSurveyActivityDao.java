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
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.UserStatsQueryResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.UserStatsQueryAwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author selsky
 */
public class MostRecentSurveyActivityDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MostRecentSurveyActivityDao.class);
	
	private String _sql = "SELECT upload_timestamp"
		                 + " FROM survey_response sr"
                         + " WHERE sr.upload_timestamp =" 
						 + " (SELECT MAX(upload_timestamp)"
						 +	" FROM survey_response sr, campaign c, user u"
						 +	" WHERE u.id = sr.user_id "
						 +	 " AND sr.campaign_id = c.id "
						 +   " AND c.urn = ?"
						 +   " AND u.username = ?)";
	
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
		final String campaignUrn = req.getCampaignUrn();
		
		if(null == req.getUserStatsQueryResult()) {
			userStatsQueryResult = new UserStatsQueryResult();
			req.setUserStatsQueryResult(userStatsQueryResult);
		} else {
			userStatsQueryResult = req.getUserStatsQueryResult();
		}
		
		try {
			
			List<?> results = getJdbcTemplate().query(_sql, new Object[] {campaignUrn, userId}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getTimestamp(1).getTime();
				}
			});
			
			if(0 != results.size()) { 
				userStatsQueryResult.setMostRecentSurveyUploadTime((Long) results.get(0));
			}
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + campaignUrn + ", " +userId);
			throw new DataAccessException(dae.getMessage());
		}
	}
}
