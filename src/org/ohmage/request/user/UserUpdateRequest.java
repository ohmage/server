package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

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
		super(httpRequest, TokenLocation.PARAMETER);
		
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
			else if(httpRequest.getParameterValues(InputKeys.USERNAME).length > 1) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Multiple username parameters were given.");
				throw new ValidationException("Multiple username parameters were given.");
			}
			
			tAdmin = UserValidators.validateAdminValue(this, httpRequest.getParameter(InputKeys.USER_ADMIN));
			if((tAdmin != null) && (httpRequest.getParameterValues(InputKeys.USER_ADMIN).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_ADMIN_VALUE, "Multiple admin parameters were given.");
				throw new ValidationException("Multiple admin parameters were given.");
			}
			
			tEnabled = UserValidators.validateEnabledValue(this, httpRequest.getParameter(InputKeys.USER_ENABLED));
			if((tEnabled != null) && (httpRequest.getParameterValues(InputKeys.USER_ENABLED).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_ENABLED_VALUE, "Multiple enabled parameters were given.");
				throw new ValidationException("Multiple enabled parameters were given.");
			}
			
			tNewAccount = UserValidators.validateNewAccountValue(this, httpRequest.getParameter(InputKeys.NEW_ACCOUNT));
			if((tNewAccount != null) && (httpRequest.getParameterValues(InputKeys.NEW_ACCOUNT).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_NEW_ACCOUNT_VALUE, "Multiple new account parameters were given.");
				throw new ValidationException("Multiple new account parameters were given.");
			}
			
			tCampaignCreationPrivilege = UserValidators.validateCampaignCreationPrivilegeValue(this, httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE));
			if((tCampaignCreationPrivilege != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_CREATION_PRIVILEGE).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, "Multiple campaign creation privilege parameters were given.");
				throw new ValidationException("Multiple campaign creation privilege parameters were given.");
			}
			
			tFirstName = UserValidators.validateFirstName(this, httpRequest.getParameter(InputKeys.FIRST_NAME));
			if((tFirstName != null) && (httpRequest.getParameterValues(InputKeys.FIRST_NAME).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "Multiple first name parameters were given.");
				throw new ValidationException("Multiple first name parameters were given.");
			}
			
			tLastName = UserValidators.validateLastName(this, httpRequest.getParameter(InputKeys.LAST_NAME));
			if((tLastName != null) && (httpRequest.getParameterValues(InputKeys.LAST_NAME).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "Multiple last name parameters were given.");
				throw new ValidationException("Multiple last name parameters were given.");
			}
			
			tOrganization = UserValidators.validateOrganization(this, httpRequest.getParameter(InputKeys.ORGANIZATION));
			if((tOrganization != null) && (httpRequest.getParameterValues(InputKeys.ORGANIZATION).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "Multiple organization parameters were given.");
				throw new ValidationException("Multiple organization parameters were given.");
			}
			
			tPersonalId = UserValidators.validatePersonalId(this, httpRequest.getParameter(InputKeys.PERSONAL_ID));
			if((tPersonalId != null) && (httpRequest.getParameterValues(InputKeys.PERSONAL_ID).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "Multiple personal ID parameters were given.");
				throw new ValidationException("Multiple personal ID parameters were given.");
			}
			
			tEmailAddress = UserValidators.validateEmailAddress(this, httpRequest.getParameter(InputKeys.EMAIL_ADDRESS));
			if((tEmailAddress != null) && (httpRequest.getParameterValues(InputKeys.EMAIL_ADDRESS).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_EMAIL_ADDRESS, "Multiple email address parameters were given.");
				throw new ValidationException("Multiple email address parameters were given.");
			}
			
			tJsonData = UserValidators.validateJsonData(this, httpRequest.getParameter(InputKeys.USER_JSON_DATA));
			if((tJsonData != null) && (httpRequest.getParameterValues(InputKeys.USER_JSON_DATA).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_JSON_DATA, "Multiple JSON data parameters were given.");
				throw new ValidationException("Multiple JSON data parameters were given.");
			}
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
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requesting user is an admin.");
			UserServices.verifyUserIsAdmin(this, getUser().getUsername());
			
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