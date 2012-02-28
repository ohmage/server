/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ISurveyResponseImageQueries;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class is responsible for creating, reading, updating, and deleting 
 * survey-image associations.
 * 
 * @author John Jenkins
 */
public class SurveyResponseImageQueries extends Query implements ISurveyResponseImageQueries {
	// Returns all of the image IDs for all of the photo prompt responses for a
	// survey.
	private static final String SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE = 
		"SELECT response " +
		"FROM prompt_response pr " +
		"WHERE pr.survey_response_id = ? " +
		"AND prompt_type = 'photo'";
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private SurveyResponseImageQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseImageQueries#getImageIdsFromSurveyResponse(java.lang.Long)
	 */
	public List<UUID> getImageIdsFromSurveyResponse(UUID surveyResponseId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE,
					new Object[] { surveyResponseId.toString() },
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							String response = rs.getString("response");
							try {
								return UUID.fromString(response);
							}
							catch(IllegalArgumentException notUuid) {
								try {
									NoResponse.valueOf(response.toUpperCase());
									return null;
								}
								catch(IllegalArgumentException notNoResponse) {
									throw new SQLException(
											"The response value is not a valid UUID.", 
											notNoResponse);
								}
							}
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE + "' with parameter: " + surveyResponseId, e);
		}
	}
}
