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

import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;


/**
 * Validates that a UUID is a valid UUID.
 * 
 * @author John Jenkins
 */
public class UuidValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(UuidValidator.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Constructor for this validator.
	 * 
	 * @param annotator The annotator to respond with should the validation
	 * 					fail.
	 * 
	 * @param regexp The regular expression to validate a UUID against.
	 */
	public UuidValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that, if the UUID is required that it exists. Then, checks
	 * that the UUID is a valid UUID.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String uuid;
		try {
			uuid = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			uuid = (String) awRequest.getToValidateValue(_key);
			
			if(uuid == null) {
				if(_required) {
					throw new IllegalArgumentException("Missing required key '" + _key + "'.");
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating UUID for key '" + _key + "'.");

		try {
			UUID.fromString(uuid);
		}
		catch(IllegalArgumentException e) {
			getAnnotator().annotate(awRequest, "The given UUID isn't a valid UUID.");
			return false;
		}
		
		awRequest.addToProcess(_key, uuid, true);
		return true;
	}
}
