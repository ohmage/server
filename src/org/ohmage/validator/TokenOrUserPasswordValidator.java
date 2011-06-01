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

import org.ohmage.request.AwRequest;

/**
 * @author joshua selsky
 */
public class TokenOrUserPasswordValidator implements Validator {
	AwRequestUserNameValidator _usernameValidator;
	AwRequestPasswordValidator _passwordValidator;
	AwRequestUserTokenValidator _tokenValidator;
	
	public TokenOrUserPasswordValidator(AwRequestUserTokenValidator tokenValidator, 
			                            AwRequestUserNameValidator usernameValidator,
			                            AwRequestPasswordValidator passwordValidator) {
		
		if(null == passwordValidator) {
			throw new IllegalStateException("a AwRequestPasswordValidator is required");
		}
		
		if(null == usernameValidator) {
			throw new IllegalStateException("a AwRequestUserNameValidator is required");
		}
		
		if(null == tokenValidator) {
			throw new IllegalStateException("a AwRequestUserTokenValidator is required");
		}
		
		_passwordValidator = passwordValidator;
		_usernameValidator = usernameValidator;
		_tokenValidator = tokenValidator;
	}
	
	
	@Override
	public boolean validate(AwRequest awRequest) {
		if(null == awRequest.getUserToken()) {
			
			return _usernameValidator.validate(awRequest) && _passwordValidator.validate(awRequest);
			
		} else {
			
			return _tokenValidator.validate(awRequest);
		}
	}
}
