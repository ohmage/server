package org.ohmage.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.dao.ClassDaos;
import org.ohmage.dao.ClassDaos.UserAndClassRole;
import org.ohmage.domain.ClassInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to classes.
 * 
 * @author John Jenkins
 */
public final class ClassServices {
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private ClassServices() {}
	
	/**
	 * Creates a new class.
	 * 
	 * @param request The request that it is creating the new class.
	 * 
	 * @param classId The unique identifier for the new class.
	 * 
	 * @param className The new class' name.
	 * 
	 * @param classDescription An optional description for the new class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void createClass(Request request, String classId, String className, String classDescription) throws ServiceException {
		try {
			ClassDaos.createClass(classId, className, classDescription);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a class exists and compares that value to whether or not it
	 * should exist. If they don't match or there is an error, it will set the
	 * request as failed with an error message if the reason for failure is
	 * known and throw a ServiceException.<br />
	 * <br />
	 * Note: Passing in a value of null will always result in the class not
	 * existing.
	 * 
	 * @param request The request that is validating this class' identifier.
	 * 
	 * @param classId The class identifier to use to check for existence.
	 * 
	 * @param shouldExist Whether or not the class should already exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, the class doesn't
	 * 							exist and it should, or the class does exist
	 * 							and it shouldn't.
	 */
	public static void checkClassExistence(Request request, String classId, boolean shouldExist) throws ServiceException {
		try {
			if((classId != null) && ClassDaos.getClassExists(classId)) {
				if(! shouldExist) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class already exists: " + classId);
					throw new ServiceException("The class already exists: " + classId);
				}
			}
			else {
				if(shouldExist) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class does not exist: " + classId);
					throw new ServiceException("The class does not exist: " + classId);
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a list of classes exist and compares each result to whether or
	 * not they should exist. If any of them don't match or there is an error
	 * at any point, it will set the request as failed with an error message if
	 * the reason for failure is known and will throw a ServiceException. 
	 * 
	 * @param request The request that is having these classes' existence 
	 * 				  checked.
	 * 
	 * @param classIds The List of class identifiers whose existence need to be
	 * 				   checked.
	 * 
	 * @param shouldExist Whether or not each of the classes in the list need
	 * 					  to exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, or if any of the
	 * 							classes exist and they shouldn't, or if any of
	 * 							the classes don't exist and they should.
	 */
	public static void checkClassesExistence(Request request, Collection<String> classIds, boolean shouldExist) throws ServiceException {
		for(String classId : classIds) {
			checkClassExistence(request, classId, shouldExist);
		}
	}
	
	/**
	 * Retrieves the information about all of the classes in the class  
	 * identifier list.
	 * 
	 * @param request The request that is attempting to aggregate this 
	 * 				  information.
	 * 
	 * @param classIds A List of class identifiers to use to aggregate the
	 * 				   information.
	 * 
	 * @param requester The username of the user that is requesting this 
	 * 					information.
	 * 
	 * @return Returns a List of ClassInformation objects that contain the
	 * 		   information about the class. This may be an empty list, but it
	 * 		   will never be null.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<ClassInformation> getClassesInformation(Request request, List<String> classIds, String requester) throws ServiceException {
		try {
			return ClassDaos.getClassesInformation(classIds, requester);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Generates a Map of class IDs to a List of users and their roles for a 
	 * List of classes.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param classIds A List of unique identifiers for the classes that should
	 * 				   be added to the roster.
	 * 
	 * @return A Map of class IDs to a List of users and their roles in that
	 * 		   class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<String, List<UserAndClassRole>> generateClassRoster(Request request, List<String> classIds) throws ServiceException {
		try {
			Map<String, List<UserAndClassRole>> result = new HashMap<String, List<UserAndClassRole>>();
			
			for(String classId : classIds) {
				result.put(classId, ClassDaos.getUserRolePairs(classId));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}

	/**
	 * Updates the class.
	 * 
	 * @param request The request that is asking to have the class updated.
	 * 
	 * @param classId The unique identifier for the class to update.
	 * 
	 * @param className A new name for the class or null if the class name
	 * 					should not be updated.
	 * 
	 * @param classDescription A new description for the class or null if the
	 * 						   class' description should not be updated.
	 * 
	 * @param usersToAdd A List of usernames and respective roles that should 
	 * 					 be associated with the class.
	 * 
	 * @param usersToRemove A List of usernames and respective roles that 
	 * 						should be disassociated with the class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updateClass(Request request, String classId, String className, String classDescription, Map<String, ClassRoleCache.Role> usersToAdd, List<String> usersToRemove) throws ServiceException{
		try {
			ClassDaos.updateClass(classId, className, classDescription, usersToAdd, usersToRemove);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates the class via a class roster.
	 * 
	 * @param request The Request that is asking to have the class updated.
	 * 
	 * @param roster A Map of class IDs to Maps of usernames to class roles.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> updateClassViaRoster(Request request, Map<String, Map<String, ClassRoleCache.Role>> roster) throws ServiceException {
		try {
			List<String> warningMessages = new ArrayList<String>();
			
			for(String classId : roster.keySet()) {
				warningMessages.addAll(ClassDaos.updateClass(classId, null, null, roster.get(classId), null));
			}
			
			return warningMessages;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes the class.
	 * 
	 * @param request The request that is asking to have the class deleted.
	 * 
	 * @param classId The unique identifier or the class to be deleted.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteClass(Request request, String classId) throws ServiceException {
		try {
			ClassDaos.deleteClass(classId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}