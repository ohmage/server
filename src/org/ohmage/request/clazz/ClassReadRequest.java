package org.ohmage.request.clazz;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Request to read the information about a list of classes including the 
 * list of users in the class and their class roles.</p>
 * <p>The requester must be a member of each of the classes.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes identifiers (URNs) separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassReadRequest.class);
	private static final String JSON_KEY_USERS = "users";
	
	private final Collection<String> classIds;
	private final Map<Clazz, Map<String, Clazz.Role>> result;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the 
	 * 					  parameters to and metadata for this request.
	 */
	public ClassReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		Set<String> tempClassIds = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a new class read request.");
			
			try {
				tempClassIds = ClassValidators.validateClassIdList(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
				if(tempClassIds == null) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
					throw new ValidationException("Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists were found.");
					throw new ValidationException("Multiple class ID lists were found.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		classIds = tempClassIds;
		result = new HashMap<Clazz, Map<String, Clazz.Role>>();
	}

	/**
	 * Validates that the classes in the list exist, then aggregates the
	 * requested information.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// Check that each of the classes in the list exist and that the 
			// requester is a member of each class.
			LOGGER.info("Checking that all of the classes in the class list exist.");
			UserClassServices.instance().classesExistAndUserBelongs(classIds, getUser().getUsername());
			
			// Get the information about the classes.
			LOGGER.info("Gathering the information about the classes in the list.");
			List<Clazz> informationAboutClasses = ClassServices.instance().getClassesInformation(classIds);
			
			if(informationAboutClasses.size() > 0) {
				LOGGER.info("Classes found: " + informationAboutClasses.size());
				
				for(Clazz clazz : informationAboutClasses) {
					String classId = clazz.getId();
					
					LOGGER.info("Gathering the requesting user's role in the class.");
					boolean isPrivileged = 
						Clazz.Role.PRIVILEGED.equals(
								UserClassServices.instance().getUserRoleInClass(
										classId, 
										getUser().getUsername()
									)
							);
					
					List<String> usernames = 
						UserClassServices.instance().getUsersInClass(classId);
					
					Map<String, Clazz.Role> usernamesAndRespectiveRole =
						new HashMap<String, Clazz.Role>(usernames.size());
					
					for(String username : usernames) {
						if(isPrivileged) {
							usernamesAndRespectiveRole.put(
									username, 
									UserClassServices.instance().getUserRoleInClass(
											classId, 
											username)
										);
						}
						else {
							usernamesAndRespectiveRole.put(username, null);
						}
					}
					
					result.put(clazz, usernamesAndRespectiveRole);
				}
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Writes the response to the HTTP response object.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the result to the user.");
		
		// Populate our result JSONObject with class information.
		LOGGER.info("Creating the result JSONObject with the information about the classes.");
		JSONObject jsonResult = new JSONObject();
		try {
			for(Clazz clazz : result.keySet()) {
				// Retrieve the username to class role map.
				Map<String, Clazz.Role> userRole = result.get(clazz);
				
				// Create the JSON for the class.
				JSONObject jsonClass = clazz.toJson(false);
				
				// Generate the user to class role JSON and add it to the class
				// JSON.
				JSONObject users = new JSONObject();
				for(String username : userRole.keySet()) {
					Clazz.Role role = userRole.get(username);
					
					users.put(username, ((role == null) ? "" : role));
				}
				jsonClass.put(JSON_KEY_USERS, users);
				
				// Add the class JSON to the result JSON with an index of the
				// class' ID.
				jsonResult.put(clazz.getId(), jsonClass);
			}
		}
		catch(JSONException e) {
			LOGGER.error("Error adding a class' information to the result object.", e);
			setFailed();
		}
		
		respond(httpRequest, httpResponse, jsonResult);
	}
	
	/**
	 * Returns the list of classes from the parameters.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> auditInfo = new HashMap<String, String[]>();
		
		if(classIds != null) {
			auditInfo.put(InputKeys.CLASS_URN, classIds.toArray(new String[0]));
		}
		
		return auditInfo;
	}
}