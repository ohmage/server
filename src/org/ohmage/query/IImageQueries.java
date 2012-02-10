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

import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface IImageQueries {

	/**
	 * Retrieves whether or not an image with the given ID exists.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns true if the image exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean getImageExists(UUID imageId) throws DataAccessException;

	/**
	 * Retrieves the URL for the image if the image exists. If the image does
	 * not exist, null is returned. 
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @return Returns the URL for the image if it exists; otherwise, null is
	 * 		   returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	String getImageUrl(UUID imageId) throws DataAccessException;

	/**
	 * Deletes an image reference from the database and, if successful, deletes
	 * the image off of the file system.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void deleteImage(UUID imageId) throws DataAccessException;

	/**
	 * Deletes the image off of the hard disk only. This means that a reference
	 * to the image may still exist in the database.<br />
	 * <br />
	 * This bugs me, but the only use case for this function is when we are 
	 * doing cascading deletes in the DB via user or campaign deletion, and we
	 * know that the references should have already been deleted. It would be
	 * wasteful to double check that the references were deleted; however, if
	 * this was used improperly, it could easily put us in a state where images
	 * were deleted but their references still existed. Use this with caution
	 * and only when you know the reference no longer exists. Otherwise, use
	 * {@link #deleteImage(UUID)}.
	 * 
	 * @param imageUrl A link to the image's URL.
	 * 
	 * @see #deleteImage(UUID)
	 */
	public void deleteImageDiskOnly(String imageUrl) ;
}
