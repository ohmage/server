package org.ohmage.validator;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class to contain all of the functionality for validating class information.
 * 
 * @author John Jenkins
 */
public final class ClassValidators {
	private static final Logger LOGGER = Logger.getLogger(ClassValidators.class);
	
	/**
	 * Default constructor. Private so that no one can instantiate it.
	 */
	private ClassValidators() {}
	
	/**
	 * Validates that the 'classId' is either null or whitespace only in which
	 * case null is returned or is a valid class identifier in which case the 
	 * class ID is returned; however, if it is not null, not whitespace only,  
	 * and not a valid class identifier, a ValidationException is thrown.
	 *  
	 * @param request The request that is attempting to have this class ID 
	 * 				  validated.
	 * 
	 * @param classId The class identifier to be validated.
	 * 
	 * @return Returns the class ID if it is not null, not whitespace, and 
	 * 		   valid. If it is null or whitespace only, null is returned.
	 * 
	 * @throws ValidationException Thrown if the class ID is not null, not
	 * 		   whitespace only, and not a valid class ID.
	 */
	public static String validateClassId(Request request, String classId) throws ValidationException {
		LOGGER.info("Validating a class ID.");
		
		// If the value is null or whitespace only, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			return null;
		}
		
		// If the value is a valid URN, meaning that it is a plausible class 
		// ID, return the class ID back to the caller.
		if(StringUtils.isValidUrn(classId)) {
			return classId;
		}
		// If the class ID is not null, not whitespace only, and not a valid
		// URN, set the request as failed and throw a ValidationException to
		// warn the caller.
		else {
			request.setFailed("1201", "The class identifier is invalid: " + classId);
			throw new ValidationException("The class identifier is invalid: " + classId);
		}
	}
	
	/**
	 * Validates that all of the class identifiers in a list String are valid 
	 * class identifiers. If the class identifier list String is null or 
	 * whitespace only, it will return null. If not, it will attempt to parse
	 * the String and evaluate each of the class identifiers in the list. If 
	 * any are invalid, it will return an error message stating which one in
	 * the list was invalid.
	 *  
	 * @param request The request that it attempting to have this class ID list
	 * 				  validated.
	 * 
	 * @param classIdListString The class list as a String where each item is
	 * 							separated by
	 * 						  	{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.
	 * 
	 * @return Returns a List of class identifiers from the 'classListString'
	 * 		   or null if the class ID list String is null or whitespace only.
	 * 		   The resulting list may not be null but may still be empty if 
	 * 		   the class ID list String contains only separators and 
	 * 		   whitespace.
	 * 
	 * @throws ValidationException Thrown if the class Id list String contains a
	 * 							  class ID that is an invalid class ID.
	 */
	public static List<String> validateClassIdList(Request request, String classIdListString) throws ValidationException {
		LOGGER.info("Validating the list of classes.");
		
		// If the class list is an empty string, then we return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(classIdListString)) {
			return null;
		}
		
		// Create the list of class IDs to be returned to the caller.
		List<String> classIdList = new LinkedList<String>();
		
		// Otherwise, attempt to parse the class list and evaluate each of the
		// class IDs.
		String[] classListArray = classIdListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classListArray.length; i++) {
			try {
				// Validate the current class ID.
				String currClassId = validateClassId(request, classListArray[i]);
				
				// If it returned null, then the current class ID in the array
				// was probably whitespace only because the class list had two
				// list item separators in a row.
				if(currClassId != null) {
					classIdList.add(currClassId);
				}
			}
			catch(ValidationException e) {
				request.setFailed("1204", "An invalid class identifier was found: " + classListArray[i]);
				throw e;
			}
		}
		
		return classIdList;
	}
	
	/**
	 * Validates that the given class role exists. If it is null or whitespace
	 * only, null is returned. If it is not a valid role, a ValidationException
	 * is thrown. If it is a valid role, it is returned.
	 * 
	 * @param request The request that is attempting to have the class role
	 * 				  validated.
	 * 
	 * @param classRole The class role to validate.
	 * 
	 * @return Returns null if the class role is null or whitespace only; 
	 * 		   otherwise, the class role is returned.
	 * 
	 * @throws ValidationException Thrown if the class role is not a valid class
	 * 							  role.
	 */
	public static String validateClassRole(Request request, String classRole) throws ValidationException {
		LOGGER.info("Validating a class role.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(classRole)) {
			return null;
		}
		
		try {
			ClassRoleCache.instance().lookup(classRole);
			
			// If the lookup doesn't throw an exception then the class role 
			// must be known.
			return classRole;
		}
		catch(CacheMissException e) {
			request.setFailed("1211", "Unknown class role: " + classRole);
			throw new ValidationException("Unkown class role: " + classRole, e);
		}
	}
}