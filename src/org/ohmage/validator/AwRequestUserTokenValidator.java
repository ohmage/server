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
 * Validates the userToken (a UUID) from the AwRequest based on the regexp provided at construction time. 
 * 
 * @author selsky
 */
public class AwRequestUserTokenValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(AwRequestUserTokenValidator.class);
	
	public AwRequestUserTokenValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if a user token (a UUID) is not present in the AwRequest or is malformed.  
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the user token follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUserToken())) {
			
			getAnnotator().annotate(awRequest, "empty user token found");
			return false;
		
		}
		
		String userToken = awRequest.getUserToken();
		
		if(! _regexpPattern.matcher(userToken).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect user token: " + userToken);
			return false;
		}
		
		return true;
	}
}
