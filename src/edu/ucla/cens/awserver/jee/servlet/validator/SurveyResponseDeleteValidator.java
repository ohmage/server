package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates an incoming survey deletion HTTP request.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseDeleteValidator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseDeleteValidator() {
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
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
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
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
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
		else if(greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 250)) {
			_logger.warn(InputKeys.CLIENT + " is too long.");
			return false;
		}
		
		return true;
	}
}
