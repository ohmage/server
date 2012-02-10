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

import java.util.List;
import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface ISurveyResponseImageQueries {

	/**
	 * Retrieves all of the image IDs from a single survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @return A, possibly empty, list of image IDs from a survey response.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> getImageIdsFromSurveyResponse(UUID surveyResponseId)
			throws DataAccessException;

}
