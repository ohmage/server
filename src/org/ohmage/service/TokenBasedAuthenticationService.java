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
package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service for authenticating users based on a token instead of the usual username-password.
 * 
 * @author selsky
 */
public class TokenBasedAuthenticationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(TokenBasedAuthenticationService.class);
	private UserBin _userBin;

	public TokenBasedAuthenticationService(UserBin userBin, AwRequestAnnotator annotator) {
		super(annotator);
		if(null == userBin) {
			throw new IllegalArgumentException("a UserBin is required");
		}
		_userBin = userBin;
	}
	
	/**
	 * 	Checks the UserBin for existence of an authenticated User based on the token provided in the AwRequest. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to authenticate token.");
		
		String token = awRequest.getUserToken();
		User user =  _userBin.getUser(token);
		
		if(null == user) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("no user found for token " + token);
			}
			
			getAnnotator().annotate(awRequest, "no user found for token");
			
		} else {
			
			awRequest.setUser(user);
		}
	}
}
