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

import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IPromptResponseQueries;

/**
 * This class is responsible for creating, reading, updating, and deleting
 * prompt responses.
 * 
 * @author Joshua Selsky
 */
public class PromptResponseQueries extends Query implements IPromptResponseQueries {
	
	private static final String SQL_GET_PROMPT_RESPONSE_ID =
	    "SELECT pr.id " +
	    "FROM survey_response sr, prompt_response pr " +
	    "WHERE sr.uuid = ? " +
	    "AND pr.prompt_id = ?";

	private static final String SQL_GET_PROMPT_RESPONSE_ID_WHERE_REPEATABLE_SET =
		" AND pr.repeatable_set_id = ? " +
		"AND pr.repeatable_set_iteration = ?";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private PromptResponseQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public Integer retrievePromptResponseIdFor(UUID surveyResponseId,
		String promptId, String repeatableSetId, Integer repeatableSetIteration)
			throws DataAccessException {
		
		StringBuilder sql = new StringBuilder(SQL_GET_PROMPT_RESPONSE_ID);
		
		try {
			
			if(repeatableSetId != null && repeatableSetIteration != null) {
				
				sql.append(SQL_GET_PROMPT_RESPONSE_ID_WHERE_REPEATABLE_SET);
				
				return getJdbcTemplate().queryForInt(sql.toString(), new Object[] {surveyResponseId.toString(), promptId, repeatableSetId, repeatableSetIteration});
				
			} else {
				
				return getJdbcTemplate().queryForInt(sql.toString(), new Object[] {surveyResponseId.toString(), promptId});
			}
		}
		
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +sql.toString() + "'.", e);
		}
	}
}
