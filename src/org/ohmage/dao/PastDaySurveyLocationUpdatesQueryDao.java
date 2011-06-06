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

import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.UserStatsQueryResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.UserStatsQueryAwRequest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;


/**
 * DAO for counting the number of successful survey location updates for the previous 24 hours (based on server-time) for a 
 * particular user or a group of users. 
 * 
 * A successful location update is defined by non-null latitude and longitude values in the survey_response table.
 * 
 * @author selsky
 */
public class PastDaySurveyLocationUpdatesQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PastDaySurveyLocationUpdatesQueryDao.class);

	private String _nonNullPointsSql = "SELECT COUNT(*)"
					                 + " FROM survey_response sr, campaign c, user u"
					                 + " WHERE u.username = ?"
						             + " AND sr.user_id = u.id"
						             + " AND sr.campaign_id = c.id"
						             + " AND c.urn = ?"
						             + " AND date(sr.upload_timestamp) BETWEEN date(now() - 1) and date(now())"
						             + " AND sr.location is not NULL";      
	
	private String _totalPointsSql = "SELECT COUNT(*)"
                                   + " FROM survey_response sr, campaign c, user u"
                                   + " WHERE u.username = ?"
                                   + " AND sr.user_id = u.id"
                                   + " AND sr.campaign_id = c.id"
                                   + " AND c.urn = ?"
                                   + " AND date(sr.upload_timestamp) BETWEEN date(now() - 1) and date(now())";
	
	public PastDaySurveyLocationUpdatesQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Calculates the percentage of successful location updates for the user id found in the provided AwRequest.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		UserStatsQueryAwRequest req = (UserStatsQueryAwRequest) awRequest; //TODO should do an instanceof check here
		
		Object[] paramArray = {req.getUserNameRequestParam(), req.getCampaignUrn()};
		double totalSuccess = 0d;
		double total = 0d;
		String currentSql =_totalPointsSql; 
		Double userPercentage = null;
		UserStatsQueryResult userStatsQueryResult = null;
		
		if(null == req.getUserStatsQueryResult()) {
			userStatsQueryResult = new UserStatsQueryResult();
			req.setUserStatsQueryResult(userStatsQueryResult);
		} else {
			userStatsQueryResult = req.getUserStatsQueryResult();
		}
		
		try {
			
			total = getJdbcTemplate().queryForInt(currentSql, paramArray);
			
			if(0 == total) {
				
				userPercentage = new Double(0d);
				
			} else {
				
				currentSql = _nonNullPointsSql;
				totalSuccess = getJdbcTemplate().queryForInt(currentSql, paramArray);
				userPercentage = new Double(totalSuccess / total);
			}
			
			userStatsQueryResult.setSurveyLocationUpdatesPercentage(userPercentage);
			
		} catch (IncorrectResultSizeDataAccessException irsdae) { // thrown if queryForInt returns more than one row which means 
                                                                  // there is a logical error in the SQL being run

			_logger.error("an incorrect number of rows was returned by '" + currentSql + "' with parameters " + Arrays.toString(paramArray));
			throw new DataAccessException(irsdae);

		} catch (org.springframework.dao.DataAccessException dae) { // thrown for general SQL errors

			_logger.error("an error was encountered when executing the following SQL: " + currentSql + " with parameters " 
				+ Arrays.toString(paramArray));
			throw new DataAccessException(dae);
		}
	}
}
