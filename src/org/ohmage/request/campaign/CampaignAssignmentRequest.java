package org.ohmage.request.campaign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.request.survey.SurveyUploadRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.SurveyResponseValidators;
import org.ohmage.validator.UserValidators;

public class CampaignAssignmentRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(CampaignAssignmentRequest.class);
	
	final String username;
	final String password;
	
	final String classId;
	
	final String campaignId;
	final Set<String> surveyIds;
	final SurveyUploadRequest uploadRequest;

	/**
	 * Creates a new campaign assignment request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters
	 * 					  necessary for servicing this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public CampaignAssignmentRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		String tUsername = null;
		String tPassword = null;
		
		String tClassId = null;
		
		String tCampaignId = null;
		Set<String> tSurveyIds = null;
		SurveyUploadRequest tUploadRequest = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a campaign assignment request.");
			String[] t;
			
			try {
				// Username. Required to know whom is being assigned.
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple usernames were given: " +
								InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tUsername = UserValidators.validateUsername(t[0]);
				}
				if(tUsername == null) {
					throw
						new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"No username was given: " + InputKeys.USERNAME);
				}
				
				// Password. Optional. If given, requires the user to be
				// created. If not given, requires the user to already exist
				// and be in a class in which the requester is privileged.
				t = getParameterValues(InputKeys.NEW_PASSWORD);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.USER_INVALID_PASSWORD,
							"Multiple passwords were given: " +
								InputKeys.NEW_PASSWORD);
				}
				else if(t.length == 1) {
					tPassword = UserValidators.validatePlaintextPassword(t[0]);
				}
				
				// Class ID. Optional. If given, the requesting user must be
				// privileged in that class in order to add users to it.
				t = getParameterValues(InputKeys.CLASS_URN);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.CLASS_INVALID_ID,
							"Multiple class IDs were given: " +
								InputKeys.CLASS_URN);
				}
				else if(t.length == 1) {
					tClassId = ClassValidators.validateClassId(t[0]);
				}
				
				// Campaign ID. Required. Get the campaign's unique identifier
				// to know which surveys the responses belong.
				t = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID,
							"Multiple campaign IDs were given: " +
								InputKeys.CAMPAIGN_URN);
				}
				else if(t.length == 1) {
					tCampaignId = CampaignValidators.validateCampaignId(t[0]);
				}
				if(tCampaignId == null) {
					throw
						new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID,
							"No campaign ID was given: " +
								InputKeys.CAMPAIGN_URN);
				}
				
				// Survey IDs. Required. The list of surveys that should exist
				// in the user's custom campaign.
				t = getParameterValues(InputKeys.SURVEY_ID_LIST);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_ID,
							"Multiple survey ID lists were given: " +
								InputKeys.SURVEY_ID_LIST);
				}
				else if(t.length == 1) {
					tSurveyIds =
						SurveyResponseValidators.validateSurveyIds(t[0]);
				}
				if((tSurveyIds == null) || (tSurveyIds.size() == 0)) {
					throw
						new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_ID,
							"No survey IDs were given: " +
								InputKeys.SURVEY_ID_LIST);
				}
				
				// Survey response. Optional. These are survey responses that
				// can be used to further configure the user's campaign.
				t = getParameterValues(InputKeys.SURVEYS);
				if(t.length > 1) {
					throw
						new ValidationException(
							ErrorCode.SURVEY_INVALID_RESPONSES,
							"Multiple survey response lists were given: " +
								InputKeys.SURVEYS);
				}
				else if(t.length == 1) {
					tUploadRequest = 
						new SurveyUploadRequest(
							httpRequest, 
							getParameterMap(), 
							tCampaignId, 
							t[0]);
				}
			}
			catch(ValidationException e) {
				e.logException(LOGGER);
				e.failRequest(this);
			}
		}
		
		username = tUsername;
		password = tPassword;
		
		classId = tClassId;
		
		campaignId = tCampaignId;
		surveyIds = tSurveyIds;
		uploadRequest = tUploadRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign assignment request.");

		// Authenticate.
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// If given, verify that the class exists.
			if(classId != null) {
				LOGGER.info("Verifying that the class exists.");
				ClassServices.instance().checkClassExistence(classId, true);
				
				LOGGER
					.info(
						"Verifing that the requesting user is privileged in the class.");
				if(!
					Clazz
						.Role
							.PRIVILEGED
								.equals(
									UserClassServices
										.instance()
											.getUserRoleInClass(
												classId,
												getUser().getUsername()))) {
					
					throw
						new ServiceException(
							ErrorCode.CLASS_INSUFFICIENT_PERMISSIONS,
							"You are not privileged in the class: " + classId);
				}
			}
			
			// Verify that the campaign exists.
			LOGGER.info("Verifying that the campaign exists.");
			CampaignServices
				.instance().checkCampaignExistence(campaignId, true);
			
			// Verify that the user already exists and that the requesting user
			// is allowed to assign a campaign to them.
			if(password == null) {
				LOGGER.info(
					"Verifying that the requesting user is allowed to assign campaigns to the desired user.");
				UserClassServices
					.instance()
						.userIsPrivilegedInAnotherUserClass(
							getUser().getUsername(), 
							username);
			}
			// Otherwise, attempt to create the new user.
			else {
				LOGGER.info("Verifying that the user doesn't already exist.");
				UserServices.instance().checkUserExistance(username, false);
				
				LOGGER.info("Creating the user.");
				UserServices.instance()
					.createUser(
						username, 
						password, 
						null, 
						false, 
						true, 
						false, 
						false);
			}
			
			// If a class ID was given, ensure that the user is part of the
			// as a restricted user.
			if(classId != null) {
				// Make sure the user is not already associated with the class.
				// If they are associated with the class in any capacity, then
				// this is unnecessary.
				if(UserClassServices
					.instance()
						.getUserRoleInClass(classId, username) == null) {
				
					// Create the single-sized map indicating to add the given
					// user with the given permission.
					Map<String, Clazz.Role> usersToAdd = 
						new HashMap<String, Clazz.Role>(1);
					usersToAdd.put(username, Clazz.Role.RESTRICTED);
					
					LOGGER.info("Adding user to class.");
					ClassServices
						.instance()
							.updateClass(
								classId, 
								null, 
								null, 
								usersToAdd, 
								null);
				}
			}
			
			// Upload the survey response.
			uploadRequest.service();
			
			// Create the campaign "mask".
			UserCampaignServices
				.instance()
					.createUserCampaignMask(
						username, 
						campaignId, 
						UUID.randomUUID(), 
						System.currentTimeMillis(), 
						surveyIds);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
			e.failRequest(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
		final HttpServletRequest httpRequest,
		final HttpServletResponse httpResponse) {
		
		if((uploadRequest != null) && (uploadRequest.isFailed())) {
			uploadRequest.respond(httpRequest, httpResponse);
		}
		else {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
		}
	}
}