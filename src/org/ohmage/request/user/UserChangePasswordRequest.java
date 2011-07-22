package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

/**
 * <p>Updates a user's own password. The user must give their username and
 * current password in order to call this API and can only change their own
 * password.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#NEW_PASSWORD}</td>
 *     <td>The user's new plaintext password.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserChangePasswordRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserChangePasswordRequest.class);
	
	private final String newPassword;
	
	/**
	 * Creates a password change request.
	 * 
	 * @param httpRequest The HttpServletRequest with all of the parameters for
	 * 					  this request.
	 */
	public UserChangePasswordRequest(HttpServletRequest httpRequest) {
		super(httpRequest.getParameter(InputKeys.USERNAME), httpRequest.getParameter(InputKeys.PASSWORD), true, httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a user change password request.");
		
		String tNewPassword = null;
		
		try {
			tNewPassword = UserValidators.validatePlaintextPassword(this, httpRequest.getParameter(InputKeys.NEW_PASSWORD));
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		newPassword = tNewPassword;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the change password request.");
		
		if(! authenticate(true)) {
			return;
		}
		
		try {
			LOGGER.info("Updating the user's password.");
			UserServices.updatePassword(this, user.getUsername(), newPassword);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success if the user's password was successfully changed.
	 * Otherwise, a failure message and description are returned.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the change password request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}