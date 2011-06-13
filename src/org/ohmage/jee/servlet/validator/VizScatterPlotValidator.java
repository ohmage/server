package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Basic validation that the required parameters exist and are reasonable for a
 * scatter plot visualization request.
 * 
 * @author John Jenkins
 */
public class VizScatterPlotValidator extends VisualizationValidator {
	private static final Logger _logger = Logger.getLogger(VizScatterPlotValidator.class);
	
	/**
	 * Default constructor
	 */
	public VizScatterPlotValidator() {
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
		String prompt2Id = httpRequest.getParameter(InputKeys.PROMPT2_ID);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			_logger.warn("Missing required parameter: " + InputKeys.PROMPT_ID);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(prompt2Id)) {
			_logger.warn("Missing required parameter: " + InputKeys.PROMPT2_ID);
		}
		
		return true;
	}
}