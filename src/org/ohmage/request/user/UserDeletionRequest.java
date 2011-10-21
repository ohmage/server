package org.ohmage.request.user;

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

/**
 * <p>Deletes a list of users.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USER_LIST}</td>
 *     <td>A list of usernames of users to delete. The usernames should be 
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserDeletionRequest.class);
	
	private final Collection<String> usernames;
	
	/**
	 * Creates a user deletion request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 */
	public UserDeletionRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a user deletion request.");
		
		Set<String> tUsernames = null;
		
		try {
			tUsernames = UserValidators.validateUsernames(httpRequest.getParameter(InputKeys.USER_LIST));
			if(tUsernames == null) {
				setFailed(ErrorCode.USER_INVALID_USERNAME, "The list of usernames must contain at least one username.");
				throw new ValidationException("The list of usernames must contain at least one username.");
			}
			else if(httpRequest.getParameterValues(InputKeys.USER_LIST).length > 1) {
				setFailed(ErrorCode.USER_INVALID_USERNAME, "Multiple username parameters were given.");
				throw new ValidationException("Multiple username parameters were given.");
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			LOGGER.info(e.toString());
		}
		
		usernames = tUsernames;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user deletion request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is an admin.");
			UserServices.verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Verifying that the users in the list exist.");
			UserServices.verifyUsersExist(usernames, true);
			
			LOGGER.info("Deleteing the user(s).");
			UserServices.deleteUser(usernames);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with a success or failure message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the user deletion request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}