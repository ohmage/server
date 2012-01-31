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
	Boolean userBelongsToClass(String classId, String username)
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

}