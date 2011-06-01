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
import org.ohmage.util.StringUtils;


/**
 * Validates that the new password follows out conventions.
 * 
 * @author John Jenkins
 */
public class NewPasswordValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(NewPasswordValidator.class);
	
	/**
	 * Creates a new validator with the expression to use to validate the new
	 * password as well as an annotator to respond with should it not be.
	 *  
	 * @param regexp The expression that regulates what a new password in the
	 * 				 system must look like.
	 * 
	 * @param awRequestAnnotator The annotator to use should the password be
	 * 							 valid.
	 */
	public NewPasswordValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * Checks that the new password follows out conventions
	 */
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		_logger.info("Validating that the user's new password follows our convention.");

		if(StringUtils.isEmptyOrWhitespaceOnly((String) awRequest.getToValidate().get(InputKeys.NEW_PASSWORD))) {
			_logger.error("The new password is missing from the toValidate map.");
			awRequest.setFailedRequest(true);
			return false;
		}

		String newPassword = (String) awRequest.getToValidate().get(InputKeys.NEW_PASSWORD);
		
		if(! _regexpPattern.matcher(newPassword).matches()) {
			getAnnotator().annotate(awRequest, "Invalid new password: " + newPassword);
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(InputKeys.NEW_PASSWORD, newPassword, true);
		return true;
	}
}
