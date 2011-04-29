package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.request.AwRequest;

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
