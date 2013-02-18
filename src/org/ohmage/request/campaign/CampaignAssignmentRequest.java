package org.ohmage.request.campaign;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;
import org.ohmage.validator.UserValidators;

public class CampaignAssignmentRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(CampaignAssignmentRequest.class);
	
	final String username;
	final String password;
	
	final String campaignId;
	final Set<String> surveyIds;
	final List<JSONObject> surveyResponses;

	public CampaignAssignmentRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		String tUsername = null;
		String tPassword = null;
		
		String tCampaignId = null;
		Set<String> tSurveyIds = null;
		List<JSONObject> tSurveyResponses = Collections.emptyList();
		
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
				
				// Password. Optional. If given, requires user to be created.
				// If not given, requires user to already exist and be in a
				// class in which the requester is privileged.
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
								InputKeys.CAMPAIGN_URN);
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
					try {
						tSurveyResponses = 
							CampaignValidators.validateUploadedJson(t[0]);
					}
					catch(IllegalArgumentException e) {
						throw new ValidationException(
							ErrorCode.SURVEY_INVALID_RESPONSES, 
							"The survey responses could not be URL decoded.",
							e);
					}
				}
			}
			catch(ValidationException e) {
				e.logException(LOGGER);
				e.failRequest(this);
			}
		}
		
		username = tUsername;
		password = tPassword;
		
		campaignId = tCampaignId;
		surveyIds = tSurveyIds;
		surveyResponses = tSurveyResponses;
	}

	@Override
	public void service() {
		LOGGER.info("Servicing the campaign assignment request.");

		// Authenticate.
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// Verify that the user already exists.
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
				// TODO: Either create the user directly or create a user
				// registration.
			}
			
			// Verify that the campaign exists.
			CampaignServices
				.instance().checkCampaignExistence(campaignId, true);
			
			// Create the campaign "mask".
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
		
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}