package org.ohmage.validator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about images.
 * 
 * @author John Jenkins
 */
public final class ImageValidators {
	private static final Logger LOGGER = Logger.getLogger(ImageValidators.class);
	
	/**
	 * These are the different possible values for an image's size.
	 * 
	 * @author John Jenkins
	 */
	public static enum ImageSize { SMALL };
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ImageValidators() {}
	
	/**
	 * Validates that an image ID is a valid image ID. If it is null or 
	 * whitespace only, null is returned. Otherwise, the image is returned or
	 * an exception is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns null if the image ID is null or whitespace only; 
	 * 		   otherwise, the image ID is returned.
	 * 
	 * @throws ValidationException Thrown if the image ID is not null, not
	 * 							   whitespace only, and not a valid image ID.
	 */
	public static String validateId(Request request, String imageId) throws ValidationException {
		LOGGER.info("Validating an image ID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(imageId)) {
			return null;
		}
		
		if(StringUtils.isValidUuid(imageId.trim())) {
			return imageId.trim();
		}
		else {
			request.setFailed(ErrorCodes.IMAGE_INVALID_ID, "The image ID is not a valid image ID: " + imageId);
			throw new ValidationException("The image ID is not a valid image ID: " + imageId);
		}
	}
	
	/**
	 * Validates that an image size value is a valid image size value. If it is
	 * null or whitespace only, null is returned. Otherwise, an ImageSize is
	 * returned.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param imageSize The image size to be validated.
	 * 
	 * @return Returns null if the image size is null or whitespace only;
	 * 		   otherwise, an ImageSize representing the image size is returned.
	 * 
	 * @throws ValidationException Thrown if the image size is not null, not
	 * 							   whitespace only, and not a valid ImageSize.
	 */
	public static ImageSize validateImageSize(Request request, String imageSize) throws ValidationException {
		LOGGER.info("Validating an image size value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(imageSize)) {
			return null;
		}
		
		try {
			return ImageSize.valueOf(imageSize.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.IMAGE_INVALID_SIZE, "The image size value is an unknown value: " + imageSize);
			throw new ValidationException("The image size value is an unknown value: " + imageSize, e);
		}
	}
	
	/**
	 * Validates that an image's contents as a byte array are decodable as an 
	 * image.
	 *  
	 * @param request The Request that is performing this validation.
	 * 
	 * @param imageContents The image's contents as a byte array.
	 * 
	 * @return Returns null if the image's contents are null or have a length 
	 * 		   of zero; otherwise, a BufferedImage representing a decoded  
	 * 		   version of the image are returned.
	 * 
	 * @throws ValidationException Thrown if the image is not null, has a 
	 * 							   length greater than 0, and isn't decodable 
	 * 							   as any type of known image.
	 */
	public static BufferedImage validateImageContents(Request request, byte[] imageContents) throws ValidationException {
		if((imageContents == null) || (imageContents.length == 0)) {
			return null;
		}
		
		try {
			return ImageIO.read(new ByteArrayInputStream(imageContents));
		}
		catch(IOException e) {
			request.setFailed();
			throw new ValidationException("There was an error while reading the image's contents.", e);
		}
	}
}