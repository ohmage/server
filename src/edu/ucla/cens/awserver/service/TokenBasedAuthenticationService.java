package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.UserBin;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

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
