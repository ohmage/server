package org.ohmage.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic validation for a survey response count visualization request.
 * 
 * @author John Jenkins
 */
public class VizSurveyResponseCountValidator extends VizValidator {
	/**
	 * Default constructor.
	 */
	public VizSurveyResponseCountValidator() {
		super();
	}

	/**
	 * Validates that all required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		return super.validate(httpRequest);
	}
}
