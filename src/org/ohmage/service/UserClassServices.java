package org.ohmage.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.UserClassQueries;
import org.ohmage.query.UserQueries;

/**
 * This class contains the services for user-class relationships.
 * 
 * @author John Jenkins
 */
public final class UserClassServices {
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private UserClassServices() {}
	
	/**
	 * Ensures that the class exists and that the user belongs to the class in
	 * some capacity.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param username The username of the user that must belong to the class.
	 * 
	 * @throws ServiceException Thrown if there is an error, if the class  
	 * 							doesn't exist, or if the class does exist but
	 * 							the user doesn't belong to the class.
	 */
	public static void classExistsAndUserBelongs(final String classId, 
			final String username) throws ServiceException {
		
		ClassServices.checkClassExistence(classId, true);
		
		try {
			if(! UserClassQueries.userBelongsToClass(classId, username)) {
				throw new ServiceException(
						ErrorCode.USER_INVALID_USERNAME, 
						"The user does not belong to the class: " + classId);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that all of the classes in the class list exists and that the 
	 * user belong to each of them in some capacity.
	 * 
	 * @param classIds A List of unique identifiers for classes.
	 * 
	 * @param username The username of the user that must exist in each of the
	 * 				   classes.
	 * 
	 * @throws ServiceException Thrown if there is an error, if any of the 
	 * 							classes don't exist, or if the user doesn't
	 * 							belong to any of the classes.
	 */
	public static void classesExistAndUserBelongs(
			final Collection<String> classIds, final String username) 
			throws ServiceException {
		
		for(String classId : classIds) {
			classExistsAndUserBelongs(classId, username);
		}
	}
	
	/**
	 * Verifies that the user's role in the class is the same as a given role.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param classRole The class role that the user must have in the class.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have the role in the
	 * 							class or if there is an error.
	 */
	public static void userHasRoleInClass(final String username, 
			final String classId, final Clazz.Role classRole) 
			throws ServiceException {
		
		try {
			if(! UserClassQueries.getUserClassRole(classId, username).equals(classRole)) {
				throw new ServiceException(
						ErrorCode.CLASS_INSUFFICIENT_PERMISSIONS, 
						"The user doesn't have sufficient permissions for the following class: " + 
							classId);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user's role in each of the classes is the same as the
	 * given role.
	 * 
	 * @param username The user's username.
	 * 
	 * @param classIds The Collection of unique identifiers for the classes.
	 * 
	 * @param classRole The role that the user must have in all of the clasess.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have the specified
	 * 							role in each of the classes or there is an 
	 * 							error.
	 */
	public static void userHasRoleInClasses(final String username, 
			final Collection<String> classIds, final Clazz.Role classRole) 
			throws ServiceException {
		
		for(String classId : classIds) {
			userHasRoleInClass(username, classId, classRole);
		}
	}
	
	/**
	 * Checks if the user is privileged in the class or an admin.
	 *  
	 * @param classId The class to check if the user is privileged.
	 * 
	 * @param username The user to check if they are privileged in the class.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not privileged in the class nor are they an 
	 * 							admin.
	 */
	public static void userIsAdminOrPrivileged(final String classId, 
			final String username) throws ServiceException {
		
		try {
			if((! Clazz.Role.PRIVILEGED.equals(UserClassQueries.getUserClassRole(classId, username))) &&
			   (! UserQueries.userIsAdmin(username))) {
				throw new ServiceException(
						ErrorCode.CLASS_INSUFFICIENT_PERMISSIONS, 
						"The user is not privileged in the class.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the user is an admin or that they are privileged in all of 
	 * the classes in the class list.
	 * 
	 * @param username The username of the user that must be an admin or 
	 * 				   privileged in all of the classes in the list.
	 * 
	 * @param classIds A List of class identifiers.
	 * 
	 * @throws ServiceException Thrown if the user isn't an admin and isn't 
	 * 							privileged in one of the classes or there is an
	 * 							error.
	 */
	public static void userIsAdminOrPrivilegedInAllClasses(
			final String username, final Collection<String> classIds) 
			throws ServiceException {
		
		try {
			// If the user is an admin, return.
			if(UserQueries.userIsAdmin(username)) {
				return;
			}
			
			// For each of the classes in the list, the user must be 
			// privileged.
			for(String classId : classIds) {
				if(! Clazz.Role.PRIVILEGED.equals(UserClassQueries.getUserClassRole(classId, username))) {
					throw new ServiceException(
							ErrorCode.CLASS_INSUFFICIENT_PERMISSIONS, 
							"The user is not and admin nor privileged in a class: " + 
								classId);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of all the users in the class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @return Returns a List of usernames for all of the users in the class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getUsersInClass(final String classId) 
			throws ServiceException {
		
		try {
			return UserClassQueries.getUsersInClass(classId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a Set of all of the usernames in all of the classes.
	 * 
	 * @param classIds A Collection of class identifiers.
	 * 
	 * @return A Set of all of the users in all of the classes without
	 * 		   duplicates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Set<String> getUsersInClasses(
			final Collection<String> classIds) throws ServiceException {
		
		Set<String> usernames = new HashSet<String>();
		for(String classId : classIds) {
			usernames.addAll(getUsersInClass(classId));
		}
		return usernames;
	}
	
	/**
	 * Retrieves the ID of all of the classes to which the user belongs. If a
	 * role is given, the result will only contain the classes in which the
	 * user has the given role.
	 * 
	 * @param username The user's username.
	 * 
	 * @param role The class role or null if all classes are desired.
	 * 
	 * @return The set of classes.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Set<String> getClassesForUser(final String username, 
			final Clazz.Role role) throws ServiceException {
		
		try {
			Set<String> allClasses = 
				UserClassQueries.getClassIdsAndNameForUser(username).keySet();
			
			if(role == null) {
				return allClasses;
			}
			else {
				allClasses.retainAll(
						UserClassQueries.getClassIdsForUserWithRole(
								username, 
								role
							)
					);
				
				return allClasses;
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the personal information for all of the users in all of the 
	 * classes without duplicates.
	 * 
	 * @param classIds The Collection of unique identifiers for the classes.
	 * 
	 * @return Returns a Map of usernames to UserPersonal information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<String, UserPersonal> getPersonalInfoForUsersInClasses(
			final Collection<String> classIds) throws ServiceException {
		
		try {
			Map<String, UserPersonal> result = new HashMap<String, UserPersonal>();
			Collection<String> usernames = getUsersInClasses(classIds);
			
			for(String username : usernames) {
				result.put(username, UserQueries.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}