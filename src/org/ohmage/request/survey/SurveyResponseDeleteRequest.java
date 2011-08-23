package org.ohmage.request.survey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserSurveyResponseServices;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Deletes a survey response and any images associated with it. The 
 * requesting user must be the owner of the survey response and the campaign
 * must be running or they must be a supervisor in the campaign to which the
 * survey response belongs.</p>
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
 * </table>
 * 
 * @author John Jenkins
 */
public class SurveyResponseDeleteRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseDeleteRequest.class);
	
	private final Long surveyResponseId;
	
	/**
	 * Creates a survey response delete request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public SurveyResponseDeleteRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey response delete request.");
		
		Long tSurveyResponseId = null;
		
		if(! isFailed()) {
			try {
				String[] surveyIds = getParameterValues(InputKeys.SURVEY_KEY);
				if(surveyIds.length == 0) {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "Missing the required survey ID: " + InputKeys.SURVEY_KEY);
					throw new ValidationException("Missing the required survey ID: " + InputKeys.SURVEY_KEY);
				}
				else if(surveyIds.length > 1) {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "Multiple survey ID parameters were given.");
					throw new ValidationException("Multiple survey ID parameters were given.");
				}
				else {
					tSurveyResponseId = SurveyResponseValidators.validateSurveyId(this, surveyIds[0]);
					
					if(tSurveyResponseId == null) {
						setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "Missing the required survey ID: " + InputKeys.SURVEY_KEY);
						throw new ValidationException("Missing the required survey ID: " + InputKeys.SURVEY_KEY);
					}
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		surveyResponseId = tSurveyResponseId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response delete request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to delete the survey response.");
			UserSurveyResponseServices.verifyUserCanUpdateOrDeleteSurveyResponse(this, getUser().getUsername(), surveyResponseId);
			
			LOGGER.info("Deleting the survey response.");
			SurveyResponseServices.deleteSurveyResponse(this, surveyResponseId);
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