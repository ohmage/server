package org.ohmage.query.impl;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ISurveyResponseImageQueries;
import org.springframework.jdbc.core.SingleColumnRowMapper;

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
	public List<String> getImageIdsFromSurveyResponse(Long surveyResponseId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE,
					new Object[] { surveyResponseId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE + "' with parameter: " + surveyResponseId, e);
		}
	}
}
