package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.service.UserServices;

/**
 * <p>Activates a user's self-reigstered account.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_REGISTRATION_ID}</td>
 *     <td>The registration ID sent to the user via the registration link in 
 *       the email sent to them when they self-registered.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserActivationRequest extends Request {
	private static final Logger LOGGER = 
			Logger.getLogger(UserActivationRequest.class);
	
	private final String registrationId;
	
	/**
	 * Creates a user activation request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required and
	 * 					  optional parameters for creating this request.
	 */
	public UserActivationRequest(final HttpServletRequest httpRequest) {
		super(httpRequest);
		
		String tRegistrationId = null;
		
		if(! isFailed()) {
			try {
				String[] t;
				
				t = getParameterValues(InputKeys.USER_REGISTRATION_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_REGISTRATION_ID,
							"Multiple registration IDs were given: " +
								InputKeys.USER_REGISTRATION_ID);
				}
				if(t.length == 1) {
					// To avoid people attempting to upload random strings, we
					// don't syntactically validate the value and only check to
					// see if it exists in the system. If not, we return a 
					// generic message indicating that it does not exist.
					tRegistrationId = t[0];
				}
				else {
					throw new ValidationException(
							ErrorCode.USER_INVALID_REGISTRATION_ID,
							"The registration ID is missing.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		registrationId = tRegistrationId;
	}
	
	/**
	 * Validates that the registration ID exists, and, if so, activates the
	 * account.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a user activation request.");
		
		try {
			LOGGER.info("Verifying that the registration ID exists.");
			UserServices.instance().validateRegistrationId(registrationId);
			
			LOGGER.info("Activating the account.");
			UserServices.instance().activateUser(registrationId);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Returns success or failure.
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {
		
		LOGGER.info("Responding to a user activation request.");
		
		// FIXME: This should redirect to a page accessed by GET to prevent 
		// the user from being able to activate their account repeatedly. 
		// While this won't cause any real issues, it may cause them to get an
		// incorrect rejection from the server only because their account has
		// already been activated.

		super.respond(httpRequest, httpResponse, new JSONObject());
	}
}