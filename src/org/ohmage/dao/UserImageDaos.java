package org.ohmage.dao;

import javax.sql.DataSource;

/**
 * This class is responsible for the functionality to create, read, update, and
 * delete user-image assocations.
 * 
 * @author John Jenkins
 */
public final class UserImageDaos extends Dao {
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
	 * Retrieves the username of the user that created this image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Returns the creator of the image or null if the image doesn't
	 * 		   exist.
	 */
	public static String getImageOwner(String imageId) {
		try {
			return instance.jdbcTemplate.queryForObject(SQL_GET_IMAGE_OWNER, new Object[] { imageId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("More than one image has the same ID.");
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_OWNER + "' with parameter: " + imageId, e);
		}
	}
}