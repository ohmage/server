package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Basic validation that the required parameters exist and are reasonable for a
 * user timeseries visualization request.
 * 
 * @author John Jenkins
 */
public class VizUserTimeseriesValidator extends VisualizationValidator {
	private static final Logger _logger = Logger.getLogger(VizUserTimeseriesValidator.class);
	
	/**
	 * Default constructor
	 */
	public VizUserTimeseriesValidator() {
		super();
	}
	
	/**
	 * Validates that all required parameters exist.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is not in the header.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		if(! super.validate(httpRequest)) {
			return false;
		}
		
		String promptId = httpRequest.getParameter(InputKeys.PROMPT_ID);
		String userId = httpRequest.getParameter(InputKeys.USER);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			_logger.warn("Missing required parameter: " + InputKeys.PROMPT_ID);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(userId)) {
			_logger.warn("Missing required parameter: " + InputKeys.USER);
			return false;
		}
		
		return true;
	}
}