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

import org.apache.log4j.Logger;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IPromptResponseQueries;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Queries against prompt responses.
 * 
 * @author Joshua Selsky
 */
public class PromptResponseQueries extends Query implements IPromptResponseQueries {
	private static final Logger LOGGER = Logger.getLogger(PromptResponseQueries.class); 
	
	private static final String SQL_GET_PROMPT_RESPONSE_ID =
	    "SELECT pr.id " +
	    "FROM survey_response sr, prompt_response pr " +
	    "WHERE sr.uuid = ? " +
	    "AND sr.id = pr.survey_response_id " +
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
		catch(IncorrectResultSizeDataAccessException e) {
			int numberReturned = e.getActualSize();
			
			if(numberReturned == 0) {
				if(repeatableSetId != null && repeatableSetIteration != null) { 
					LOGGER.warn("Attempt to annotate a prompt response that doesn't exist with survey_id=" + surveyResponseId.toString() + ", prompt_id=" + promptId);
				}
				else {
					LOGGER.warn("Attempt to annotate a prompt response that doesn't exist with survey_id=" + surveyResponseId.toString() + ", prompt_id=" + promptId + 
						", repeatable_set_id=" + repeatableSetId + ", repeatable_set_iteration=" + repeatableSetIteration);
				}
				
				// Just throw the general exception in case someone attempted
				// to annotate a prompt response that was just deleted or
				// someone is hacking around
				
				throw new DataAccessException("Error executing SQL '" +sql.toString() + "'.", e);
			} 
			else { // more than one row returned; logical error
				
				if(repeatableSetId != null && repeatableSetIteration != null) { 
					LOGGER.warn("Attempt to annotate a multiple prompt responses with survey_id=" + surveyResponseId.toString() + ", prompt_id=" + promptId);
				}
				else {
					LOGGER.warn("Attempt to annotate a multiple prompt responses with survey_id=" + surveyResponseId.toString() + ", prompt_id=" + promptId + 
						", repeatable_set_id=" + repeatableSetId + ", repeatable_set_iteration=" + repeatableSetIteration);
				}
				
				throw new DataAccessException("Error executing SQL '" +sql.toString() + "'.", e);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" +sql.toString() + "'.", e);
		}
	}
}
