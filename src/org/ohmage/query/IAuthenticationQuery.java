package org.ohmage.query;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.impl.AuthenticationQuery.UserInformation;
import org.ohmage.request.UserRequest;

/**
 * 
 * @author joshua
 *
 */
public interface IAuthenticationQuery {

	/**
	 * Gathers the information about the user that is attempting to be 
	 * authenticated.
	 * 
	 * @param userRequest The request that contains the specific information
	 * 					  about the user.
	 * 
	 * @return A UserInformation object that gives specific login information
	 * 		   about the user, or null if the user isn't found or the password
	 * 		   isn't correct.
	 */
	UserInformation execute(UserRequest userRequest) throws DataAccessException;

}