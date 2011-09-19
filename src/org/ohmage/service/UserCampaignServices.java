package org.ohmage.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.dao.CampaignClassDaos;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.CampaignSurveyResponseDaos;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserDaos;
import org.ohmage.domain.Campaign;
import org.ohmage.domain.CampaignInformation;
import org.ohmage.domain.User;
import org.ohmage.domain.UserPersonal;
import org.ohmage.domain.UserRoleCampaignInfo;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains the services for user-campaign relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserCampaignServices {
	private static final Logger LOGGER = Logger.getLogger(UserCampaignServices.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserCampaignServices() {}
	
	/**
	 * Ensures that a campaign exists and that a user belongs to the campaign
	 * in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param campaignId The campaign ID for the campaign in question.
	 * 
	 * @param username The username of the user that must belong to the 
	 * 				   campaign.
	 * 
	 * @throws ServiceException Thrown if the campaign doesn't exist, the user
	 * 							doesn't belong to the campaign, or if there is
	 * 							an error.
	 */
	public static void campaignExistsAndUserBelongs(Request request, String campaignId, String username) throws ServiceException {
		CampaignServices.checkCampaignExistence(request, campaignId, true);
		
		try {
			if(! UserCampaignDaos.userBelongsToCampaign(username, campaignId)) {
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user does not belong to the campaign: " + campaignId);
				throw new ServiceException("The user does not belong to the campaign: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the User belongs to the campaign represented by the
	 * campaignId.
	 *  
	 * @param request The request that is performing this service.
	 * @param user  The user to validate.
	 * @param campaignId The campaign ID for the campaign in question.
	 * 
	 * @throws ServiceException Thrown if the campaign doesn't exist or the user
	 * 							doesn't belong to the campaign.
	 */
	public static void campaignExistsAndUserBelongs(Request request, User user, String campaignId) throws ServiceException {
		if(user.getCampaignsAndRoles() == null) {
			request.setFailed();
			throw new ServiceException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().keySet().contains(campaignId)) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "User does not belong to campaign.");
			throw new ServiceException("The user does not belong to the campaign: " + campaignId);
		}
	}

		
	/**
	 * Ensures that all of the campaigns in a List exist and that the user 
	 * belongs to each of them in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param campaignIds A List of campaign IDs that all must exist.
	 * 
	 * @param username The username of the user that must exist in all of the
	 * 				   campaigns.
	 * 
	 * @throws ServiceException Thrown if any of the campaigns don't exist, if
	 * 							the user doesn't belong to any of the 
	 * 							campaigns, or if there is an error. 
	 */
	public static void campaignsExistAndUserBelongs(Request request, List<String> campaignIds, String username) throws ServiceException {
		for(String campaignId : campaignIds) {
			campaignExistsAndUserBelongs(request, campaignId, username);
		}
	}
	
	/**
	 * Populates the User in the Request with campaign information and the
	 * User's associated roles for each campaign.
	 * 
	 * @param request The request to retrieve the User from.
	 * 
	 * @throws ServiceException Thrown if there is no user in the request, the
	 * user does not belong to any campaigns, or if there is an error. 
	 */
	public static void populateUserWithCampaignRoleInfo(Request request, User user) throws ServiceException {
		LOGGER.info("Populating the user in the request with campaign info and the user's roles for each campaign they belong to");
		
		try {
			List<UserRoleCampaignInfo> userRoleCampaignInfoList 
				= UserCampaignDaos.getAllCampaignRolesAndCampaignInfoForUser(user);
			
			for(UserRoleCampaignInfo info : userRoleCampaignInfoList) {
				Campaign campaign = new Campaign();
				campaign.setCampaignCreationTimestamp(info.getCampaignCreationTimestamp());
				campaign.setDescription(info.getCampaignDescription());
				campaign.setName(info.getCampaignName());
				campaign.setPrivacyState(info.getCampaignPrivacyState());
				campaign.setRunningState(info.getCampaignRunningState());
				campaign.setUrn(info.getCampaignUrn());
				user.addCampaignAndUserRole(campaign, info.getUserRole());
			}
		
		} catch (DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
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
	public static void verifyAllowedUserRoleInCampaign(Request request, User user, String campaignId, List<CampaignRoleCache.Role> allowedRoles)
		throws ServiceException {
		
		if(user.getCampaignsAndRoles() == null) { // logical error
			request.setFailed();
			throw new ServiceException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().containsKey(campaignId)) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "User does not belong to campaign.");
			throw new ServiceException("The User in the Request does not belong to the campaign " + campaignId);
		}
		
		List<CampaignRoleCache.Role> roleList = user.getCampaignsAndRoles().get(campaignId).getUserRoleStrings();
		for(CampaignRoleCache.Role role : roleList) {
			if(allowedRoles.contains(role)) {
				return;
			}
		}

		request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "User does not have a correct role to perform" +
			" the operation.");
		throw new ServiceException("User does not have a correct role to perform the operation.");
	}
	
	/**
	 * Verifies that a user is allowed to read the personal information of all
	 * of the users in a campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to view the
	 * 							personal information about all of the users in
	 * 							the class or if there is an error.
	 */
	public static void verifyUserCanReadUsersInfoInCampaign(Request request, String username, String campaignId) throws ServiceException  {
		try {
			if(! UserCampaignDaos.getUserCampaignRoles(username, campaignId).contains(CampaignRoleCache.Role.SUPERVISOR)) {
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is not allowed to read the personal information of the users in the following campaign: " + campaignId);
				throw new ServiceException("The user is not allowed to read the personal information of the users in the following campaign: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the personal information of all
	 * of the users in all of the campaigns.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignIds A Collection of unique identifiers for the campaigns.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to read the
	 * 							personal information of the users in one of the
	 * 							classes or if there is an error.
	 */
	public static void verifyUserCanReadUsersInfoInCampaigns(Request request, String username, Collection<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			verifyUserCanReadUsersInfoInCampaign(request, username, campaignId);
		}
	}
	
	/**
	 * Verifies that some user is allowed to update some campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to update 
	 * 							this campaign or if there is an error.
	 */
	public static void verifyUserCanUpdateCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(CampaignRoleCache.Role.SUPERVISOR) ||
			   roles.contains(CampaignRoleCache.Role.AUTHOR)) {
				return;
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is not allowed to update the campaign.");
			throw new ServiceException("The user is not allowed to update the campaign.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to update a campaign's XML.
	 *  
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to modify the
	 * 							campaign, if the user is allowed to modify the
	 * 							campaign but responses exist, or if there is an
	 * 							error.
	 */
	public static void verifyUserCanUpdateCampaignXml(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(CampaignRoleCache.Role.SUPERVISOR) ||
			   roles.contains(CampaignRoleCache.Role.AUTHOR)) {
				if(CampaignSurveyResponseDaos.getNumberOfSurveyResponsesForCampaign(campaignId) == 0) {
					return;
				}
				
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "Survey responses exist; therefore the XML can no longer be modified.");
				throw new ServiceException("Survey responses exist; therefore the XML can no longer be modified.");
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is not allowed to modify the campaign's XML.");
			throw new ServiceException("The user is not allowed to modify the campaign's XML.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user is allowed to grant or revoke all of the roles in
	 * the collection.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign where the user
	 * 					 is attempting to add or revoke roles.
	 * 
	 * @param roles The roles to check if the user can grant or revoke.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to grant or
	 * 							revoke some role or if there is an error.
	 */
	public static void verifyUserCanGrantOrRevokeRoles(Request request, String username, 
			String campaignId, Collection<CampaignRoleCache.Role> roles) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> usersRoles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(usersRoles.contains(CampaignRoleCache.Role.SUPERVISOR)) {
				return;
			}
			
			if(usersRoles.contains(CampaignRoleCache.Role.AUTHOR)) {
				if(! roles.contains(CampaignRoleCache.Role.SUPERVISOR)) {
					return;
				}
				
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is not allowed to grant the supervisor privilege.");
				throw new ServiceException("The user is not allowed to grant the supervisor privilege.");
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is not allowed to grant privileges.");
			throw new ServiceException("The user is not allowed to grant privileges.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the user can delete the campaign. At least one of the 
	 * following must be true to delete a campaign:
	 * <ul>
	 *   <li>The user is a supervisor.</li>
	 *   <li>The user is an author and there are no responses.</li>
	 * </ul>
	 * <br />
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The user's username.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to delete the campaign or if there
	 * 							is an error.
	 */
	public static void userCanDeleteCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(CampaignRoleCache.Role.SUPERVISOR)) {
				return;
			}
			
			if(roles.contains(CampaignRoleCache.Role.AUTHOR)) {
				long numberOfResponses = CampaignSurveyResponseDaos.getNumberOfSurveyResponsesForCampaign(campaignId);
				
				if(numberOfResponses == 0) {
					return;
				}
				else {
					request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The campaign has responses; therefore, you can no longer delete it.");
					throw new ServiceException("The campaign has responses; therefore, you can no longer delete it.");
				}
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "You do not have sufficient permissoins to delete this campaign.");
			throw new ServiceException("You do not have sufficient permissoins to delete this campaign.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of usernames for the users in a campaign. The List will
	 * be empty if there are no users in the campaign or if the campaign 
	 * doesn't exist.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for all of the users in a class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getUsersInCampaign(Request request, String campaignId) throws ServiceException {
		try {
			return UserCampaignDaos.getUsersInCampaign(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns a Set of distinct usernames for all of the users in a Collection 
	 * of campaign IDs.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignIds The unique identifier of the campaign.
	 * 
	 * @return Returns a Set of distinct usernames from all of the campaigns.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Set<String> getUsersInCampaigns(Request request, Collection<String> campaignIds) throws ServiceException {
		Set<String> usernames = new HashSet<String>();
		for(String campaignId : campaignIds) {
			usernames.addAll(getUsersInCampaign(request, campaignId));
		}
		return usernames;
	}
	
	/**
	 * Aggregates all of the personal information for all of the users in all 
	 * of the campaigns without duplicates.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignIds A Collection of unique identifiers for campaigns.
	 * 
	 * @return A Map of usernames to personal information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<String, UserPersonal> getPersonalInfoForUsersInCampaigns(Request request, Collection<String> campaignIds) throws ServiceException {
		try {
			Map<String, UserPersonal> result = new HashMap<String, UserPersonal>();
			Collection<String> usernames = getUsersInCampaigns(request, campaignIds);
			
			for(String username : usernames) {
				result.put(username, UserDaos.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that the requesting user can view survey responses for some 
	 * collection of users. There may not actually be any responses to read or
	 * the responses may need to be made public first. This only guarantees 
	 * that, if the other users have any public responses that the requesting
	 * user is allowed to view them. Therefore, this will pass as long as any 
	 * of the following are true:
	 * <br />
	 * <br />
	 * - If the user is a supervisor or an author.<br />
	 * - If the user is an analyst and the campaign is shared.<br />
	 * - If the user is the same as all of the requesting users.<br />
	 * <br />
	 * If you want to check if a user can read survey responses from every user
	 * in a campaign, don't pass in any user usernames.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param requesterUsername The requesting user's username.
	 * 
	 * @param userUsernames The array of usernames of specific users to check
	 * 						if the requesting user has permission to read their
	 * 						information.
	 *
	 * @throws ServiceException Thrown if none of the rules are true or there 
	 * 							is an error.
	 */
	public static void requesterCanViewUsersSurveyResponses(Request request, 
			String campaignId, String requesterUsername, String... userUsernames) throws ServiceException {
		try {
			// If the requester is the same as all of the users in question.
			boolean otherUsers = false;
			for(String username : userUsernames) {
				if(! requesterUsername.equals(username)) {
					otherUsers = true;
				}
			}
			if(! otherUsers) {
				return;
			}
			
			List<CampaignRoleCache.Role> requesterRoles = 
				UserCampaignDaos.getUserCampaignRoles(requesterUsername, campaignId);
			
			// If the requester's role list contains supervisor, return.
			if(requesterRoles.contains(CampaignRoleCache.Role.SUPERVISOR)) {
				return;
			}
			
			// If the requester's role list contains author, return.
			if(requesterRoles.contains(CampaignRoleCache.Role.AUTHOR)) {
				return;
			}
			
			// If the requester's role list contains analyst,
			if(requesterRoles.contains(CampaignRoleCache.Role.ANALYST)) {
				CampaignPrivacyStateCache.PrivacyState privacyState = CampaignDaos.getCampaignPrivacyState(campaignId);
				
				if((privacyState != null) && 
				   (CampaignPrivacyStateCache.PrivacyState.SHARED.equals(privacyState))) {
					return;
				}
			}
				
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user does not have sufficient permissions to read information about other users.");
			throw new ServiceException("The user does not have sufficient permissions to read information about other users.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Generates a List of unique identifiers for campaigns based on the
	 * parameters. The List is based on the 'campaignIds' parameter unless it 
	 * is null in which case the List is based on all campaigns visible to the 
	 * user. All parameters except 'request' and 'username' are optional and
	 * each will filter the resulting List of campaign identifiers.<br />
	 * <br />
	 * <br />
	 * For example, if 'campaignIds' was null as were 'endDate' and 
	 * 'privacyState', then what would be returned would be the intersection of
	 * the following lists:<br />
	 * - All of the campaigns to which the user was associated (because 
	 * 'campaignIds' was null).<br />
	 * - All of the campaigns that are associated with any of the classes whose
	 * unique identifier was in the 'classIds' list.<br />
	 * - All of the campaigns whose creation timestamp was equal to or after
	 * 'startDate'<br />
	 * - All of the campaigns whose running state equaled 'runningState'.<br />
	 * - All of the campaigns to which the user had the campaign role 'role'.
	 * <br />
	 * <br />
	 * Therefore, if a campaign was associated with a user only through a 
	 * single class, but that class wasn't in the 'classIds' list, then that
	 * campaign ID would not be returned even if all of the other parameters
	 * matched.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignIds An optional Collection of campaign identifiers from 
	 * 					  which to base the List. If this is empty, the 
	 * 					  resulting List will be empty. If this is null, the
	 * 					  base List will be all campaigns to which the user is
	 * 					  associated.
	 * 
	 * @param classIds A Collection of unique identifiers for classes where the
	 * 				   resulting list will only contain campaign identifiers
	 * 				   for campaigns that are associated with any of these
	 * 				   classes.
	 * 
	 * @param startDate A Calendar where only campaigns whose creation 
	 * 					timestamp is equal to or after this date.
	 * 
	 * @param endDate A Calendar where only campaigns whose creation timestamp
	 * 				  is equal to or before this date.
	 * 
	 * @param privacyState A campaign privacy state that trims the resulting 
	 * 					   list of campaigns to only those that have this 
	 * 					   privacy state.
	 * 					   
	 * @param runningState A campaign running state that trims the resulting
	 * 					   list of campaigns to only those that have this
	 * 					   running state.
	 * 
	 * @param role A campaign role that trims the resulting list of campaigns 
	 * 			   to only those where the user has that role in the campaign.
	 *  
	 * @return A List of campaign unique identifiers based on the 'campaignIds'
	 * 		   parameter and trimmed by the rest of the parameters.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getCampaignsForUser(Request request, 
			String username, 
			Collection<String> campaignIds, Collection<String> classIds,
			Calendar startDate, Calendar endDate, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignRoleCache.Role role) throws ServiceException, DataAccessException {
		
		Set<String> desiredCampaignIds = new HashSet<String>();
		
		if(campaignIds == null) {
			// Initializes the list with all of the campaign IDs for the 
			// requesting user.
			desiredCampaignIds.addAll(UserCampaignDaos.getCampaignIdsAndNameForUser(username).keySet());
		}
		else {
			// Initializes the list with the campaign IDs in the query.
			desiredCampaignIds.addAll(campaignIds);
		}
		
		if(classIds != null) {
			// Get all of the campaigns associated with all of the classes in
			// the list.
			for(String classId : classIds) {
				desiredCampaignIds.retainAll(CampaignClassDaos.getCampaignsAssociatedWithClass(classId));
			}
		}
		
		if(startDate != null) {
			// Get all of the campaigns whose creation timestamp is greater
			// than or equal to the start date.
			desiredCampaignIds.retainAll(CampaignDaos.getCampaignsOnOrAfterDate(startDate));
		}
		
		if(endDate != null) {
			// Get all of the campaigns whose creation timestamp is less than
			// or equal to the end date.
			desiredCampaignIds.retainAll(CampaignDaos.getCampaignsOnOrBeforeDate(endDate));
		}
		
		if(privacyState != null) {
			// Get all of the campaigns with a privacy state of 'privacyState'.
			desiredCampaignIds.retainAll(CampaignDaos.getCampaignsWithPrivacyState(privacyState));
		}
		
		if(runningState != null) {
			// Get all of the campaigns with a running state of 'runningState'.
			desiredCampaignIds.retainAll(CampaignDaos.getCampaignsWithRunningState(runningState));
		}
		
		if(role != null) {
			// Get all of the campaigns where the user's role is 'role'.
			desiredCampaignIds.retainAll(UserCampaignDaos.getCampaignIdsForUserWithRole(username, role));
		}
		
		return new ArrayList<String>(desiredCampaignIds);
	}
	
	/**
	 * Gathers the requested information about a campaign. This will be at 
	 * least its name, description (possibly null), running state, privacy 
	 * state, creation timestamp, and all of the requesting user's roles in the
	 * campaign.<br />
	 * <br />
	 * The extras include the campaign's XML, all of the users associated with 
	 * the campaign and their roles, and all of the classes associated with the
	 * campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose roles in the campaign are
	 * 				   desired.
	 * 
	 * @param campaignIds The IDs for the campaigns whose information is 
	 * 					  desired.
	 * 
	 * @param withExtras A flag to indicate if the extra information should be
	 * 					 included in each Campaign object.
	 * 
	 * @return A map of campaigns and their information to the list of roles
	 * 		   for this user in the campaign.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<CampaignInformation, List<CampaignRoleCache.Role>> getCampaignAndUserRolesForCampaigns(Request request,
			String username, Collection<String> campaignIds, boolean withExtras) throws ServiceException {
		try {
			Map<CampaignInformation, List<CampaignRoleCache.Role>> result = new HashMap<CampaignInformation, List<CampaignRoleCache.Role>>();
			
			for(String campaignId : campaignIds) {
				// Create the Campaign object with the campaign's ID.
				CampaignInformation campaign = CampaignDaos.getCampaignInformation(campaignId);
				
				// Get the user's roles.
				List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
				
				// If we are supposed to get the extra information as well.
				if(withExtras) {
					// Add the campaign's XML.
					campaign.setXml(CampaignDaos.getXml(campaignId));
					
					// Add the classes that are associated with the campaign.
					campaign.addClasses(CampaignClassDaos.getClassesAssociatedWithCampaign(campaignId));
					
					// Add the list of roles and all of the users with those
					// roles.
					List<String> campaignUsernames = UserCampaignDaos.getUsersInCampaign(campaignId);
					for(String campaignUsername : campaignUsernames) {
						List<CampaignRoleCache.Role> userRoles = UserCampaignDaos.getUserCampaignRoles(campaignUsername, campaignId);
						
						for(CampaignRoleCache.Role userRole : userRoles) {
							if(CampaignRoleCache.Role.SUPERVISOR.equals(userRole)) {
								campaign.addSupervisor(campaignUsername);
							}
							else if(CampaignRoleCache.Role.AUTHOR.equals(userRole)) {
								campaign.addAuthor(campaignUsername);
							}
							else if(CampaignRoleCache.Role.ANALYST.equals(userRole)) {
								campaign.addAnalyst(campaignUsername);
							}
							else if(CampaignRoleCache.Role.PARTICIPANT.equals(userRole)) {
								campaign.addParticipant(campaignUsername);
							}
						}
					}
				}

				// Add the user's roles.
				result.put(campaign, roles);
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that some user is allowed to read the list of users in a 
	 * campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have permissions to
	 * 							read the users of a campaign and their roles or
	 * 							if there is an error.
	 */
	public static void verifyUserCanReadUsersInCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(CampaignRoleCache.Role.SUPERVISOR) || 
					roles.contains(CampaignRoleCache.Role.AUTHOR)) {
				return;
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + campaignId);
			throw new ServiceException("The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of users in a 
	 * Collection of campaigns.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignIds The Collection of campaign unique identifiers.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to read the
	 * 							list of users in one of the campaigns or if 
	 * 							there is an error.
	 */
	public static void verifyUserCanReadUsersInCampaigns(Request request, String username, Collection<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			verifyUserCanReadUsersInCampaign(request, username, campaignId);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of classes associated 
	 * with a campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the list of classes for a
	 * 							campaign.
	 */
	public static void verifyUserCanReadClassesAssociatedWithCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(CampaignRoleCache.Role.SUPERVISOR) || 
					roles.contains(CampaignRoleCache.Role.AUTHOR)) {
				return;
			}
			
			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + campaignId);
			throw new ServiceException("The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of classes associated
	 * with a Collection of campaigns.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignIds A Collection of unique identifiers for the campaigns.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the list of classes for any
	 * 							of the campaigns.
	 */
	public static void verifyUserCanReadClassesAssociatedWithCampaigns(Request request, String username, Collection<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			verifyUserCanReadClassesAssociatedWithCampaign(request, username, campaignId);
		}
	}
	
	/**
	 * Verifies that each username in usernameList belongs to the campaign
	 * specified by the campaignId.
	 * 
	 * @param request  The request to fail when a user does not belong to a
	 * campaign or an IO problem occurs.
	 * @param campaignId  The campaign to check each user against.
	 * @param usernameList  The users in question
	 * @throws ServiceException  If any user in usernameList does not belong
	 * to the campaign or if an IO problem occurs.
	 * @throws IllegalArgumentException if the request is null; if the 
	 * campaignId is empty or null; or if the usernameList is null.
	 */
	public static void verifyUsersExistInCampaign(Request request, String campaignId, List<String> usernameList) throws ServiceException {
		// check for logical errors
		if(request == null || StringUtils.isEmptyOrWhitespaceOnly(campaignId) || usernameList == null) {
			throw new IllegalArgumentException("null request, empty campaignId, or null usernameList");
		}
		
		// check each username in usernameList
		try {
			for(String username : usernameList) {
				if(! UserCampaignDaos.userBelongsToCampaign(username, campaignId)) {
					StringBuilder sb = new StringBuilder();
					sb.append("User in usernameList does not belong to campaign. Username: ");
					sb.append(username);
					sb.append(" Campaign ID: ");
					sb.append(campaignId);
					String msg = sb.toString();
					request.setFailed(ErrorCodes.USER_NOT_IN_CAMPAIGN, msg);
					throw new ServiceException(msg);
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}