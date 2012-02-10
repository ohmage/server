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

import java.util.List;

import org.ohmage.exception.DataAccessException;

public interface IUserClassDocumentQueries {

	/**
	 * Gathers the unique identifiers for all of the documents associated with
	 * a class.
	 * 
	 * @param username The username of the requesting user.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @return A list of the documents associated with a class. The list may be
	 * 		   empty but never null.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getVisibleDocumentsToUserInClass(String username,
			String classId) throws DataAccessException;

	/**
	 * Retrieves whether or not the user is privileged any of the classes 
	 * associated with the document.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique identifier of the document.
	 * 
	 * @return Returns true if the user is privileged in any class that is 
	 * 		   associated with the campaign.
	 */
	Boolean getUserIsPrivilegedInAnyClassAssociatedWithDocument(
			String username, String documentId) throws DataAccessException;

}
