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

public interface IUserSurveyResponseQueries {

	/**
	 * Return the username of the user that created this survey response.
	 * 
	 * @param surveyResponseId The unique identifier for the survey response.
	 * 
	 * @return The username of the user that owns this survey response or null
	 * 		   if the survey response doesn't exist.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	String getSurveyResponseOwner(UUID surveyResponseId)
			throws DataAccessException;

	/**
	 * Retrieves the time stamp of the time that the last uploaded survey was 
	 * taken aligned to the time zone on the phone.
	 *  
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user to which the data belongs.
	 * 
	 * @return A Time stamp of the last update whose time zone is set to the 
	 * 		   same one as reported by the uploader.
	 */
	Long getLastUploadForUser(String requestersUsername,
			String usersUsername) throws DataAccessException;

	/**
	 * Retrieves the percentage of non-null location values from surveys over
	 * the past 'hours'.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user to which the data belongs.
	 * 
	 * @param hours Defines the timespan for which the information should be
	 * 				retrieved. The timespan is from now working backwards until
	 * 				'hours' hours ago.
	 * 
	 * @return Returns the percentage of non-null location values from surveys
	 * 		   over the last 'hours' or null if there were no surveys.
	 */
	Double getPercentageOfNonNullSurveyLocations(String requestersUsername,
			String usersUsername, int hours) throws DataAccessException;

}
