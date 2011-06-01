/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;


/**
 * @author selsky
 */
public class OutputFormatValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(OutputFormatValidator.class);
	private List<String> _allowedValues;
	
	public OutputFormatValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedValues) {
		super(awRequestAnnotator);
		if(null == allowedValues || allowedValues.isEmpty()) {
			throw new IllegalArgumentException("a non-null, non-empty list is required");
		}
		_allowedValues = allowedValues;
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a SurveyResponseReadAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating output format");
		
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a SurveyResponseReadAwRequest: " + awRequest.getClass());
		}
		
		String outputFormat = ((SurveyResponseReadAwRequest) awRequest).getOutputFormat();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			getAnnotator().annotate(awRequest, "empty output format found");
			return false;
		}
			
		if(! _allowedValues.contains(outputFormat)) {
			getAnnotator().annotate(awRequest, "found an unknown output format: " + outputFormat);
			return false;
		}
		
		return true;
	}
}
