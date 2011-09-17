package org.ohmage.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class for validating username and campaign pair values.
 * 
 * @author John Jenkins
 */
public class UserCampaignValidators {
	private static final Logger LOGGER = Logger.getLogger(UserCampaignValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserCampaignValidators() {}
	
	/**
	 * Validates a String that should be a list of username, campaign role 
	 * pairs. Because a user can have more than one role in a campaign, the
	 * result is a map of usernames to a set of campaign roles. It is a set 
	 * instead of a list to prevent duplicate roles for the same user.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param userAndCampaignRoleList A String representing a list of username
	 * 								  and campaign role pairs. The pairs should
	 * 								  be separated by 
	 * 								  {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s
	 * 								  and the username and campaign role should
	 * 								  be separated by
	 * 								  {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}s.
	 * 
	 * @return A Map of usernames to a list of the campaign roles associated 
	 * 		   with them from the string list or null if the string is null,
	 * 		   whitespace only, or contains only separators and no meaningful
	 * 		   information.
	 * 
	 * @throws ValidationException Thrown if the list is invalid, any of the
	 * 							   pairs are invalid, or either the username or
	 * 							   class role are invalid.
	 */
	public static Map<String, Set<CampaignRoleCache.Role>> validateUserAndCampaignRole(Request request, String userAndCampaignRoleList) throws ValidationException {
		LOGGER.info("Validating a list of user and class role pairs.");
		
		// If it's null or empty, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(userAndCampaignRoleList)) {
			return null;
		}
		
		// Create the resulting object which will initially be empty.
		Map<String, Set<CampaignRoleCache.Role>> result = new HashMap<String, Set<CampaignRoleCache.Role>>();
		// Split the parameterized value into its pairs.
		String[] userAndRoleArray = userAndCampaignRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		// For each of these pairs,
		for(int i = 0; i < userAndRoleArray.length; i++) {
			String currUserAndRole = userAndRoleArray[i].trim();
			
			// If the pair is empty, i.e. there were two list item separators
			// in a row, then skip it.
			if((! StringUtils.isEmptyOrWhitespaceOnly(currUserAndRole)) && 
					(! currUserAndRole.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] userAndRole = currUserAndRole.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				// If the pair isn't actually a pair, fail with an invalid 
				// campaign role error.
				if(userAndRole.length != 2) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "The username, campaign role pair is invalid: " + currUserAndRole);
					throw new ValidationException("The user campaign-role list at index " + i + " is invalid: " + currUserAndRole);
				}
				
				// Validate the actual elements in the pair.
				String username = UserValidators.validateUsername(request, userAndRole[0].trim());
				if(username == null) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username in the username, campaign role pair is missing: " + currUserAndRole);
					throw new ValidationException("The username in the username, campaign role pair is missing: " + currUserAndRole);
				}
				
				CampaignRoleCache.Role role = CampaignValidators.validateRole(request, userAndRole[1].trim());
				if(role == null) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "The campaign role in the username, campaign role pair is missing: " + currUserAndRole);
					throw new ValidationException("The campaign role in the username, campaign role pair is missing: " + currUserAndRole);
				}
				
				// Add the role to the list of roles.
				Set<CampaignRoleCache.Role> roles = result.get(username);
				if(roles == null) {
					roles = new HashSet<CampaignRoleCache.Role>();
					result.put(username, roles);
				}
				roles.add(role);
			}
		}
		
		// If the list is empty, return null.
		if(result.size() == 0) {
			return null;
		}
		else {
			return result;
		}
	}
}
