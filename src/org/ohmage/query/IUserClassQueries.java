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
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.Clazz;
import org.ohmage.exception.DataAccessException;

public interface IUserClassQueries {

	/**
	 * Queries the database to see if a user belongs to a class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param username The username for the user.
	 * 
	 * @return Whether or not the user belongs to the class.
	 */
	boolean userBelongsToClass(String classId, String username)
			throws DataAccessException;

	/**
	 * Retrieves all of the users in a class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @return Returns a List of usernames of all of the users in a class.
	 */
	List<String> getUsersInClass(String classId) throws DataAccessException;

	/**
	 * Queries the database to get the role of a user in a class. If a user 
	 * doesn't have a role in a class, null is returned.
	 * 
	 * @param classId A class' unique identifier.
	 * 
	 * @param username A the username of the user whose role is being checked.
	 * 
	 * @return Returns the user's role in the class unless they have no role in
	 * 		   the class in which case null is returned.
	 */
	Clazz.Role getUserClassRole(String classId, String username)
			throws DataAccessException;
	
	/**
	 * Returns the list of roles for a user in a set of classes.
	 * 
	 * @param username The user's username.
	 * 
	 * @param classIds A set of class IDs.
	 * 
	 * @return The set of the class roles for the user in the classes.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Set<Clazz.Role> getUserClassRoles(
			final String username, 
			final Set<String> classIds) 
			throws DataAccessException;
	
	/**
	 * Returns a Map of all classes and the user's respective role in that 
	 * class for all classes to which the user is associated.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A map of class ID to the user's respective role.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Map<String, Clazz.Role> getClassAndRoleForUser(
			final String username)
			throws DataAccessException;

	/**
	 * Returns a Map of all classes and the user's respective role in that 
	 * class for all users in the user set.
	 * 
	 * @param userSet A set of usernames.
	 * 
	 * @return A map of user and class ID to the user's respective role.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Map<String, Map<String, Clazz.Role>> getClassAndRoleForUserSet(
			final Set<String> userSet)
			throws DataAccessException;
	
	
	/**
	 * Returns the set of all class IDs and their names with which a user is
	 * associated.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A, possibly empty but never null, map of class unique 
	 * 		   identifiers to their name for all of the classes to which a user
	 * 		   is associated.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Map<String, String> getClassIdsAndNameForUser(String username)
			throws DataAccessException;

	/**
	 * Retrieves the list of class identifiers for a user with a given role in
	 * that class.
	 * 
	 * @param username The user's username.
	 *  
	 * @param role The user's class role.
	 * 
	 * @return The list of class identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getClassIdsForUserWithRole(final String username,
			final Clazz.Role role) throws DataAccessException;

	/**
	 * Adds a given user to a given class with a given role.
	 * 
	 * @param username The user's username.
	 * 
	 * @param classId The class' ID.
	 * 
	 * @param classRole The class role for the user.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public void addUserToClassWithRole(
			final String username,
			final String classId,
			final Clazz.Role classRole)
			throws DataAccessException;
}