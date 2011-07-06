package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserClassDaos;
import org.ohmage.dao.UserDaos;
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
				request.setFailed(ErrorCodes.CLASS_USER_DOES_NOT_BELONG, "The user does not belong to the class: " + classId);
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
	public static void userIsPrivilegedOrAdmin(Request request, String classId, String username) throws ServiceException {
		try {
			if(! ClassRoleCache.ROLE_PRIVILEGED.equals(UserClassDaos.userClassRole(classId, username))) {
				if(! UserDaos.userIsAdmin(username)) {
					request.setFailed(ErrorCodes.CLASS_INSUFFICIENT_PERMISSIONS, "The user is not privileged in the class.");
					throw new ServiceException("The user is not privileged in the class.");
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}