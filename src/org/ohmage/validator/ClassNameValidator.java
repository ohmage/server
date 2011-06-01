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

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * Validates that the class name is valid.
 * 
 * @author John Jenkins
 */
public class ClassNameValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(ClassNameValidator.class);
	
	private boolean _required;
	
	/**
	 * Sets up the validator with a class name.
	 * 
	 * @param annotator The annotator to respond with should there be an
	 * 					error.
	 */
	public ClassNameValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}

	/**
	 * There are no restrictions on a class name other than length which was
	 * checked in the JEE validator, so this does nothing except move it to
	 * the appropriate map if it exists.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the class name.");
		
		String name = (String) awRequest.getToValidate().get(InputKeys.CLASS_NAME);
		if(name == null) {
			if(_required) {
				_logger.error("Request reached the description validator without the required description parameter.");
				throw new ValidatorException("Missing required description.");
			}
			return true;
		}
		
		awRequest.addToProcess(InputKeys.CLASS_NAME, name, true);
		return true;
	}

}
