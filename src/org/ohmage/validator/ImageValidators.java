/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Image;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about images.
 * 
 * @author John Jenkins
 */
public final class ImageValidators {
	private static final Logger LOGGER = Logger.getLogger(ImageValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ImageValidators() {}
	
	/**
	 * Validates that an image ID is a valid image ID. If it is null or 
	 * whitespace only, null is returned. Otherwise, the image is returned or
	 * an exception is thrown.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns null if the image ID is null or whitespace only; 
	 * 		   otherwise, the image ID is returned.
	 * 
	 * @throws ValidationException Thrown if the image ID is not null, not
	 * 							   whitespace only, and not a valid image ID.
	 */
	public static UUID validateId(final String imageId) 
			throws ValidationException {
		
		LOGGER.info("Validating an image ID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(imageId)) {
			return null;
		}
		
		try {
			return UUID.fromString(imageId);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.IMAGE_INVALID_ID, 
					"The image ID is not a valid image ID: " + imageId);
		}
	}
	
	/**
	 * Validates that an image size value is a valid image size value. If it is
	 * null or whitespace only, null is returned. Otherwise, an ImageSize is
	 * returned.
	 * 
	 * @param imageSize The image size to be validated.
	 * 
	 * @return Returns null if the image size is null or whitespace only;
	 * 		   otherwise, an ImageSize representing the image size is returned.
	 * 
	 * @throws ValidationException Thrown if the image size is not null, not
	 * 							   whitespace only, and not a valid ImageSize.
	 */
	public static Image.Size validateImageSize(final String imageSize) 
			throws ValidationException {
		
		LOGGER.info("Validating an image size value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(imageSize)) {
			return Image.ORIGINAL;
		}
		
		try {
			return Image.getSize(imageSize.trim());
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.IMAGE_INVALID_SIZE, 
					"The image size value is an unknown value: " + imageSize, 
					e);
		}
	}
	
	/**
	 * Validates that an image's contents as a byte array are decodable as an 
	 * image.
	 * 
	 * @param imageId The ID for this image.
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
	public static Image validateImageContents(
			final UUID imageId,
			final byte[] imageContents) throws ValidationException {
		
		if(imageId == null) {
			throw new ValidationException("The image ID is null.");
		}
		if((imageContents == null) || (imageContents.length == 0)) {
			return null;
		}
		
		try {
			return new Image(imageId, new ByteArrayInputStream(imageContents));
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.IMAGE_INVALID_DATA,
				"The image data was not valid image data.",
				e);
		}
	}
}