package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.cache.UserBin;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @selsky
 */
public class AddUserToBinService implements Service {
	private UserBin _userBin;
	
	/**
	 * @throws IllegalArgumentException if the provided Dao is null
	 */
	public AddUserToBinService(UserBin userBin) {
		if(null == userBin) {
			throw new IllegalArgumentException("a UserBin is required");
		}
		_userBin = userBin;
	}
	
	/**
	 * Adds the User from the AwRequest to the UserBin.
	 */
	public void execute(AwRequest awRequest) {
		String userToken = _userBin.addUser(awRequest.getUser());
		awRequest.setUserToken(userToken);
	}
}
