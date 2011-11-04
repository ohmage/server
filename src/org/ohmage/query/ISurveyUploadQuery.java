package org.ohmage.query;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;

public interface ISurveyUploadQuery {

	/**
	 * Inserts surveys into survey_response, prompt_response,
	 * and url_based_resource (if the payload contains images).
	 * Any images are also persisted to the file system. The entire persistence
	 * process is wrapped in one giant transaction.
	 * 
	 * @param user  The owner of the survey upload.
	 * @param client  The software client that performed the upload.
	 * @param campaignUrn  The campaign for the survey upload.
	 * @param surveyUploadList  The surveys to persist.
	 * @param bufferedImageMap  The images to persist.
	 * @return Returns a List of Integers representing the ids of duplicate
	 * surveys.
	 * @throws DataAccessException  If any IO error occurs.
	 */
	List<Integer> insertSurveys(final String username, final String client,
			final String campaignUrn,
			final List<SurveyResponse> surveyUploadList,
			final Map<String, BufferedImage> bufferedImageMap)
			throws DataAccessException;

}