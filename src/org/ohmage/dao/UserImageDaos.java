package org.ohmage.dao;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;

/**
 * This class is responsible for the functionality to create, read, update, and
 * delete user-image associations.
 * 
 * @author John Jenkins
 */
public final class UserImageDaos extends Dao {
	// Returns a boolean representing whether or not some photo prompt response
	// exists whose response value is the same as some photo's ID.
	private static final String SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE =
		"SELECT EXISTS(" +
			"SELECT pr.response " +
			"FROM user u, prompt_response pr, survey_response sr " +
			"WHERE u.username = ? " +
			"AND pr.response = ? " +
			"AND pr.prompt_type = 'photo' " +
			"AND pr.survey_response_id = sr.id " +
			"AND sr.user_id = u.id" +
		")";
	
	// Retrieves the name of the user that created the image.
	private static final String SQL_GET_IMAGE_OWNER =
		"SELECT u.username " +
		"FROM user u, url_based_resource ubr " +
		"WHERE ubr.uuid = ? " +
		"AND ubr.user_id = u.id";
	
	private static UserImageDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserImageDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Returns whether or not a photo prompt response exists for some user 
	 * whose response value is the photo's ID.
	 *  
	 * @param username The username of the user.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Whether or not a photo prompt response exists for some user
	 * 		   whose response value is the photo's ID.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static Boolean responseExistsForUserWithImage(String username, String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE, new Object[] { username, imageId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE + "' with parameters: " + 
					username + ", " + imageId, e);
		}
	}

	/**
	 * Retrieves the username of the user that created this image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Returns the creator of the image or null if the image doesn't
	 * 		   exist.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static String getImageOwner(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_IMAGE_OWNER, new Object[] { imageId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("More than one image has the same ID.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_OWNER + "' with parameter: " + imageId, e);
		}
	}
}