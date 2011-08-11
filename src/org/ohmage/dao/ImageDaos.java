package org.ohmage.dao;

import javax.sql.DataSource;

/**
 * This class is responsible for all operations directly pertaining to images.
 * It may read information from other entities as required but the parameters
 * to these functions, the return values from these functions, and the changes
 * made by these functions should only pertain to images.
 * 
 * @author John Jenkins
 */
public final class ImageDaos extends Dao {
	// Checks if an image exists.
	private static final String SQL_EXISTS_IMAGE = 
		"SELECT EXISTS(" +
			"SELECT ubr.uuid " +
			"FROM url_based_resource ubr, prompt_response pr " +
			"WHERE ubr.uuid = ? " +
			"AND pr.response = ubr.uuid " +
			"AND pr.prompt_type = 'photo'" +
		")";
	
	// Retrieves the URL for an image.
	private static final String SQL_GET_IMAGE_URL =
		"SELECT DISTINCT(ubr.url) " +
		"FROM url_based_resource ubr, prompt_response pr " +
		"WHERE ubr.uuid = ? " +
		"AND pr.response = ubr.uuid " +
		"AND pr.prompt_type = 'photo'";
	
	private static ImageDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private ImageDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}

	/**
	 * Retrieves whether or not an image with the given ID exists.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns true if the image exists; false, otherwise.
	 */
	public static Boolean getImageExists(String imageId) {
		try {
			return instance.jdbcTemplate.queryForObject(SQL_EXISTS_IMAGE, new Object[] { imageId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_IMAGE + "' with parameter: " + imageId, e);
		}
	}
	
	/**
	 * Retrieves the URL for the image if the image exists. If the image does
	 * not exist, null is returned. 
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @return Returns the URL for the image if it exists; otherwise, null is
	 * 		   returned.
	 */
	public static String getImageUrl(String imageId) {
		try {
			return instance.jdbcTemplate.queryForObject(SQL_GET_IMAGE_URL, new Object[] { imageId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple images have the same unique identifier.");
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_URL + "' with parameter: " + imageId, e);
		}
	}
}
