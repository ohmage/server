package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates an incoming survey update HTTP request.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseUpdateValidator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseUpdateValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that the required parameters exist and represent sane values based on their lengths.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {		 
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String campaignUrn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		String surveyKey = httpRequest.getParameter(InputKeys.SURVEY_KEY);
		String privacyState = httpRequest.getParameter(InputKeys.PRIVACY_STATE);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			// Don't log this to avoid flooding the logs when an attack occurs.
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignUrn)) {
			return false;
		} 
		else if(StringUtils.isEmptyOrWhitespaceOnly(surveyKey)) {
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return false;
		} 
		
		if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CAMPAIGN_URN, InputKeys.CAMPAIGN_URN, campaignUrn, 255)) {
			_logger.warn(InputKeys.CAMPAIGN_URN + " is too long.");
			return false;
		} 
		else if(greaterThanLength(InputKeys.SURVEY_KEY, InputKeys.SURVEY_KEY, surveyKey, 10)) {
			_logger.warn(InputKeys.SURVEY_KEY + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.PRIVACY_STATE, InputKeys.PRIVACY_STATE, privacyState, 9)) {
			_logger.warn(InputKeys.PRIVACY_STATE + " is too long.");
			return false;
		}
		
		return true;
	}
}
