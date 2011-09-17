package org.ohmage.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.exception.ValidationException;
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
	
	private static final int MAX_NAME_LENGTH = 255;
	
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
		if(StringUtils.isValidUrn(classId.trim())) {
			return classId.trim();
		}
		// If the class ID is not null, not whitespace only, and not a valid
		// URN, set the request as failed and throw a ValidationException to
		// warn the caller.
		else {
			request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class identifier is invalid: " + classId);
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
	 * 		   or null if the class ID list String is null, whitespace only, or
	 * 		   would otherwise be empty because it doesn't contain any 
	 * 		   meaningful data.
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
		Set<String> classIdList = new HashSet<String>();
		
		// Otherwise, attempt to parse the class list and evaluate each of the
		// class IDs.
		String[] classListArray = classIdListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classListArray.length; i++) {
			// Validate the current class ID.
			String currClassId = validateClassId(request, classListArray[i].trim());
			
			// If it returned null, then the current class ID in the array
			// was probably whitespace only because the class list had two
			// list item separators in a row.
			if(currClassId != null) {
				classIdList.add(currClassId);
			}
		}
		
		if(classIdList.size() == 0) {
			return null;
		}
		else {
			return new ArrayList<String>(classIdList);
		}
	}
	
	/**
	 * Validates that a class name is a valid class name by ensuring that it is
	 * not profane and not too long.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param name The name to validate.
	 * 
	 * @return Returns null if the name is null or whitespace only; otherwise,
	 * 		   the name is returned.
	 * 
	 * @throws ValidationException Thrown if the name is profane or too long.
	 */
	public static String validateName(Request request, String name) throws ValidationException {
		LOGGER.info("Validating a class name.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			return null;
		}
		
		if(StringUtils.isProfane(name.trim())) {
			request.setFailed(ErrorCodes.CLASS_INVALID_NAME, "The class name contains profanity: " + name);
			throw new ValidationException("The class name contains profanity: " + name);
		}
		else if(! StringUtils.lengthWithinLimits(name.trim(), 0, MAX_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.CLASS_INVALID_NAME, "The class name is too long. The maximum length of the class name is " + MAX_NAME_LENGTH + " characters");
			throw new ValidationException("The class name is too long. The maximum length of the class name is " + MAX_NAME_LENGTH + " characters");
		}
		else {
			return name.trim();
		}
	}
	
	/**
	 * Validates that a class description is a valid class description by 
	 * ensuring that it doesn't contain profanity.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param description The description to be validated.
	 * 
	 * @return Returns null if the description is null or whitespace only;
	 * 		   otherwise, it returns the description.
	 * 
	 * @throws ValidationException Thrown if the description contains 
	 * 							   profanity.
	 */
	public static String validateDescription(Request request, String description) throws ValidationException {
		LOGGER.info("Validating a class description.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(description)) {
			return null;
		}
		
		if(StringUtils.isProfane(description.trim())) {
			request.setFailed(ErrorCodes.CLASS_INVALID_DESCRIPTION, "The class description contains profanity: " + description);
			throw new ValidationException("The class description contains profanity: " + description);
		}
		else {
			return description.trim();
		}
	}
	
	/**
	 * Validates that the given class role exists. If it is null or whitespace
	 * only, null is returned. If it is not a valid role, a ValidationException
	 * is thrown. If it is a valid role, it is returned.
	 * 
	 * @param request The request that is attempting to have the class role
	 * 				  validated.
	 * 
	 * @param role The class role to validate.
	 * 
	 * @return Returns null if the class role is null or whitespace only; 
	 * 		   otherwise, the class role is returned.
	 * 
	 * @throws ValidationException Thrown if the class role is not a valid class
	 * 							  role.
	 */
	public static ClassRoleCache.Role validateClassRole(Request request, String role) throws ValidationException {
		LOGGER.info("Validating a class role.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			return null;
		}
		
		try {
			return ClassRoleCache.Role.getValue(role.trim());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CLASS_INVALID_ROLE, "Unknown class role: " + role);
			throw new ValidationException("Unkown class role: " + role, e);
		}
	}
	
	/**
	 * Validates that a byte array is a valid class roster. If it is null or 
	 * has no length, null is returned. Otherwise, a Map of class IDs to Maps
	 * of usernames to class roles is returned. If the roster is not a valid 
	 * roster a ValidationException is thrown and the request is failed with 
	 * the error code, {@value ErrorCodes.CLASS_INVALID_ROSTER}.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param roster The class roster as a byte array. This should a series of
	 * 				 newline-deliminated rows where each row is a comma-
	 * 				 separated list of class ID, username, and the user's role
	 * 				 in the class. Excel for any OS, and many other Microsoft
	 * 				 products, deliminate lines with a carriage return instead 
	 * 				 of a newline; this is taken care of. 
	 * 
	 * @return Returns null if the roster is null or has a length of zero;
	 * 		   otherwise, it returns a Map of class IDs to Maps of usernames to
	 * 		   class roles.
	 * 
	 * @throws ValidationException Thrown if the roster is not a valid roster.
	 */
	public static Map<String, Map<String, ClassRoleCache.Role>> validateClassRoster(Request request, byte[] roster) throws ValidationException {
		LOGGER.info("Validating a class roster.");
		
		if((roster == null) || (roster.length == 0)) {
			return null;
		}
		
		String rosterString = new String(roster);
		
		// Excel (and most of Microsoft) saves newlines as carriage returns 
		// instead of newlines, so we substitute those here as we only deal 
		// with newlines.
		rosterString = rosterString.replace('\r', '\n');
		
		Map<String, Map<String, ClassRoleCache.Role>> result = new HashMap<String, Map<String, ClassRoleCache.Role>>();
		
		String[] rosterLines = rosterString.split("\n");
		for(int i = 0; i < rosterLines.length; i++) {
			if(StringUtils.isEmptyOrWhitespaceOnly(rosterLines[i])) {
				continue;
			}
			
			String[] rosterLine = rosterLines[i].split(",");
			
			if(rosterLine.length != 3) {
				request.setFailed(ErrorCodes.CLASS_INVALID_ROSTER, "The following line is malformed in the class roster.");
				throw new ValidationException("The following line is malformed in the class roster.");
			}
			
			String classId = ClassValidators.validateClassId(request, rosterLine[0]);
			String username = UserValidators.validateUsername(request, rosterLine[1]);
			ClassRoleCache.Role classRole = ClassValidators.validateClassRole(request, rosterLine[2]);
			
			Map<String, ClassRoleCache.Role> userRoleMap = result.get(classId);
			if(userRoleMap == null) {
				userRoleMap = new HashMap<String, ClassRoleCache.Role>();
				result.put(classId, userRoleMap);
			}
			
			ClassRoleCache.Role originalRole = userRoleMap.put(username, classRole);
			// Add the role but keep track of whether or not a role already 
			// existed for this user in this class. It is an error only if the
			// two roles do not match.
			if((originalRole != null) && (! originalRole.equals(classRole))) {
				request.setFailed(ErrorCodes.CLASS_INVALID_ROSTER, "Two different roles were found for the same user in the same class. The user was '" + 
						username + "' and the class was '" + classId + "'. The first role was '" + originalRole + "' and the second role was '" + classRole + "'");
				throw new ValidationException("Two different roles were found for the same user in the same class. The user was '" + 
						username + "' and the class was '" + classId + "'. The first role was '" + originalRole + "' and the second role was '" + classRole + "'");
			}
		}
		
		return result;
	}
}