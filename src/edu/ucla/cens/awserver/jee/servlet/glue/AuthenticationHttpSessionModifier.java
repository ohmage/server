package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Post-processor for merging authentication results into the user's HttpSession.
 * 
 * @author selsky
 */
public class AuthenticationHttpSessionModifier implements HttpSessionModifier {
//	private static Logger _logger = Logger.getLogger(AuthenticationHttpSessionModifier.class);
	
	/**
	 * Modifies the user's HttpSession based on a successful or failed authentication attempt.
	 */
	public void modifySession(AwRequest awRequest, HttpSession httpSession) {
		
		if(awRequest.isFailedRequest()) {
			
			httpSession.setAttribute("failedLogin", "true");
			
		} else {
			
			// TODO -- place the User object into the session?
			
			httpSession.setAttribute("userName", awRequest.getUser().getUserName());
			httpSession.setAttribute("isLoggedIn", "true");
			
			// remove previously failed login attempt -- removeAttribute() does nothing if no value is bound to the key
			httpSession.removeAttribute("failedLogin");
		}
	}
}
