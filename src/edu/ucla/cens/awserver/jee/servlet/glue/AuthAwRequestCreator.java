package edu.ucla.cens.awserver.jee.servlet.glue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ResultListAwRequest;

/**
 * Transformer for creating an AwRequest for authentication.
 * 
 * @author selsky
 */
public class AuthAwRequestCreator implements AwRequestCreator {
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public AuthAwRequestCreator() {
		
	}
	
	/**
	 *  Pulls the u (userName) parameter and the subdomain out of the HttpServletRequest and places them in a new AwRequest.
	 *  Validation of the data is performed within a controller.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		String userName = request.getParameter("u");
		String password = null; 
			
		if(null != request.getParameter("p")) {
			try {
				
				password = URLDecoder.decode(request.getParameter("p"), "UTF-8");
			
			} catch(UnsupportedEncodingException uee) { // if UTF-8 is not recognized we have big problems
			
				throw new IllegalStateException(uee);
			}
		}
		
		UserImpl user = new UserImpl();
		user.setUserName(userName);
		user.setPassword(password);
		
		AwRequest awRequest = new ResultListAwRequest();
		awRequest.setUser(user);
		
		return awRequest;
	}
}
