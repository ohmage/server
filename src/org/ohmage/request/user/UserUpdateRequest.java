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