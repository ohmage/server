package org.ohmage.validator;

import java.util.List;

import org.ohmage.domain.User;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.Request;

/**
 * Class to contain the validators for user-campaign related validation
 * 
 * @author Joshua Selsky
 */
public final class UserCampaignValidators {
	// private static final Logger LOGGER = Logger.getLogger(UserCampaignValidators.class);
	
	/**
	 * Default constructor. Made private to prevent instantiation.
	 */
	private UserCampaignValidators() {}
	
	/**
	 * For the given campaign and list of allowed roles, determines if the 
	 * given User has one of those roles in their campaigns. 
	 * 
	 * @param request The request to fail if the User does not have one of the
	 * allowed roles in the campaign.
	 * @param user The User to check.
	 * @param campaignId The id of the campaign for the User.
	 * @param allowedRoles The allowed roles for some particular operation.
	 * @throws ServiceException If the User object contains no CampaignsAndRoles, 
	 * if the User does not belong to the campaign represented by the campaignId,
	 * or if the User does not have one of the allowedRoles in the campaign
	 * represented by the campaignId.
	 */
	public static void verifyAllowedUserRoleInCampaign(Request request, User user, String campaignId, List<String> allowedRoles)
		throws ValidationException {
		
		if(user.getCampaignsAndRoles() == null) { // logical error
			request.setFailed();
			throw new ValidationException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().containsKey(campaignId)) {
			// request.setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_CAMPAIGN_ID, "User does not belong to campaign.");
			throw new ValidationException("The User in the Request does not belong to the campaign " + campaignId);
		}
		
		List<String> roleList = user.getCampaignsAndRoles().get(campaignId).getUserRoleStrings();
		for(String role : roleList) {
			if(allowedRoles.contains(role)) {
				return;
			}
		}
		
		// TODO this is the wrong place to be setting the error message
		//request.setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_USER_ROLE, "User does not have a correct role to perform" +
		//	" the operation.");
		throw new ValidationException("User does not have a correct role to perform the operation.");
	}	
	
	/**
	 * Ensures that the user in the UserRequest belongs to the campaign
	 * represented by the campaignId.
	 *  
	 * @param request The request that is performing this service.
	 * 
	 * @param campaignId The campaign ID for the campaign in question.
	 * 
	 * @throws ServiceException Thrown if the campaign doesn't exist or the user
	 * 							doesn't belong to the campaign, or if there is
	 * 							an error.
	 */
	public static void campaignExistsAndUserBelongs(Request request, User user, String campaignId) throws ValidationException {
		if(user.getCampaignsAndRoles() == null) {
			request.setFailed();
			throw new ValidationException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().keySet().contains(campaignId)) {
		//	request.setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_CAMPAIGN_ID, "User does not belong to campaign.");
		//	throw new ValidationException("The user does not belong to the campaign: " + campaignId);
		}
	}
}