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

import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface ICampaignSurveyResponseQueries {

	/**
	 * Retrieves the total number of survey responses for a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The total number of survey responses for a campaign. If the
	 * 		   campaign doesn't exist, 0 is returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	long getNumberOfSurveyResponsesForCampaign(String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves the total number of prompt responses for a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The total number of prompt responses for a campaign. If the 
	 * 		   campaign doesn't exist, 0 is returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	long getNumberOfPromptResponsesForCampaign(String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves the campaign ID for the campaign to which a survey belongs 
	 * given a survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @return The campaign's unique identifier or null if the survey response
	 *		   doesn't exist.
	 *
	 * @throws DataAccessException Thrown if there is an error.
	 */
	String getCampaignIdFromSurveyId(UUID surveyResponseId)
			throws DataAccessException;

}
