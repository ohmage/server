package org.ohmage.request.survey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserSurveyResponseServices;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Allows a requester to change the privacy state on a survey 
 * response. The access rules for this API are identical to 
 * those for deleting a survey response.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_ID}</td>
 *     <td>The survey response's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The new privacy state.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 * @see org.ohmage.request.survey.SurveyResponseDeleteRequest
 */
public class SurveyResponseUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseUpdateRequest.class);
	
	private final Long surveyResponseId;
	private final SurveyResponsePrivacyStateCache.PrivacyState privacyState;
	
	/**
	 * Creates a survey response delete request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public SurveyResponseUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey response update request.");
		
		Long tSurveyResponseId = null;
		SurveyResponsePrivacyStateCache.PrivacyState tPrivacyState = null;
		
		if(! isFailed()) {
			try {
				
				// FIXME: for survey_response/delete the parameter is called
				// survey_id, but for survey_response/update the parameter is
				// called survey_key. We should be using survey_id consistently.
				
				LOGGER.info("Validating survey_id parameter.");
				String[] surveyIds = getParameterValues(InputKeys.SURVEY_KEY);
				if(surveyIds.length == 0) {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_KEY_VALUE, "Missing the required survey key: " + InputKeys.SURVEY_KEY);
					throw new ValidationException("Missing the required survey ID: " + InputKeys.SURVEY_KEY);
				}
				else if(surveyIds.length > 1) {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_KEY_VALUE, "Multiple survey key parameters were given.");
					throw new ValidationException("Multiple survey ID parameters were given.");
				}
				else {
					tSurveyResponseId = SurveyResponseValidators.validateSurveyId(this, surveyIds[0]);
					
					if(tSurveyResponseId == null) {
						setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_KEY_VALUE, "Missing the required survey key: " + InputKeys.SURVEY_KEY);
						throw new ValidationException("Missing the required survey key: " + InputKeys.SURVEY_KEY);
					}
				}
				LOGGER.info("Validating privacy_state parameter.");
				String[] privacyStates = getParameterValues(InputKeys.PRIVACY_STATE);
				if(privacyStates.length == 0) {
					setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "Missing the required privacy state: " + InputKeys.PRIVACY_STATE);
					throw new ValidationException("Missing the required privacy state: " + InputKeys.PRIVACY_STATE);
				}
				else if(privacyStates.length > 1) {
					setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "Multiple privacy state parameters were given.");
					throw new ValidationException("Multiple privacy state parameters were given.");
				}
				else {
					tPrivacyState = SurveyResponseValidators.validatePrivacyState(this, privacyStates[0]);
					
					if(tPrivacyState == null) {
						setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "Privacy state is missing.");
						throw new ValidationException("Privacy state is missing.");
					}
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		surveyResponseId = tSurveyResponseId;
		privacyState = tPrivacyState;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to update the survey response.");
			UserSurveyResponseServices.verifyUserCanUpdateOrDeleteSurveyResponse(this, this.getUser().getUsername(), this.surveyResponseId);
			
			LOGGER.info("Updating the survey response.");
			SurveyResponseServices.updateSurveyResponsePrivacyState(this, this.surveyResponseId, this.privacyState);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Replies to the user success or failure with a message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}