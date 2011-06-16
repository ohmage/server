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
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;


/**
 * Validates the username list for a new data point query.
 * 
 * @author selsky
 */
public class UserListValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(UserListValidator.class);
	
	private String _key;
	private boolean _required;
	
	public UserListValidator(String regexp, AwRequestAnnotator awRequestAnnotator, String key, boolean required) {
		super(regexp, awRequestAnnotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("A key is required.");
		}
		
		_key = key;
		_required = required;
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("validating user list for key: " + _key);

		if(awRequest instanceof SurveyResponseReadAwRequest) { // lame
			// TODO: As a quick fix, I (John) moved this into the if statement
			// and implemented a request-agnostic version in the if portion.
			// Once NewDataPointQueryAwRequest has been modified to use the
			// new toValidate map, this whole section can be removed, and it
			// *should* just work out-of-the-box.
			String userListString = ((SurveyResponseReadAwRequest) awRequest).getUserListString();
			
			// _logger.info(userListString);
			
			if(StringUtils.isEmptyOrWhitespaceOnly(userListString)) {
				
				getAnnotator().annotate(awRequest, "empty user name list found");
				return false;
			
			}
			
			// first check for the special "all users" value
			// FIXME: This should be defined somewhere as a constant rather
			// than replicated everywhere.
			if("urn:ohmage:special:all".equals(userListString)) {
				
				return true;
				
			} else {
				
				String[] users = userListString.split(InputKeys.LIST_ITEM_SEPARATOR);
				
				if(users.length > 10) {
					
					getAnnotator().annotate(awRequest, "more than 10 users in query: " + userListString);
					return false;
					
				} else {
					
					for(int i = 0; i < users.length; i++) {
						if(! _regexpPattern.matcher(users[i]).matches()) {
							getAnnotator().annotate(awRequest, "incorrect user name: " + users[i]);
							return false;
						}
					}
				}
			}
			
			return true;
		}
		else { // The generalized solution.
			String userList = (String) awRequest.getToValidate().get(_key);
			
			if(userList == null) {
				if(_required) {
					_logger.error("Missing " + _key + " in request. This should have been caught earlier.");
					throw new ValidatorException("Missing key in request: " + _key);
				}
				else {
					return true;
				}
			}
			
			if("urn:ohmage:special:all".equals(userList)) {
				awRequest.addToProcess(_key, "urn:ohmage:special:all", true);
				return true;
			}
			else {
				String[] users = userList.split(InputKeys.LIST_ITEM_SEPARATOR);
				
				for(int i = 0; i < users.length; i++) {
					if(! _regexpPattern.matcher(users[i]).matches()) {
						getAnnotator().annotate(awRequest, "Invalid user name: " + users[i]);
						awRequest.setFailedRequest(true);
						return false;
					}
				}
			}
			
			awRequest.addToProcess(_key, userList, true);
			return true;
		}
	}
}
