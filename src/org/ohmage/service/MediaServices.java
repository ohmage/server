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
import java.util.List;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Image;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IImageQueries;
import org.ohmage.query.IMediaQueries;

/**
 * This class is responsible for all operations pertaining to media (url_based_resource). 
 * The functions in this class may read information from other entities, but their
 * parameters, return values, and changes to the system should pertain only to
 * media.
 * 
 * @author Hongsuda T.
 */
public final class MediaServices {
	private static MediaServices instance;
	private IMediaQueries mediaQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iImageQueries is null
	 */
	private MediaServices(IMediaQueries imediaQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(imediaQueries == null) {
			throw new IllegalArgumentException("An instance of IMediaQueries is required.");
		}
		
		mediaQueries = imediaQueries;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static MediaServices instance() {
		return instance;
	}
	
	/**
	 * Checks if a media exists or not and compares that to whether or not it
	 * should exist.
	 * 
	 * @param mediaId The unique identifier for the image.
	 * 
	 * @param shouldExist Whether or not the image should exist.
	 * 
	 * @throws ServiceException Thrown if the image does not exist and it 
	 * 							should or does exist and it should not, or if
	 * 							there is an error.
	 */
	public void verifyMediaExistance(final UUID mediaId, final boolean shouldExist) throws ServiceException {
		
		try {
			Boolean mediaExists = mediaQueries.getMediaExists(mediaId);
			
			if(mediaExists && (! shouldExist)) {
				throw new ServiceException(
						ErrorCode.MEDIA_INVALID_ID, 
						"The media already exists: " + mediaId);
			}
			else if((! mediaExists) && shouldExist) {
				throw new ServiceException(
						ErrorCode.MEDIA_INVALID_ID, 
						"The image does not exist: " + mediaId);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	
	/**
	 * Retrieves the URL of a media.
	 * 
	 * @param mediaId The image's unique identifier.
	 * 
	 * @return A URL to the image.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public URL getMediaUrl(final UUID mediaId) throws ServiceException {
		try {
			return mediaQueries.getMediaUrl(mediaId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	

}