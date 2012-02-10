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
package org.ohmage.query;

import java.util.Collection;
import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface IUserImageQueries {

	/**
	 * Returns whether or not a photo prompt response exists for some user 
	 * whose response value is the photo's ID.
	 *  
	 * @param username The username of the user.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Whether or not a photo prompt response exists for some user
	 * 		   whose response value is the photo's ID.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean responseExistsForUserWithImage(String username, UUID imageId)
			throws DataAccessException;

	/**
	 * Retrieves the username of the user that created this image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Returns the creator of the image or null if the image doesn't
	 * 		   exist.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	String getImageOwner(UUID imageId) throws DataAccessException;

	/**
	 * Retrieves the URL of all images that are associated with a given user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A collection of URLs as Strings.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Collection<String> getImageUrlsFromUsername(String username)
			throws DataAccessException;
}
