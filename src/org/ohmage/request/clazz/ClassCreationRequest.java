package org.ohmage.request.clazz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.ClassValidators;

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
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a class creation request.");
		
		String tempClassId = null;
		String tempClassName = null;
		String tempClassDescription = null;
		
		if(! isFailed()) {
			try {
				tempClassId = ClassValidators.validateClassId(httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Missing the required class ID: " + 
								InputKeys.CLASS_URN
						);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN).length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Duplicate class IDs found."
						);
				}
				
				tempClassName = ClassValidators.validateName(httpRequest.getParameter(InputKeys.CLASS_NAME));
				if(tempClassName == null) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_NAME, 
							"Missing the required class name: " + 
								InputKeys.CLASS_NAME
						);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_NAME).length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_NAME, 
							"Multiple class name parameters found."
						);
				}
				
				tempClassDescription = ClassValidators.validateDescription(httpRequest.getParameter(InputKeys.DESCRIPTION));
				if((tempClassDescription != null) && (httpRequest.getParameterValues(InputKeys.DESCRIPTION).length > 1)) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_DESCRIPTION, 
							"Multiple class descriptions were found."
						);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
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
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// Check if the user is an administrator.
			LOGGER.info("Checking that the user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			// Check that the class doesn't already exist.
			LOGGER.info("Checking that a class with the same ID doesn't already exist.");
			ClassServices.instance().checkClassExistence(classId, false);
			
			// Create the class.
			LOGGER.info("Creating the class.");
			ClassServices.instance().createClass(classId, className, classDescription);
		}
		catch(ServiceException e) {
			e.failRequest(this);
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