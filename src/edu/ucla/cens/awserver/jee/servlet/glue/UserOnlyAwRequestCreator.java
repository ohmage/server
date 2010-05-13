package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.EmaVizQueryAwRequest;

/**
 * @author selsky
 */
public class UserOnlyAwRequestCreator implements AwRequestCreator {
	
	public UserOnlyAwRequestCreator() {
		
	}
	
	/**
	 * Copies the user out of the HttpSession bound to the current request and places it in a new AwRequest.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		HttpSession session = request.getSession();
		User user = new UserImpl((User) session.getAttribute("user"));
		AwRequest awRequest = new EmaVizQueryAwRequest();
		awRequest.setUser(user);
		return awRequest;
	}
}
