package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Abstract superclass for all visualization requests.
 * 
 * @author John Jenkins
 */
public abstract class VisualizationValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(VisualizationValidator.class);
	
	/**
	 * Validates that the required parameters for every visualization request
	 * exist and aren't empty.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is not in the header.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		String width = httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH);
		String height = httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT);
		String campaignId = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(width)) {
			_logger.warn("Missing required parameter: " + InputKeys.VISUALIZATION_WIDTH);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(height)) {
			_logger.warn("Missing required parameter: " + InputKeys.VISUALIZATION_HEIGHT);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			_logger.warn("Missing required parameter: " + InputKeys.CAMPAIGN_URN);
			return false;
		}
		
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		
		return true;
	}

}
