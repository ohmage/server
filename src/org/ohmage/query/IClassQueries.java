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
import java.util.List;
import java.util.Map;

import org.ohmage.domain.Clazz;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.impl.QueryResultsList;

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public interface IClassQueries {

	/**
	 * Creates a new class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param className The name of the class.
	 * 
	 * @param classDescription An optional description of the class. This may
	 * 						   be null.
	 * 
	 * @throws DataAccessException Thrown if there is an error executing any of
	 * 							   the SQL.
	 */
	void createClass(String classId, String className, String classDescription)
			throws DataAccessException;

	/**
	 * Queries the database to see if a class exists.
	 * 
	 * @param classId The ID of the class in question.
	 * 
	 * @return Whether or not the class exists.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean getClassExists(String classId) throws DataAccessException;
	
	/**
	 * Returns the unique ID for every class in the system.
	 * 
	 * @return The unique ID for every class in the system.
	 * 
	 * @throws DataAccessException Thrown if there was an error.
	 */
	List<String> getAllClassIds() throws DataAccessException;
	
	/**
	 * Queries the database for class IDs whose ID is similar to the given one.
	 * 
	 * @param partialId The partial class ID to match against all other class
	 * 					IDs.
	 * 
	 * @return A, possibly empty but never null, collection of valid class IDs
	 * 		   that contain the given one.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getClassIdsFromPartialId(String partialId) 
			throws DataAccessException;
	
	/**
	 * Queries the database for class IDs whose name is similar to the given 
	 * one.
	 * 
	 * @param partialName The partial class name to match against all other 
	 * 					  class names.
	 * 
	 * @return A, possibly empty but never null, collection of valid class IDs
	 * 		   that contain the given one.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getClassIdsFromPartialName(String partialName) 
			throws DataAccessException;
	
	/**
	 * Queries the database for class IDs whose description is similar to the 
	 * given one.
	 * 
	 * @param partialDescription The partial class description to match against 
	 * 							 all other classIDs.
	 * 
	 * @return A, possibly empty but never null, collection of valid class IDs
	 * 		   that contain the given one.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getClassIdsFromPartialDescription(String partialDescription) 
			throws DataAccessException;

	/**
	 * Aggregates all of the information about all of the classes visible to a
	 * user and then limits those results to only the ones in the collection of
	 * class IDs.
	 * 
	 * @param username The requesting user's username. This is required.
	 * 
	 * @param classIds The collection of class IDs to limit the results. This 
	 * 				   is optional and may be null.
	 * 
	 * @return The query result containing the total number of results as well 
	 * 		   as the results for this page.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public QueryResultsList<Clazz> getClassesInformation(
			final String username,
			final Collection<String> classIds) 
			throws DataAccessException;

	/**
	 * Retrieves a map of usernames to that user's class role. If the user is
	 * not an admin nor are they privileged in the class, then the class roles
	 * will all be null.
	 * 
	 * @param classId The unique identifier for a class.
	 * 
	 * @return A map of usernames to class role.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	Map<String, Clazz.Role> getUserRolePairs(
			final String username, 
			final String classId)
			throws DataAccessException;

	/**
	 * Updates a class' information and adds and removes users from the class
	 * all as requested.
	 * 
	 * @param classId The class identifier to use to lookup which class to 
	 * 				  update.
	 * 
	 * @param className The class' new name or null in which case the name will
	 * 					not be updated.
	 * 
	 * @param classDescription The class' new description or null in which case
	 * 						   the description will not be updated.
	 *  
	 * @param userAndRolesToAdd A list of users and respective roles to 
	 * 							associate with this class.
	 * 
	 * @param usersToRemove A list of users and respective roles to remove from
	 * 						this class.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */

	List<String> updateClass(String classId, String className,
			String classDescription, Map<String, Clazz.Role> userAndRolesToAdd,
			Collection<String> usersToRemove) throws DataAccessException;

	/**
	 * Deletes a class.
	 * 
	 * @param classId The unique identifier for the class to be deleted.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void deleteClass(String classId) throws DataAccessException;

}
