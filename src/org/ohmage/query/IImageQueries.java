package org.ohmage.query;

import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface IImageQueries {

	/**
	 * Retrieves whether or not an image with the given ID exists.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns true if the image exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean getImageExists(UUID imageId) throws DataAccessException;

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
	String getImageUrl(UUID imageId) throws DataAccessException;

	/**
	 * Deletes an image reference from the database and, if successful, deletes
	 * the images off of the file system.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void deleteImage(UUID imageId) throws DataAccessException;

}