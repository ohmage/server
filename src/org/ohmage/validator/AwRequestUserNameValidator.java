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
 * Validates the userName from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestUserNameValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(AwRequestUserNameValidator.class);
	
	public AwRequestUserNameValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the userName property of the User within the AwRequest is null, the empty string, all 
	 * whitespace, or if it contains numbers or special characters aside from ".".  
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the user's username follows our convention.");
		
		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getUserName())) {
			
			getAnnotator().annotate(awRequest, "empty user name found");
			return false;
		
		}
		
		String userName = awRequest.getUser().getUserName();
		
		if(! _regexpPattern.matcher(userName).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect user name: " + userName);
			return false;
		}
		
		return true;
	}
}
