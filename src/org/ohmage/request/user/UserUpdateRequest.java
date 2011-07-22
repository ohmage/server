package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.UserPersonal;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>Updates a user's information. Only an admin can update a user's 
 * information.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user to update.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ADMIN}</td>
 *     <td>Whether or not the user should be an admin.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ENABLED}</td>
 *     <td>Whether or not the user's account should be enabled.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NEW_ACCOUNT}</td>
 *     <td>Whether or not the user needs to change their password the next time
 *       the login.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_PRIVILEGE}
 *       </td>
 *     <td>Whether or not the user is allowed to create campaigns.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#FIRST_NAME}</td>
 *     <td>The first name of the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#LAST_NAME}</td>
 *     <td>The last name of the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ORGANIZATION}</td>
 *     <td>The organization to which the user belongs.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PERSONAL_ID}</td>
 *     <td>The personal identifier for the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#EMAIL_ADDRESS}</td>
 *     <td>The user's email address.</td>
 *     <td>false+</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_JSON_DATA}</td>
 *     <td>Additional data for the user as a JSONObject. Note: Uploading a new
 *       JSONObject will erase the old one; therefore, if you want to add or
 *       remove some information from the JSONObject, you should first query
 *       for the current JSONObject, update that object, and send it back 
 *       through this API.</td>
 *     <td>false+</td>
 *   </tr>
 * </table>
 * <br />
 * * If a user does not already have a personal information entry in the 
 * database, then all of these entries must be present in order to create a new
 * one.<br />
 * <br />
 * + These are not required to create a new personal information entry in the
 * database; however, if one does not exist, then adding this fields requires
 * that the ones marked with "*" must be present to create the entry.<br />
 * 
 * @author John Jenkins
 */
public class UserUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserUpdateRequest.class);
	
	private final String username;
	
	private final Boolean admin;
	private final Boolean enabled;
	private final Boolean newAccount;
	private final Boolean campaignCreationPrivilege;
	
	private final UserPersonal personalInfo;
	
	/**
	 * Creates a new user update request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters to build 
	 * 					  this request.
	 */
	public UserUpdateRequest(HttpServletRequest httpRequest) {
		super(getToken(httpRequest), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a user update request.");
		
		String tUsername = null;
		
		Boolean tAdmin = null;
		Boolean tEnabled = null;
		Boolean tNewAccount = null;
		Boolean tCampaignCreationPrivilege = null;
		
		String tFirstName = null;
		String tLastName = null;
		String tOrganization = null;
		String tPersonalId = null;
		String tEmailAddress = null;
		JSONObject tJsonData = null;
		
		try {
			tUsername = UserValidators.validateUsername(this, httpRequest.getParameter(InputKeys.USERNAME));
			if(tUsername == null) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username is missing or not a valid username.");
				throw new ValidationException("The username is missing or not a valid username.");
			}
			
			tAdmin = UserValidators.validateAdminValue(this, httpRequest.getParameter(InputKeys.USER_ADMIN));
			tEnabled = UserValidators.validateEnabledValue(this, httpRequest.getParameter(InputKeys.USER_ENABLED));
			tNewAccount = UserValidators.validateNewAccountValue(this, httpRequest.getParameter(InputKeys.NEW_ACCOUNT));
			tCampaignCreationPrivilege = UserValidators.validateCampaignCreationPrivilegeValue(this, httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE));
			
			tFirstName = UserValidators.validateFirstName(this, httpRequest.getParameter(InputKeys.FIRST_NAME));
			tLastName = UserValidators.validateLastName(this, httpRequest.getParameter(InputKeys.LAST_NAME));
			tOrganization = UserValidators.validateOrganization(this, httpRequest.getParameter(InputKeys.ORGANIZATION));
			tPersonalId = UserValidators.validatePersonalId(this, httpRequest.getParameter(InputKeys.PERSONAL_ID));
			tEmailAddress = UserValidators.validateEmailAddress(this, httpRequest.getParameter(InputKeys.EMAIL_ADDRESS));
			tJsonData = UserValidators.validateJsonData(this, httpRequest.getParameter(InputKeys.USER_JSON_DATA));
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		username = tUsername;
		
		admin = tAdmin;
		enabled = tEnabled;
		newAccount = tNewAccount;
		campaignCreationPrivilege = tCampaignCreationPrivilege;
		
		personalInfo = new UserPersonal(tFirstName, tLastName, tOrganization, tPersonalId, tEmailAddress, tJsonData);
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Validating the user update request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requesting user is an admin.");
			UserServices.verifyUserIsAdmin(this, user.getUsername());
			
			LOGGER.info("Verifying that the user to be upaded exists.");
			UserServices.checkUserExistance(this, username, true);
			
			LOGGER.info("Verify that either the user to be updated already has a personal record or that enough information was provided to create a new one.");
			UserServices.verifyUserHasOrCanCreatePersonalInfo(this, username, personalInfo);
			
			LOGGER.info("Updating the user.");
			UserServices.updateUser(this, username, admin, enabled, newAccount, campaignCreationPrivilege, personalInfo);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user's request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the user update request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}