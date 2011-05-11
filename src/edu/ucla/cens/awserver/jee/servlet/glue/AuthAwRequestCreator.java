package edu.ucla.cens.awserver.jee.servlet.glue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AuthRequest;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * @author selsky
 */
public class AuthAwRequestCreator implements AwRequestCreator {

	public AuthAwRequestCreator() {
		
	}
	
	/**
	 * Pushes the client HTTP param into the Log4J NDC and creates an AwRequest with the login information.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		
		String userName = request.getParameter(InputKeys.USER);
		String password = null; 
		try {
			
			password = URLDecoder.decode(request.getParameter(InputKeys.PASSWORD), "UTF-8");
		
		} catch(UnsupportedEncodingException uee) { // if UTF-8 is not recognized we have big problems
		
			throw new IllegalStateException(uee);
		}
		
		User user = new User();
		user.setUserName(userName);
		user.setPassword(password);
		
		String client = request.getParameter(InputKeys.CLIENT); 
		NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - 
		                              // this means that it will be in every log message for the thread
		
		AwRequest awRequest = new AuthRequest();
		awRequest.setUser(user);
		
		return awRequest;
	}
}
