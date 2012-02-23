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

import org.ohmage.exception.DomainException;

/**
 * A representation of an image.
 * 
 * @author John Jenkins
 */
public class Image {
	private final URL url;
	
	/**
	 * Creates a new Image object from a URL object.
	 * 
	 * @param url The URL object to use to retrieve the image.
	 * 
	 * @throws DomainException The URL is null.
	 */
	public Image(
			final URL url) 
			throws DomainException {
		
		if(url == null) {
			throw new DomainException("The image's URL is null.");
		}
		
		this.url = url;
		
	}
	
	/**
	 * Returns the size of the image.
	 * 
	 * @return The size of the image.
	 * 
	 * @throws DomainException There was a problem connecting to the image.
	 */
	public long getSize() throws DomainException {
		try {
			return url.openConnection().getContentLength();
		}
		catch(IOException e) {
			throw new DomainException(
					"Problem retrieving the image's length.", e);
		}
	}
	
	/**
	 * Returns an InputStream connected to the image.
	 * 
	 * @return An InputStream
	 * 
	 * @throws DomainException There was a problem connecting to the image.
	 */
	public InputStream openStream() throws DomainException {
		try {
			return url.openStream();
		}
		catch(IOException e) {
			throw new DomainException("Error opening stream.", e);
		}
	}
	
	/**
	 * Returns a URLConnection connected to the image.
	 * 
	 * @return A URLConnection
	 * 
	 * @throws DomainException There was a problem connecting to the image.
	 */
	public URLConnection openConnection() throws DomainException {
		try {
			return url.openConnection();
		}
		catch(IOException e) {
			throw new DomainException("Error opening connection.", e);
		}
	}
}
