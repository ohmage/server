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
package org.ohmage.domain;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ohmage.exception.DomainException;

/**
 * A representation of an image.
 * 
 * @author John Jenkins
 */
public class Image {
	/**
	 * These are the different possible values for an image's size.
	 * 
	 * @author John Jenkins
	 */
	public static enum Size { 
		ORIGINAL,
		SMALL;
		
		public static Size getValue(final String value) {
			if(value == null) {
				throw new IllegalArgumentException("The value is null.");
			}
			
			for(Size imageSize : values()) {
				if(imageSize.name().toLowerCase().equals(value.toLowerCase())) {
					return imageSize;
				}
			}
			
			throw new IllegalArgumentException(
					"The value is unknown: " + value);
		}
		
		public String toString() {
			return name().toLowerCase();
		}
	};
	
	private final UUID id;
	
	// For this image, this maps the different sized images to their unique,
	// although probably similar, URLs.
	private final Map<Size, URL> sizeToUrlMap;
	
	/**
	 * Creates a new Image object from a URL object.
	 * 
	 * @param url The URL object to use to retrieve the image.
	 * 
	 * @throws DomainException The URL is null.
	 */
	public Image(
			final UUID id, 
			final Map<Size, URL> sizeToUrlMap) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The image's ID is null.");
		}
		
		if(sizeToUrlMap == null) {
			throw new DomainException("The size to URL is null.");
		}
		else if(sizeToUrlMap.isEmpty()) {
			throw new DomainException("The size to URL map is empty.");
		}
		
		this.id = id;
		this.sizeToUrlMap = new HashMap<Size, URL>(sizeToUrlMap);
	}
	
	/**
	 * The ID of the image.
	 * 
	 * @return The image's ID.
	 */
	public UUID getId() {
		return id;
	}
	
	/**
	 * Returns the set of image sizes for this image.
	 * 
	 * @return The set of sizes for this image.
	 */
	public Set<Size> getSizes() {
		return Collections.unmodifiableSet(sizeToUrlMap.keySet());
	}
	
	/**
	 * Returns the URL for the image with the given size.
	 * 
	 * @param size The size of the image for which the URL is requested.
	 * 
	 * @return The URL for the image with the given size.
	 * 
	 * @throws DomainException The image does not have a URL for the given 
	 * 						   size.
	 */
	public URL getUrl(final Size size) throws DomainException {
		URL url = sizeToUrlMap.get(size);
		if(url == null) {
			throw new DomainException(
					"The image does not have a URL for the given size: " +
						size.toString());
		}
		else {
			return url;
		}
	}
	
	/**
	 * Returns a URLConnection connected to the image.
	 * 
	 * @param size Which size of the file to connect to.
	 * 
	 * @return A URLConnection to the image with the given size.
	 * 
	 * @throws DomainException The image doesn't have a URL for that size or 
	 * 						   there was a problem connecting to the image.
	 */
	public URLConnection openConnection(
			final Size size) 
			throws DomainException {
		
		try {
			return getUrl(size).openConnection();
		}
		catch(IOException e) {
			throw new DomainException("Error opening connection.", e);
		}
	}
	
	/**
	 * Returns the size of the image.
	 * 
	 * @param size Which size of the file to connect to.
	 * 
	 * @return The size of the image in bytes.
	 * 
	 * @throws DomainException The image doesn't have a URL for that size or 
	 * 						   there was a problem connecting to the image.
	 */
	public long getSizeBytes(final Size size) throws DomainException {
		return openConnection(size).getContentLength();
	}
	
	/**
	 * Returns an InputStream connected to the image. It is the caller's 
	 * responsibility to close the stream when they are done.
	 * 
	 * @param size Which size of the file to connect to.
	 * 
	 * @return An InputStream to the file with the given size.
	 * 
	 * @throws DomainException The image doesn't have a URL for that size or 
	 * 						   there was a problem connecting to the image.
	 */
	public InputStream openStream(final Size size) throws DomainException {
		try {
			return openConnection(size).getInputStream();
		}
		catch(IOException e) {
			throw new DomainException("Error opening stream.", e);
		}
	}
}