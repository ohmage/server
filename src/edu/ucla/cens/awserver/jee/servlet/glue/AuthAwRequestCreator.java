package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.datatransfer.AwRequestImpl;
import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Transformer for creating an AwRequest for authentication.
 * 
 * @author selsky
 */
public class AuthAwRequestCreator implements AwRequestCreator {
//	private static Logger _logger = Logger.getLogger(AuthAwRequestCreator.class);
	
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
		String subdomain = StringUtils.retrieveSubdomainFromUrlString(request.getRequestURL().toString());
		String serverName = StringUtils.retrieveServerNameFromUrlString(request.getRequestURL().toString());
//		_logger.info("found subdomain: " + subdomain + " from URL: " + request.getRequestURL());
		
		String userName = request.getParameter("u");
		UserImpl user = new UserImpl();
		user.setUserName(userName);
		
		AwRequestImpl awRequest = new AwRequestImpl();
		awRequest.setUser(user);
		awRequest.setAttribute("subdomain", subdomain);
		awRequest.setAttribute("serverName", serverName);
		
		return awRequest;
	}
}
