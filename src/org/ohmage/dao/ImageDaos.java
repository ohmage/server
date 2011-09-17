package org.ohmage.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class is responsible for all operations directly pertaining to images.
 * It may read information from other entities as required but the parameters
 * to these functions, the return values from these functions, and the changes
 * made by these functions should only pertain to images.
 * 
 * @author John Jenkins
 */
public final class ImageDaos extends Dao {
	private static final Logger LOGGER = Logger.getLogger(ImageDaos.class);
	
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
	
	// Deletes an image form the url_based_resource table.
	private static final String SQL_DELETE_IMAGE =
		"DELETE FROM url_based_resource " +
		"WHERE uuid = ?";
	
	public static final String IMAGE_STORE_FORMAT = "png";
	public static final String IMAGE_SCALED_EXTENSION = "-s";
	
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
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static Boolean getImageExists(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_EXISTS_IMAGE, new Object[] { imageId }, Boolean.class);
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
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static String getImageUrl(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_IMAGE_URL, new Object[] { imageId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple images have the same unique identifier.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_URL + "' with parameter: " + imageId, e);
		}
	}
	
	/**
	 * Deletes an image reference from the database and, if successful, deletes
	 * the images off of the file system.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static void deleteImage(String imageId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting an image.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			String imageUrl = getImageUrl(imageId);
			
			try {
				instance.getJdbcTemplate().update(
						SQL_DELETE_IMAGE,
						new Object[] { imageId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing SQL '" + SQL_DELETE_IMAGE + 
						"' with parameter: " +
							imageId, 
						e);
			}
			
			try {
				// Delete the original image.
				if((new File((new URL(imageUrl)).getFile())).delete()) {
					LOGGER.warn("The image no longer existed.");
				}
				
				// Delete the scaled image.
				if((new File((new URL(imageUrl + IMAGE_SCALED_EXTENSION)).getFile())).delete()) {
					LOGGER.warn("The scaled image no longer existed.");
				}
			}
			catch(MalformedURLException e) {
				LOGGER.warn("The URL was malformed, but we are deleting the image anyway.", e);
			}
			catch(SecurityException e) {
				LOGGER.warn("The system would not allow us to delete the image.", e);
			}

			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
}