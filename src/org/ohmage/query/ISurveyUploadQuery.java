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
import java.util.Map;
import java.util.UUID;

import org.ohmage.domain.Audio;
import org.ohmage.domain.DocumentP;
import org.ohmage.domain.Image;
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;

public interface ISurveyUploadQuery {

	/**
	 * Inserts surveys into survey_response, prompt_response, and
	 * url_based_resource (if the payload contains images). Any images are also
	 * persisted to the file system. The entire persistence process is wrapped
	 * in one giant transaction.
	 * 
	 * @param user
	 *        The owner of the survey upload.
	 * @param client
	 *        The software client that performed the upload.
	 * @param campaignUrn
	 *        The campaign for the survey upload.
	 * @param surveyUploadList
	 *        The surveys to persist.
	 * @param bufferedImageMap
	 *        The images to persist.
	 * @param videoContentsMap
	 *        The videos to persist.
	 * @param audioContentsMap
	 *        The audio files to persist.
	 * @param documentContentsMap 
	 * @return Returns a List of Integers representing the ids of duplicate
	 *         surveys.
	 * @throws DataAccessException
	 *         If any IO error occurs.
	 */
	List<Integer> insertSurveys(
			final String username, 
			final String client,
			final String campaignUrn,
			final List<SurveyResponse> surveyUploadList,
			final Map<UUID, Image> bufferedImageMap,
			final Map<String, Video> videoContentsMap,
			final Map<String, Audio> audioContentsMap, 
			final Map<String, DocumentP> documentContentsMap)
			throws DataAccessException;
}