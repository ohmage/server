package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Validates that all of the required parameters for the User Timeseries
 * visualization exist. 
 * 
 * @author John Jenkins
 */
public class VizUserTimeseriesValidator extends VizValidator {
	private static final Logger _logger = Logger.getLogger(VizUserTimeseriesValidator.class);
	
	/**
	 * Default constructor
	 */
	public VizUserTimeseriesValidator() {
		super();
	}
	
	/**
	 * Validates that all required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		if(! super.validate(httpRequest)) {
			return false;
		}
		
		String promptId = httpRequest.getParameter(InputKeys.PROMPT_ID);
		String userId = httpRequest.getParameter(InputKeys.USER_ID);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			_logger.warn("Missing required parameter: " + InputKeys.PROMPT_ID);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(userId)) {
			_logger.warn("Missing required parameter: " + InputKeys.USER_ID);
			return false;
		}
		
		return true;
	}
}
