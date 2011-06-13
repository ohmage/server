package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
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
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
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
		
		return true;
	}

}
