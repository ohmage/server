package org.ohmage.service;

import java.util.Map;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IOmhQueries;
import org.ohmage.util.StringUtils;

/**
 * The services for OMH queries.
 *
 * @author John Jenkins
 */
public class OmhServices {
	private static OmhServices instance;
	private IOmhQueries omhQueries;
	
	/**
	 * Privately instantiated via reflection.
	 * 
	 * @param iOmhQueries The instance of the OmhQueries.
	 * 
	 * @throws IllegalStateException This class has already been setup.
	 * 
	 * @throws IllegalArgumentException A parameter is invalid.
	 */
	private OmhServices(final IOmhQueries iOmhQueries) {
		if(instance != null) {
			throw new IllegalStateException(
				"An instance of this class already exists.");
		}
		
		if(iOmhQueries == null) {
			throw new IllegalArgumentException(
				"An instance of IOmhQueries is required.");
		}
		
		omhQueries = iOmhQueries;
		instance = this;
	}
	
	/**
	 * Returns the instance of this service.
	 * 
	 * @return The instance of this service.
	 */
	public static OmhServices instance() {
		return instance;
	}
	
	/**
	 * Retrieves all of the authentication credentials for a given domain.
	 * 
	 * @param domain The ID for the domain.
	 * 
	 * @return A map of key-value pairs that contains all of the credentials.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<String, String> getCredentials(
			final String domain) 
			throws ServiceException {
		
		try {
			return omhQueries.getCredentials(domain);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a requesting user can read data about another user.
	 * 
	 * @param requestingUser
	 *        The requesting user's username.
	 * 
	 * @param otherUser
	 *        The other user's username.
	 * 
	 * @return The username of the user whose data should be returned.
	 * 
	 * @throws ServiceException
	 *         The requesting user is not allowed ot read data about the other
	 *         user.
	 */
	public String verifyUserCanReadAboutOtherUser(
		final String requestingUser,
		final String otherUser)
		throws ServiceException {

		// First, be sure there really is an other user.
		if((otherUser != null) && (! otherUser.equals(requestingUser))) {
			// Check if the requesting user is an admin.
			try {
				UserServices.instance().verifyUserIsAdmin(requestingUser);
			}
			// If the requesting user isn't an admin,
			catch(ServiceException notAdmin) {
				// See if this is even allowed.
				boolean isPlausible;
				try {
					isPlausible = 
						StringUtils
							.decodeBoolean(
								PreferenceCache
									.instance()
									.lookup(
										PreferenceCache
											.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
				}
				catch(CacheMissException e) {
					throw new ServiceException(e);
				}
				
				// If allowed, make sure the requesting user is privileged in
				// another class to which the other user is a member.
				if(isPlausible) {
					UserClassServices
						.instance()
						.userIsPrivilegedInAnotherUserClass(
							requestingUser, 
							otherUser);
				}
				else {
					throw new ServiceException(
						ErrorCode.OMH_INSUFFICIENT_PERMISSIONS,
						"This user is not allowed to query data about the " +
							"requested user.");
				}
			}
			
			return otherUser;
		}
		else {
			return requestingUser;
		}
	}
}