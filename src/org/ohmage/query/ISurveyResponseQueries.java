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
package org.ohmage.query;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.DataAccessException;

public interface ISurveyResponseQueries {
	/**
	 * Returns the survey response privacy states.
	 * 
	 * @return A list of all of the survey response privacy states.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<SurveyResponse.PrivacyState> retrieveSurveyResponsePrivacyStates()
			throws DataAccessException;

	/**
	 * Retrieves the information about survey responses that match the given
	 * criteria. The criteria is based on the parameters. All parameters are
	 * optional except the campaign, and a null parameter is the equivalent to
	 * an omitted parameter. If all parameters are null, except the campaign,
	 * then all of the information about all of the survey responses will be
	 * returned for that campaign.
	 * 
	 * @param campaign The campaign to which the survey responses must belong.
	 * 
	 * @param username The username of the user that is making this request.
	 * 				   This is used by the ACLs to limit who sees what.
	 * 
	 * @param usernames Limits the results to only those submitted by any one 
	 * 					of the users in the list.
	 * 
	 * @param startDate Limits the results to only those survey responses that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate Limits the results to only those survey responses that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState Limits the results to only those survey responses
	 * 					   with this privacy state.
	 * 
	 * @param surveyIds Limits the results to only those survey responses that 
	 * 					were derived from a survey in this collection.
	 * 
	 * @param promptIds Limits the results to only those survey responses that 
	 * 					were derived from a prompt in this collection.
	 * 
	 * @param promptType Limits the results to only those survey responses that
	 * 					 are of the given prompt type.
	 * 
	 * @param columns Aggregates the data based on the column keys. If this is
	 * 				  null, no aggregation is performed. If the list is empty,
	 * 				  an empty list is returned.
	 * 
	 * @param surveyResponsesToSkip The number of survey responses to skip once
	 * 								the result has been aggregated from the 
	 * 								server.
	 * 
	 * @param surveyResponsesToProcess The number of survey responses to 
	 * 								   analyze once the survey responses to 
	 * 								   skip have been skipped.
	 * 
	 * @return A list of SurveyResponse objects where each object 
	 * 		   represents a survey response that matched the given criteria.
	 *  
	 * @throws DataAccessException Thrown if there is an error. 
	 */
	int retrieveSurveyResponses(
			final Campaign campaign,
			final String username,
			final Collection<String> usernames,
			final Date startDate,
			final Date endDate,
			final SurveyResponse.PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType,
			final Collection<ColumnKey> columns, 
			final List<SortParameter> sortOrder,
			final long surveyResponsesToSkip,
			final long surveyResponsesToProcess,
			List<SurveyResponse> result) 
			throws DataAccessException;
	
	/**
	 * Returns the number of survey responses that match the given criteria.
	 * 
	 * @param campaign The campaign to which the survey responses must belong.
	 * 
	 * @param username The username of the user that is making this request.
	 * 				   This is used by the ACLs to limit who sees what.
	 * 
	 * @param usernames Limits the results to only those submitted by any one 
	 * 					of the users in the list.
	 * 
	 * @param startDate Limits the results to only those survey responses that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate Limits the results to only those survey responses that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState Limits the results to only those survey responses
	 * 					   with this privacy state.
	 * 
	 * @param surveyIds Limits the results to only those survey responses that 
	 * 					were derived from a survey in this collection.
	 * 
	 * @param promptIds Limits the results to only those survey responses that 
	 * 					were derived from a prompt in this collection.
	 * 
	 * @param promptType Limits the results to only those survey responses that
	 * 					 are of the given prompt type.
	 * 
	 * @return A long representing the number of survey responses that match
	 * 		   the criteria.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public long retrieveSurveyResponseCount(
			final Campaign campaign,
			final String username,
			final Collection<String> usernames, 
			final Date startDate,
			final Date endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType)
			throws DataAccessException;

	/**
	 * Updates the privacy state on a survey response.
	 * 
	 * @param surveyResponseIds The survey responses' unique identifier.
	 * @param privacyState The survey's new privacy state
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void updateSurveyResponsesPrivacyState(Set<UUID> surveyResponseIds,
			SurveyResponse.PrivacyState newPrivacyState)
			throws DataAccessException;

	/**
	 * Deletes a survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void deleteSurveyResponse(UUID surveyResponseId) throws DataAccessException;

}
