package org.ohmage.request.clazz;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.ClassInformation;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserClassServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.ValidationException;

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
	
	private final List<String> classIds;
	private final JSONObject result;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the 
	 * 					  parameters to and metadata for this request.
	 */
	public ClassReadRequest(HttpServletRequest httpRequest) {
		super(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN), httpRequest.getParameter(InputKeys.CLIENT));
		
		List<String> tempClassIds = null;
		
		if(! failed) {
			LOGGER.info("Creating a new class read request.");
			
			try {
				tempClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
				if(tempClassIds == null) {
					setFailed(ErrorCodes.CLASS_INVALID_ID, "Missing required class URN list.");
					throw new ValidationException("Missing required class URN list.");
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		classIds = tempClassIds;
		result = new JSONObject();
	}

	/**
	 * Validates that the classes in the list exist, then aggregates the
	 * requested information.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class read request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			// Check that each of the classes in the list exist and that the 
			// requester is a member of each class.
			LOGGER.info("Checking that all of the classes in the class list exist.");
			UserClassServices.classesExistAndUserBelongs(this, classIds, user.getUsername());
			
			// Get the information about the classes.
			LOGGER.info("Gathering the information about the classes in the list.");
			List<ClassInformation> informationAboutClasses = ClassServices.getClassesInformation(this, classIds, user.getUsername());
			
			// Populate our result JSONObject with class information.
			LOGGER.info("Creating the result JSONObject with the information about the classes.");
			try {
				for(ClassInformation classInformation : informationAboutClasses) {
					result.put(classInformation.getUrn(), classInformation.toJson(false));
				}
			}
			catch(JSONException e) {
				LOGGER.error("Error adding a class' information to the result object.", e);
				setFailed();
				return;
			}
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Writes the response to the HTTP response object.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the result to the user.");
		
		respond(httpRequest, httpResponse, result);
	}
}