package org.ohmage.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IClassQueries;
import org.ohmage.query.impl.ClassQueries.UserAndClassRole;

/**
 * This class contains the services that pertain to classes.
 * 
 * @author John Jenkins
 */
public final class ClassServices {
	private static ClassServices instance;
	private IClassQueries classQueries;

	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iClassQueries is null
	 */	
	private ClassServices(IClassQueries iClassQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iClassQueries == null) {
			throw new IllegalArgumentException("An instance of IClassQueries is required.");
		}
		
		classQueries = iClassQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static ClassServices instance() {
		return instance;
	}
	
	/**
	 * Creates a new class.
	 * 
	 * @param classId The unique identifier for the new class.
	 * 
	 * @param className The new class' name.
	 * 
	 * @param classDescription An optional description for the new class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createClass(final String classId, 
			final String className, final String classDescription) 
			throws ServiceException {
		
		try {
			classQueries.createClass(classId, className, classDescription);
		}
		catch(DataAccessException e) {
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
	 * @param classId The class identifier to use to check for existence.
	 * 
	 * @param shouldExist Whether or not the class should already exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, the class doesn't
	 * 							exist and it should, or the class does exist
	 * 							and it shouldn't.
	 */
	public void checkClassExistence(final String classId, 
			final boolean shouldExist) throws ServiceException {
		
		try {
			if((classId != null) && classQueries.getClassExists(classId)) {
				if(! shouldExist) {
					throw new ServiceException(
							ErrorCode.CLASS_INVALID_ID,
							"The class already exists: " + classId
						);
				}
			}
			else {
				if(shouldExist) {
					throw new ServiceException(
							ErrorCode.CLASS_INVALID_ID, 
							"The class does not exist: " + classId
						);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a list of classes exist and compares each result to whether or
	 * not they should exist. If any of them don't match or there is an error
	 * at any point, it will set the request as failed with an error message if
	 * the reason for failure is known and will throw a ServiceException. 
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
	public void checkClassesExistence(final Collection<String> classIds, 
			final boolean shouldExist) throws ServiceException {
		
		for(String classId : classIds) {
			checkClassExistence(classId, shouldExist);
		}
	}
	
	/**
	 * Returns the unique identifier for each class in the system.
	 * 
	 * @return The unique identifier for each class in the system.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getAllClassIds() throws ServiceException {
		try {
			return classQueries.getAllClassIds();
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves all of the valid class IDs that contain the parameterized
	 * partial class ID.
	 * 
	 * @param partialClassId The partial class ID.
	 * 
	 * @return The list of matching class IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getClassIdsFromPartialClassId(
			final String partialClassId) 
			throws ServiceException {
		
		try {
			return classQueries.getClassIdsFromPartialId(partialClassId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves all of the valid class IDs that contain the parameterized
	 * partial class name.
	 * 
	 * @param partialClassName The partial class name.
	 * 
	 * @return The list of matching class IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getClassIdsFromPartialClassName(
			final String partialClassName) 
			throws ServiceException {
		
		try {
			return classQueries.getClassIdsFromPartialName(partialClassName);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves all of the valid class IDs that contain the parameterized
	 * partial class description.
	 * 
	 * @param partialClassDescription The partial class description.
	 * 
	 * @return The list of matching class IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getClassIdsFromPartialClassDescription(
			final String partialClassDescription) 
			throws ServiceException {
		
		try {
			return classQueries.getClassIdsFromPartialDescription(partialClassDescription);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Searches for all class IDs that match the given parameters. The 
	 * base-case is all class IDs. For each parameter, the result is trimmed to
	 * only those classes that match the parameter.<br />
	 * <br />
	 * For example, if all classes in the system were:
	 * <li>urn:class:one, Class One Name, Class one description.</li>
	 * <li>urn:class:one, Class Two Name, Class two description.</li>
	 * If this call had no parameters, then both URN IDs would be returned. If
	 * the ID parameter was "urn:" and the name parameter was "One", then only
	 * the first URN ID, "urn:class:one" would be returned. If the ID parameter
	 * was "urn:" and the name parameter was "one", then an empty set would be
	 * returned because the "o" in "one" was not capitalized. Finally, if only
	 * the name parameter was given and it was "One", then exactly one result
	 * would be returned, "urn:class:one".
	 *  
	 * @param partialClassId A partial class ID to compare with all other class
	 * 						 IDs and limit the results to only those that 
	 * 						 contain this value.
	 * 
	 * @param partialClassName A partial class name to compare with all other
	 * 						   class names and limit the results to only those
	 * 						   that contain this value.
	 * 
	 * @param partialClassDescription A partial class description to compare
	 * 								  with all other class descriptions and 
	 * 								  limit the results to only those that
	 * 								  contain this value.
	 * 
	 * @return A, possibly empty but never null, set of class IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Set<String> classIdSearch(
			final String partialClassId,
			final String partialClassName,
			final String partialClassDescription)
			throws ServiceException {
		
		try {
			Set<String> result = null;
			
			if(partialClassId != null) {
				result = new HashSet<String>(
						classQueries.getClassIdsFromPartialId(partialClassId));
			}
			
			if(partialClassName != null) {
				List<String> classIds = 
					classQueries.getClassIdsFromPartialName(partialClassName);
				
				if(result == null) {
					result = new HashSet<String>(classIds);
				}
				else {
					result.retainAll(classIds);
				}
			}
			
			if(partialClassDescription != null) {
				List<String> classIds =
					classQueries.getClassIdsFromPartialDescription(partialClassDescription);
				
				if(result == null) {
					result = new HashSet<String>(classIds);
				}
				else {
					result.retainAll(classIds);
				}
			}
			
			// If all of the parameters were null.
			if(result == null) {
				result = new HashSet<String>(classQueries.getAllClassIds());
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the information about all of the classes in the class ID list.
	 * 
	 * @param classIds A List of class identifiers to use to aggregate the
	 * 				   information.
	 * 
	 * @return Returns a List of ClassInformation objects that contain the
	 * 		   information about the class. This may be an empty list, but it
	 * 		   will never be null.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<Clazz> getClassesInformation(
			final Collection<String> classIds) 
			throws ServiceException {
		
		try {
			return classQueries.getClassesInformation(classIds);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Generates a Map of class IDs to a List of users and their roles for a 
	 * List of classes.
	 * 
	 * @param classIds A List of unique identifiers for the classes that should
	 * 				   be added to the roster.
	 * 
	 * @return A Map of class IDs to a List of users and their roles in that
	 * 		   class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Map<String, List<UserAndClassRole>> generateClassRoster(
			final Collection<String> classIds) throws ServiceException {
		
		try {
			Map<String, List<UserAndClassRole>> result = new HashMap<String, List<UserAndClassRole>>();
			
			for(String classId : classIds) {
				result.put(classId, classQueries.getUserRolePairs(classId));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Updates the class.
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
	public void updateClass(final String classId, 
			final String className, final String classDescription, 
			final Map<String, Clazz.Role> usersToAdd, 
			final Collection<String> usersToRemove) throws ServiceException {
		
		try {
			classQueries.updateClass(classId, className, classDescription, usersToAdd, usersToRemove);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates the class via a class roster.
	 * 
	 * @param roster A Map of class IDs to Maps of usernames to class roles.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> updateClassViaRoster(
			final Map<String, Map<String, Clazz.Role>> roster) 
			throws ServiceException {
		
		try {
			List<String> warningMessages = new ArrayList<String>();
			
			for(String classId : roster.keySet()) {
				warningMessages.addAll(classQueries.updateClass(classId, null, null, roster.get(classId), null));
			}
			
			return warningMessages;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes the class.
	 * 
	 * @param classId The unique identifier or the class to be deleted.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void deleteClass(final String classId) 
			throws ServiceException {
		
		try {
			classQueries.deleteClass(classId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}