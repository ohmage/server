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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audio;
import org.ohmage.domain.OFile;
import org.ohmage.domain.IMedia;
import org.ohmage.domain.Image;
import org.ohmage.domain.Media;
import org.ohmage.domain.Video;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
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
	private static final Logger LOGGER =
			Logger.getLogger(MediaServices.class);
	
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
	
	
	public IMedia getMediaHelper(final UUID id, final Class<? extends IMedia> mediaType) throws ServiceException {
		try {
			Map<String, String> result = mediaQueries.getMediaUrlAndMetadata(id);
			
			if (result == null) {
				LOGGER.debug("HT: There is no result found!");
				throw new ServiceException(ErrorCode.MEDIA_NOT_FOUND, "The media object doesn't exist");
			}
			String urls[] = result.keySet().toArray(new String[0]);
			if (urls.length > 1) {
				throw new ServiceException(ErrorCode.SYSTEM_GENERAL_ERROR, "There are multiple urls associated with id:" + id.toString());
			}
			URL url = new URL(urls[0]);
			String info = result.get(urls[0]);
			
			if (mediaType.equals(Media.class))
			return new Media(id, url, info);
			else if (mediaType.equals(Image.class))
				return new Image(id, url, info);
			else if (mediaType.equals(Audio.class))
				return new Audio(id, url, info);
			else if (mediaType.equals(Video.class))
				return new Video(id, url, info);
			else if (mediaType.equals(OFile.class))
				return new OFile(id, url, info);
			else throw new ServiceException("The type is not a media object:" + mediaType.getName());
			
		}
		catch(MalformedURLException e) {
			throw new ServiceException(
				"The URL is malformed for the media: " + id.toString(),
				e);
		}
		catch(DomainException e) {
			throw new ServiceException("Can't create media from DB", e);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
	}
	/**
	 * Returns a Media object.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return A media object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Media getMedia(final UUID id) throws ServiceException {
		return (Media) getMediaHelper(id, Media.class);	
	}
	
	
	/**
	 * Returns an Audio object representing the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return An Audio object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Audio getAudio(final UUID id) throws ServiceException {
		return (Audio) getMediaHelper(id, Audio.class);
	}

	/**
	 * Returns a Video object representing the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return A Video object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Video getVideo(final UUID id) throws ServiceException {
		return (Video) getMediaHelper(id, Video.class);
	}


	
}