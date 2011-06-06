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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * Checks to make sure that the survey key exists and belongs to the currently logged-in user.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsForLoggedInUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsForLoggedInUserDao.class);
	
	private static final String SQL = "SELECT EXISTS (SELECT * " +
									  "FROM survey_response, user " +
									  "WHERE survey_response.id = ? AND user.username = ? AND survey_response.user_id = user.id)";
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public SurveyKeyExistsForLoggedInUserDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the survey key belongs to the logged-in user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String surveyKey = (String) awRequest.getToValidate().get(InputKeys.SURVEY_KEY);
		String userName = awRequest.getUser().getUserName();
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { surveyKey, userName }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with the parameters: " + surveyKey + ", " + userName, dae);
			throw new DataAccessException(dae);
		}
	}
}
