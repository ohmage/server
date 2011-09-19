package org.ohmage.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.dao.UserClassDaos;
import org.ohmage.dao.UserDaos;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

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
	 * @param request The request that is performing this check.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param username The username of the user that must belong to the class.
	 * 
	 * @throws ServiceException Thrown if there is an error, if the class  
	 * 							doesn't exist, or if the class does exist but
	 * 							the user doesn't belong to the class.
	 */
	public static void classExistsAndUserBelongs(Request request, String classId, String username) throws ServiceException {
		ClassServices.checkClassExistence(request, classId, true);
		
		try {
			if(! UserClassDaos.userBelongsToClass(classId, username)) {
				request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The user does not belong to the class: " + classId);
				throw new ServiceException("The user does not belong to the class: " + classId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that all of the classes in the class list exists and that the 
	 * user belong to each of them in some capacity.
	 * 
	 * @param request The request that is performing this check.
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
	public static void classesExistAndUserBelongs(Request request, List<String> classIds, String username) throws ServiceException {
		for(String classId : classIds) {
			classExistsAndUserBelongs(request, classId, username);
		}
	}
	
	/**
	 * Verifies that the user's role in the class is the same as a given role.
	 * 
	 * @param request The Request that is performing this service.
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
	public static void userHasRoleInClass(Request request, String username, String classId, ClassRoleCache.Role classRole) throws ServiceException {
		try {
			if(! UserClassDaos.getUserClassRole(classId, username).equals(classRole)) {
				request.setFailed(ErrorCodes.CLASS_INSUFFICIENT_PERMISSIONS, "The user doesn't have sufficient permissions for the following class: " + classId);
				throw new ServiceException("The user doesn't have sufficient permissions for the following class: " + classId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user's role in each of the classes is the same as the
	 * given role.
	 * 
	 * @param request The Request that is performing this service.
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
	public static void userHasRoleInClasses(Request request, String username, Collection<String> classIds, ClassRoleCache.Role classRole) throws ServiceException {
		for(String classId : classIds) {
			userHasRoleInClass(request, username, classId, classRole);
		}
	}
	
	/**
	 * Checks if the user is privileged in the class or an admin.
	 *  
	 * @param request The request that is performing this check.
	 * 
	 * @param classId The class to check if the user is privileged.
	 * 
	 * @param username The user to check if they are privileged in the class.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not privileged in the class nor are they an 
	 * 							admin.
	 */
	public static void userIsAdminOrPrivileged(Request request, String classId, String username) throws ServiceException {
		try {
			if((! ClassRoleCache.Role.PRIVILEGED.equals(UserClassDaos.getUserClassRole(classId, username))) &&
			   (! UserDaos.userIsAdmin(username))) {
				request.setFailed(ErrorCodes.CLASS_INSUFFICIENT_PERMISSIONS, "The user is not privileged in the class.");
				throw new ServiceException("The user is not privileged in the class.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the user is an admin or that they are privileged in all of 
	 * the classes in the class list.
	 * 
	 * @param request The request that performs this service.
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
	public static void userIsAdminOrPrivilegedInAllClasses(Request request, String username, Collection<String> classIds) throws ServiceException {
		try {
			// If the user is an admin, return.
			if(UserDaos.userIsAdmin(username)) {
				return;
			}
			
			// For each of the classes in the list, the user must be 
			// privileged.
			for(String classId : classIds) {
				if(! ClassRoleCache.Role.PRIVILEGED.equals(UserClassDaos.getUserClassRole(classId, username))) {
					request.setFailed(ErrorCodes.CLASS_INSUFFICIENT_PERMISSIONS, "The user is not and admin nor privileged in a class: " + classId);
					throw new ServiceException("The user is not and admin nor privileged in a class: " + classId);
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of all the users in the class.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @return Returns a List of usernames for all of the users in the class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getUsersInClass(Request request, String classId) throws ServiceException {
		try {
			return UserClassDaos.getUsersInClass(classId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a Set of all of the usernames in all of the classes.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param classIds A Collection of class identifiers.
	 * 
	 * @return A Set of all of the users in all of the classes without
	 * 		   duplicates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Set<String> getUsersInClasses(Request request, Collection<String> classIds) throws ServiceException {
		Set<String> usernames = new HashSet<String>();
		for(String classId : classIds) {
			usernames.addAll(getUsersInClass(request, classId));
		}
		return usernames;
	}
	
	/**
	 * Retrieves the personal information for all of the users in all of the 
	 * classes without duplicates.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param classIds The Collection of unique identifiers for the classes.
	 * 
	 * @return Returns a Map of usernames to UserPersonal information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<String, UserPersonal> getPersonalInfoForUsersInClasses(Request request, Collection<String> classIds) throws ServiceException {
		try {
			Map<String, UserPersonal> result = new HashMap<String, UserPersonal>();
			Collection<String> usernames = getUsersInClasses(request, classIds);
			
			for(String username : usernames) {
				result.put(username, UserDaos.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}