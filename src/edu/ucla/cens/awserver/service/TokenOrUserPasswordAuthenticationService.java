package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author joshua selsky
 */
public class TokenOrUserPasswordAuthenticationService implements Service {
	private AuthenticationService _authenticationService;
	private TokenBasedAuthenticationService _tokenBasedAuthenticationService;
	
	public TokenOrUserPasswordAuthenticationService(TokenBasedAuthenticationService tokenBasedAuthenticationService, 
			   										AuthenticationService authenticationService) {
		if(null == authenticationService) {
			throw new IllegalStateException("an AuthenticationService  is required");
		}
		if(null == tokenBasedAuthenticationService) {
			throw new IllegalStateException("a TokenBasedAuthenticationService is required");
		}
		
		_authenticationService = authenticationService;
		_tokenBasedAuthenticationService = tokenBasedAuthenticationService;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		if(null == awRequest.getUserToken()) {
			_authenticationService.execute(awRequest);
		} else {
			_tokenBasedAuthenticationService.execute(awRequest);
		}
	}
}
