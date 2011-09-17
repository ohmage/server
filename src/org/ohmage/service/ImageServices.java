package org.ohmage.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.ImageDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.validator.ImageValidators.ImageSize;

/**
 * This class is responsible for all operations pertaining only to images. The
 * functions in this class may read information from other entities, but their
 * parameters, return values, and changes to the system should pertain only to
 * images.
 * 
 * @author John Jenkins
 */
public final class ImageServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ImageServices() {}
	
	/**
	 * Checks if an image exists or not and compares that to whether or not it
	 * should exist.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @param shouldExist Whether or not the image should exist.
	 * 
	 * @throws ServiceException Thrown if the image does not exist and it 
	 * 							should or does exist and it should not, or if
	 * 							there is an error.
	 */
	public static void verifyImageExistance(Request request, String imageId, boolean shouldExist) throws ServiceException {
		try {
			Boolean imageExists = ImageDaos.getImageExists(imageId);
			
			if(imageExists && (! shouldExist)) {
				request.setFailed(ErrorCodes.IMAGE_INVALID_ID, "The image already exists.");
				throw new ServiceException("The image already exists.");
			}
			else if((! imageExists) && shouldExist) {
				request.setFailed(ErrorCodes.IMAGE_INVALID_ID, "The image does not exist.");
				throw new ServiceException("The image does not exist.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves an InputStream connected to the image.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @param size An ImageSize indicating if the smaller version of the image
	 * 			   is desired.
	 * 
	 * @return Returns an InputStream hooked up to this image.
	 * 
	 * @throws ServiceException Thrown if there is an error, the URL is 
	 * 							malformed, or there is an error connecting to
	 * 							them.
	 */
	public static InputStream getImage(Request request, String imageId, ImageSize size) throws ServiceException {
		try {
			String imageUrl = ImageDaos.getImageUrl(imageId);
			
			if(ImageSize.SMALL.equals(size)) {
				int imageUrlLength = imageUrl.length();
				// If it is saved in the old format with the extension, then 
				// we need to place the scaled extension before the file
				// extension.
				if((imageUrlLength >= 4) && (imageUrl.charAt(imageUrlLength - 4) == '.')) {
					imageUrl = imageUrl.substring(0, imageUrlLength - 4) + 
							ImageDaos.IMAGE_SCALED_EXTENSION + 
							imageUrl.substring(imageUrlLength - 4, imageUrlLength);
				}
				// If it is saved in the new format without the extension, then
				// we only need to attach the extension to the end of the URL.
				else {
					imageUrl += ImageDaos.IMAGE_SCALED_EXTENSION;
				}
			}
			
			return (new URL(imageUrl)).openConnection().getInputStream();
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
		catch(MalformedURLException e) {
			request.setFailed();
			throw new ServiceException("The stored URL invalid.", e);
		}
		catch(IOException e) {
			request.setFailed();
			throw new ServiceException("The URL could not be read.", e);
		}
	}
}