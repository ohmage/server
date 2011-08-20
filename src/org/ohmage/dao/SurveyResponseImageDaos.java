package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class is responsible for creating, reading, updating, and deleting 
 * survey-image associations.
 * 
 * @author John Jenkins
 */
public class SurveyResponseImageDaos extends Dao {
	// Returns all of the image IDs for all of the photo prompt responses for a
	// survey.
	private static final String SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE = 
		"SELECT response " +
		"FROM prompt_response pr " +
		"WHERE pr.survey_response_id = ? " +
		"AND prompt_type = 'photo'";
	
	private static SurveyResponseImageDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private SurveyResponseImageDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves all of the image IDs from a single survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @return A, possibly empty, list of image IDs from a survey response.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<String> getImageIdsFromSurveyResponse(Long surveyResponseId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE,
					new Object[] { surveyResponseId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_IDS_FROM_SURVEY_RESPONSE + "' with parameter: " + surveyResponseId, e);
		}
	}
}
