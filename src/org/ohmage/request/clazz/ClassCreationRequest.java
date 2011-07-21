package org.ohmage.request.clazz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>Creates a new class. The requester must be an admin.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN}</td>
 *     <td>The URN of the new class.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_NAME}</td>
 *     <td>The name of the new class</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of the class.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassCreationRequest.class);
	
	private final String classId;
	private final String className;
	private final String classDescription;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the
	 * 					  parameters to and metadata for this request.
	 */
	public ClassCreationRequest(HttpServletRequest httpRequest) {
		super(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a class creation request.");
		
		String tempClassId = null;
		String tempClassName = null;
		String tempClassDescription = null;
		
		if(! failed) {
			try {
				tempClassId = ClassValidators.validateClassId(this, httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed("1212", "Missing required class URN.");
					throw new ValidationException("Missing required class URN.");
				}
				
				tempClassName = ClassValidators.validateName(this, httpRequest.getParameter(InputKeys.CLASS_NAME));
				if(tempClassName == null) {
					setFailed("1213", "Missing required class name.");
					throw new ValidationException("Missing required class name.");
				}
				
				tempClassDescription = ClassValidators.validateDescription(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		classId = tempClassId;
		className = tempClassName;
		classDescription = tempClassDescription;
	}

	/**
	 * Ensures that the class doesn't already exists and, if not, creates it.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class creation request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			// Check if the user is an administrator.
			LOGGER.info("Checking that the user is an admin.");
			UserServices.verifyUserIsAdmin(this, user.getUsername());
			
			// Check that the class doesn't already exist.
			LOGGER.info("Checking that a class with the same ID doesn't already exist.");
			ClassServices.checkClassExistence(this, classId, false);
			
			// Create the class.
			LOGGER.info("Creating the class.");
			ClassServices.createClass(this, classId, className, classDescription);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with a success or failure message. T
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}