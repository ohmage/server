package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ClassReadAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates an internal request for reading classes.
 * 
 * @author John Jenkins
 */
public class ClassReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ClassReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassReadAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a request object based on the parameters from the HTTP request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating class read request.");
		
		ClassReadAwRequest internalRequest = new ClassReadAwRequest(request.getParameter(InputKeys.CLASS_URN_LIST));
		internalRequest.setUserToken(request.getParameter(InputKeys.AUTH_TOKEN));
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return internalRequest;
	}
}
