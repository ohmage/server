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
package org.ohmage.service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Image;
import org.ohmage.domain.Image.Size;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IImageQueries;

/**
 * This class is responsible for all operations pertaining only to images. The
 * functions in this class may read information from other entities, but their
 * parameters, return values, and changes to the system should pertain only to
 * images.
 * 
 * @author John Jenkins
 */
public final class ImageServices {
	private static ImageServices instance;
	private IImageQueries imageQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iImageQueries is null
	 */
	private ImageServices(IImageQueries iImageQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(iImageQueries == null) {
			throw new IllegalArgumentException("An instance of IImageQueries is required.");
		}
		
		imageQueries = iImageQueries;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static ImageServices instance() {
		return instance;
	}
	
	/**
	 * Checks if an image exists or not and compares that to whether or not it
	 * should exist.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @param shouldExist Whether or not the image should exist.
	 * 
	 * @throws ServiceException Thrown if the image does not exist and it 
	 * 							should or does exist and it should not, or if
	 * 							there is an error.
	 */
	public void verifyImageExistance(final UUID imageId, final boolean shouldExist) throws ServiceException {
		
		try {
			Boolean imageExists = imageQueries.getImageExists(imageId);
			
			if(imageExists && (! shouldExist)) {
				throw new ServiceException(
						ErrorCode.IMAGE_INVALID_ID, 
						"The image already exists.");
			}
			else if((! imageExists) && shouldExist) {
				throw new ServiceException(
						ErrorCode.IMAGE_INVALID_ID, 
						"The image does not exist.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves an InputStream connected to the image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @param size An ImageSize indicating if the smaller version of the image
	 * 			   is desired.
	 * 
	 * @return An image object representing the image.
	 * 
	 * @throws ServiceException Thrown if there is an error, the URL is 
	 * 							malformed, or there is an error connecting to
	 * 							them.
	 */
	public Image getImage(
			final UUID imageId, 
			final Image.Size size) 
			throws ServiceException {
		
		try {
			URL imageUrl = imageQueries.getImageUrl(imageId);
			if(imageUrl == null) {
				return null;
			}
			
			Map<Size, URL> urlMapping = new HashMap<Size, URL>();
			urlMapping.put(Image.ORIGINAL, imageUrl);
			return new Image(imageId, urlMapping);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		catch(DomainException e) {
			throw new ServiceException(
					"There was a problem creating the image object.", 
					e);
		}
	}
	
	/**
	 * Retrieves the URL of an image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return A URL to the image.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public URL getImageUrl(final UUID imageId) throws ServiceException {
		try {
			return imageQueries.getImageUrl(imageId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
