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
 * Checks to make sure that the survey key exists.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsDao.class);
	
	private static final String SQL = "SELECT EXISTS (SELECT * " +
									  "FROM survey_response " +
									  "WHERE id = ?)";
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public SurveyKeyExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the survey key in question exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String surveyKey = (String) awRequest.getToValidate().get(InputKeys.SURVEY_KEY);
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { surveyKey }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + surveyKey, dae);
			throw new DataAccessException(dae);
		}
	}
}
