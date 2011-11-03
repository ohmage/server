package org.ohmage.query;

import java.util.List;

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
	List<String> getImageIdsFromSurveyResponse(Long surveyResponseId)
			throws DataAccessException;

}