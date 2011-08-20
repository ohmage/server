package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.UserInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;

/**
 * <p>Gathers information about a user including their campaign creation 
 * privilege, the campaigns and classes with which they are associated, and the
 * union of their roles in all of those campaigns and classes.</p>
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
 * </table>
 * 
 * @author John Jenkins
 */
public class UserInfoReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserInfoReadRequest.class);
	
	private UserInformation result;
	
	/**
	 * Creates a new user info read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public UserInfoReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a user info read request.");
		
		result = null;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user info read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Gathering the information about the requesting user.");
			result = UserServices.gatherUserInformation(this, getUser().getUsername());
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user with either a success message and the information
	 * about the requesting user or a failure message with an explanation.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the user info read request.");
		
		JSONObject jsonResult = new JSONObject();
		
		if(result != null) {
			try {
				jsonResult.put(getUser().getUsername(), result.toJsonObject());
			}
			catch(JSONException e) {
				LOGGER.error("There was an error building the JSONObject result.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, jsonResult);
	}
}