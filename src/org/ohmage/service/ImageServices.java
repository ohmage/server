package org.ohmage.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.ImageDaos;
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
	 * Verifies that an image exists. If it does not, the request is failed and
	 * an exception is thrown.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @throws ServiceException Thrown if the image does not exist.
	 */
	public static void verifyImageExists(Request request, String imageId) throws ServiceException {
		try {
			if(! ImageDaos.getImageExists(imageId)) {
				request.setFailed(ErrorCodes.IMAGE_INSUFFICIENT_PERMISSIONS, "The image does not exist.");
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
				// If they are requesting the smaller image, we need to parse 
				// the URL string and insert "-s" just before the ".jpg" at the
				// end.
				// FIXME: It would probably be best to add another extension
				// onto the image to prevent this weird parsing.
				imageUrl = imageUrl.substring(0, imageUrl.length() - 4) + "-s" + imageUrl.substring(imageUrl.length() - 4, imageUrl.length());
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