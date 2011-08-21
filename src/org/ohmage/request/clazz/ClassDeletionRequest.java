package org.ohmage.request.clazz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.ClassValidators;

/**
 * <p>A request to delete a class. The requester must be an admin.</p>
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
 *     <td>The unique identifier for the class to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassDeletionRequest.class);
	
	private final String classId;
	
	/**
	 * Builds this request based on the information in the HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required
	 * 					  parameters.
	 */
	public ClassDeletionRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a class deletion request.");
		
		String tempClassId = null;
		
		if(! isFailed()) {
			try {
				tempClassId = ClassValidators.validateClassId(this, httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed(ErrorCodes.CLASS_INVALID_ID, "Missing the required class ID: " + InputKeys.CLASS_URN);
					throw new ValidationException("Missing the required class ID: " + InputKeys.CLASS_URN);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN).length > 1) {
					setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID parameters were found.");
					throw new ValidationException("Multiple class ID parameters were found.");
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		classId = tempClassId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class deletion request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Checking that the user is an admin.");
			UserServices.verifyUserIsAdmin(this, getUser().getUsername());
			
			LOGGER.info("Checking that the class exists.");
			ClassServices.checkClassExistence(this, classId, true);
			
			LOGGER.info("Deleting the class.");
			ClassServices.deleteClass(this, classId);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the deletion request. Returns success if it successfully 
	 * deleted the class or an error code and explanation if anything went
	 * wrong.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}