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
import org.ohmage.util.StringUtils;


/**
 * @author selsky
 */
public class StringAllowedValuesValidator extends AbstractAnnotatingValidator {
	private Logger _logger = Logger.getLogger(StringAllowedValuesValidator.class);
	
	private List<String> _allowedValues;
	private String _key;
	private boolean _required;
	private String _errorMessage;
	
	public StringAllowedValuesValidator(AwRequestAnnotator annotator, String key, List<String> allowedValues , boolean required) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(null == allowedValues || allowedValues.size() < 1) {
			throw new IllegalArgumentException("allowed values must not be null and must contain at least one entry");
		}
		_allowedValues = allowedValues;
		_key = key;
		_required = required;
	}
	
	public boolean validate(AwRequest awRequest) {		
		_logger.info("Validating that a String value is one of the allowed values for parameter: " + _key);
		
		String value = (String) awRequest.getToValidate().get(_key);
		
		if(_required) {
			if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
				getAnnotator().annotate(awRequest, _errorMessage);
				return false;
			}	
		}
		
		if(null != value) { // validate the content
			if(! _allowedValues.contains(value)) {
				getAnnotator().annotate(awRequest, "found disallowed value for " + _key + ". value: " + value);
				return false;
			}
			
			awRequest.addToProcess(_key, value, true);
		}
		
		return true;
	}
	
	/**
	 * @throws IllegalArgumentException if the provided error message is null, empty, or all whitespace
	 */
	public void setErrorMessage(String errorMessage) {
		if(StringUtils.isEmptyOrWhitespaceOnly(errorMessage)) {
			throw new IllegalArgumentException("an errorMessage is required");
		}
		_errorMessage = errorMessage;
	}
}
