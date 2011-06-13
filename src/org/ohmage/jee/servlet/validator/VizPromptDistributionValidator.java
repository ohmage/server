package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Basic validation that all the required parameters exist and are reasonable
 * for a prompt distribution visualization request.
 * 
 * @author John Jenkins
 */
public class VizPromptDistributionValidator extends VisualizationValidator {
	private static final Logger _logger = Logger.getLogger(VizPromptDistributionValidator.class);
	
	/**
	 * Default constructor
	 */
	public VizPromptDistributionValidator() {
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
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			_logger.warn("Missing required parameter: " + InputKeys.PROMPT_ID);
			return false;
		}
		
		return true;
	}
}