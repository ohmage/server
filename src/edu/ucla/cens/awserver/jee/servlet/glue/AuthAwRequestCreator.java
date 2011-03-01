package edu.ucla.cens.awserver.jee.servlet.glue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.PhoneResultListAwRequest;

/**
 * AwRequestCreator for phone authentication, which includes the extra phone version (phv) parameter.
 * 
 * @author selsky
 */
public class AuthAwRequestCreator implements AwRequestCreator {

	public AuthAwRequestCreator() {
		
	}
	
	/**
	 * Pushes the phone version into the Log4J NDC and creates an AwRequest with the login information and phone version.
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
		
		String ci = request.getParameter("ci"); 
		NDC.push("ci=" + ci); // push the client string into the Log4J NDC for the currently executing thread - this means that it
		                      // will be in every log message for the thread
		
		AwRequest awRequest = new PhoneResultListAwRequest();
		awRequest.setUser(user);
		awRequest.setClient(ci);
		
		return awRequest;
	}
}
