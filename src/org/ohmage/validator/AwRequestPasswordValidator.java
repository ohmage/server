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
import org.ohmage.util.StringUtils;


/**
 * Validates the password from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestPasswordValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(AwRequestPasswordValidator.class);
	
	public AwRequestPasswordValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the password property of the User within the AwRequest is null, the empty string, all 
	 * whitespace, or if it contains numbers or special characters aside from ".".  
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the user's password follows our convention.");

		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}

		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getPassword())) {
			
			getAnnotator().annotate(awRequest, "empty password found");
			return false;
		
		}

		String password = awRequest.getUser().getPassword();
		
		if(! _regexpPattern.matcher(password).matches()) {
			
			_logger.info("Password: " + password);
		
			getAnnotator().annotate(awRequest, "incorrect password");
			return false;
		}
		
		return true;
	}
}
