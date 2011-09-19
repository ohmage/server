package org.ohmage.request.campaign;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.UserCampaignValidators;

/**
 * <p>A request to update the information about a campaign. To update a 
 * campaign's information the requesting user must be a supervisor or an 
 * author. Furthermore, to update a campaign's XML, there cannot be any 
 * responses.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The unique identifier for the campaign to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#XML}</td>
 *     <td>The XML file describing this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RUNNING_STATE}</td>
 *     <td>The initial running state of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The initial privacy state of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes with which this campaign will initially be 
 *       associated.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_ADD}</td>
 *     <td>A list of users to be directly associated with the campaign. The 
 *       user-role pairs should separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and in each
 *       pair the username should be separated from the campaign role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_REMOVE}</td>
 *     <td>A list of users whose association with the campaign should be  
 *       removed. The user-role pairs should separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and in each
 *       pair the username should be separated from the campaign role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignUpdateRequest.class);
	
	private final String campaignId;
	private final String xml;
	private final String description;
	private final CampaignRunningStateCache.RunningState runningState;
	private final CampaignPrivacyStateCache.PrivacyState privacyState;
	private final List<String> classIds;
	private final Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToAdd;
	private final Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToRemove;
	
	/**
	 * Creates a campaign update request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters to build
	 * 					  this request.
	 */
	public CampaignUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a campaign update request.");
		
		String tCampaignId = null;
		String tXml = null;
		String tDescription = null;
		CampaignRunningStateCache.RunningState tRunningState = null;
		CampaignPrivacyStateCache.PrivacyState tPrivacyState = null;
		List<String> tClassIds = null;
		Map<String, Set<CampaignRoleCache.Role>> tUsersAndRolesToAdd = null;
		Map<String, Set<CampaignRoleCache.Role>> tUsersAndRolesToRemove = null;
		
		try {
			tCampaignId = CampaignValidators.validateCampaignId(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
			if(tCampaignId == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign ID is required: " + InputKeys.CAMPAIGN_URN);
				throw new ValidationException("The campaign ID is required: " + InputKeys.CAMPAIGN_URN);
			}
			else if(httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN).length > 1) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign IDs were found.");
				throw new ValidationException("Multiple campaign IDs were found.");
			}
			
			byte[] pXml = getMultipartValue(httpRequest, InputKeys.XML);
			if(pXml != null) {
				tXml = CampaignValidators.validateXml(this, new String(pXml));
			}
			
			tDescription = CampaignValidators.validateDescription(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
			if((tDescription != null) && (httpRequest.getParameterValues(InputKeys.DESCRIPTION).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_DESCRIPTION, "Multiple descriptions were found.");
				throw new ValidationException("Multiple descriptions were found.");
			}
			
			tRunningState = CampaignValidators.validateRunningState(this, httpRequest.getParameter(InputKeys.RUNNING_STATE));
			if((tRunningState != null) && (httpRequest.getParameterValues(InputKeys.RUNNING_STATE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_RUNNING_STATE, "Multiple running states were found.");
				throw new ValidationException("Multiple running states were found.");
			}
			
			tPrivacyState = CampaignValidators.validatePrivacyState(this, httpRequest.getParameter(InputKeys.PRIVACY_STATE));
			if((tPrivacyState != null) && (httpRequest.getParameterValues(InputKeys.PRIVACY_STATE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_PRIVACY_STATE, "Multiple privacy states were found.");
				throw new ValidationException("Multiple privacy states were found.");
			}

			// TODO: We cannot allow a campaign to not have any classes  
			// associated with it. Because this will return null if the class
			// list doesn't contain any meaningful class IDs and a null value
			// for this parameter means that the class associations for this
			// campaign will not be modified, we are safe. However, if we 
			// decide to split this into two parameters, we will want to add a
			// check to ensure that we are not disassociating all classes 
			// without associating any new ones.
			tClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID lists were found.");
				throw new ValidationException("Multiple class ID lists were found.");
			}
			
			tUsersAndRolesToAdd = UserCampaignValidators.validateUserAndCampaignRole(this, httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD));
			if((tUsersAndRolesToAdd != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_ADD).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "Multiple username, campaign role add lists were found.");
				throw new ValidationException("Multiple username, campaign role lists were found.");
			}
			
			tUsersAndRolesToRemove = UserCampaignValidators.validateUserAndCampaignRole(this, httpRequest.getParameter(InputKeys.USER_ROLE_LIST_REMOVE));
			if((tUsersAndRolesToRemove != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_REMOVE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "Multiple username, campaign role remove lists were found.");
				throw new ValidationException("Multiple username, campaign role remove lists were found.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		campaignId = tCampaignId;
		xml = tXml;
		description = tDescription;
		runningState = tRunningState;
		privacyState = tPrivacyState;
		classIds = tClassIds;
		usersAndRolesToAdd = tUsersAndRolesToAdd;
		usersAndRolesToRemove = tUsersAndRolesToRemove;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verfiying that the campaign exists and that the user belongs.");
			UserCampaignServices.campaignExistsAndUserBelongs(this, campaignId, getUser().getUsername());
			
			LOGGER.info("Verifying that the user is allowed to update the campaign.");
			UserCampaignServices.verifyUserCanUpdateCampaign(this, getUser().getUsername(), campaignId);
			
			if(xml != null) {
				LOGGER.info("Verifying that the user is allowed to update the campaign.");
				UserCampaignServices.verifyUserCanUpdateCampaignXml(this, getUser().getUsername(), campaignId);
				
				LOGGER.info("Verifying that the ID and name for the XML haven't changed.");
				CampaignServices.verifyTheNewXmlIdAndNameAreTheSameAsTheCurrentIdAndName(this, campaignId, xml);
			}
			
			if(classIds != null) {
				LOGGER.info("Verifying that all of the classes exist and that the user belongs.");
				UserClassServices.classesExistAndUserBelongs(this, classIds, getUser().getUsername());
			}
			
			if(usersAndRolesToAdd != null) {
				LOGGER.info("Verifying that the user is allowed to give the permissions they are trying to give.");
				Set<CampaignRoleCache.Role> roles = new HashSet<CampaignRoleCache.Role>();
				for(Set<CampaignRoleCache.Role> currRoles : usersAndRolesToAdd.values()) {
					roles.addAll(currRoles);
				}
				UserCampaignServices.verifyUserCanGrantOrRevokeRoles(this, getUser().getUsername(), campaignId, roles);
			}
			
			if(usersAndRolesToRemove != null) {
				LOGGER.info("Verifying that the user is allowed to revoke permissions that they are trying to revoke access.");
				Set<CampaignRoleCache.Role> roles = new HashSet<CampaignRoleCache.Role>();
				for(Set<CampaignRoleCache.Role> currRoles : usersAndRolesToRemove.values()) {
					roles.addAll(currRoles);
				}
				UserCampaignServices.verifyUserCanGrantOrRevokeRoles(this, getUser().getUsername(), campaignId, roles);
			}
			
			LOGGER.info("Updating the campaign.");
			CampaignServices.updateCampaign(this, campaignId, xml, description, runningState, privacyState, classIds, usersAndRolesToAdd, usersAndRolesToRemove);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success or a failure message and text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}