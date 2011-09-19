package org.ohmage.request.clazz;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Updates classes' enrollment with the class roster. If a user has a 
 * different role in the roster than their present role, their role will be
 * updated to reflect the role in the roster and the change will be noted in
 * the returned warning messages. Users that are associated with a class but 
 * are not in the roster, will not be removed from the class. The requesting
 * user must be an admin to perform this task.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ROSTER}</td>
 *     <td>A file attachment that is a CSV file. This should consist of lines
 *     terminated by newline characters. In Excel, and most Microsoft products,
 *     carriage returns are used instead of newlines. Due to the likelihood of
 *     Excel being used to generated and/or modify the class roster, this is
 *     acceptable as well.
 *     <br />Each line should consist of three, comma-separated values. The 
 *     first value should be a unique class identifier. The second value should
 *     be the username of an existing user. The third value should be a valid 
 *     class role. Each line specifies a single user's role in a single class.
 *     If multiple rows share the same class ID and username, the last row is
 *     the one that will take effect and there will be a warning message for
 *     each time the user's role in that class was changed.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassRosterUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassRosterUpdateRequest.class);
	
	private static final String KEY_WARNING_MESSAGES = "warning_messages";
	
	private final Map<String, Map<String, ClassRoleCache.Role>> roster;
	
	private List<String> warningMessages;
	
	/**
	 * Creates a new class roster update request.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the parameters for
	 * 					  this request. 
	 */
	public ClassRosterUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Create a class roster update request.");
		
		Map<String, Map<String, ClassRoleCache.Role>> tRoster = null;
		
		try {
			tRoster = ClassValidators.validateClassRoster(this, getMultipartValue(httpRequest, InputKeys.ROSTER));
			if(tRoster == null) {
				setFailed(ErrorCodes.CLASS_INVALID_ROSTER, "The class roster is missing.");
				throw new ValidationException("The class roster is missing.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		roster = tRoster;
		warningMessages = new LinkedList<String>();
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the class roster update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			Set<String> classIds = roster.keySet();
			
			LOGGER.info("Verifying that the classes in the class roster exist.");
			ClassServices.checkClassesExistence(this, classIds, true);
			
			LOGGER.info("Verifying that the requester is an admin.");
			UserServices.verifyUserIsAdmin(this, getUser().getUsername());
			
			LOGGER.info("Verifying that the users in the roster exist.");
			Set<String> uniqueUsers = new HashSet<String>();
			for(String classId : classIds) {
				uniqueUsers.addAll(roster.get(classId).keySet());
			}
			UserServices.verifyUsersExist(this, uniqueUsers, true);
			
			LOGGER.info("Updating the classes via the roster.");
			warningMessages = ClassServices.updateClassViaRoster(this, roster);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user's request with either success and a List of warning
	 * messages or failure and a failure message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, KEY_WARNING_MESSAGES, new JSONArray(warningMessages));
	}
}