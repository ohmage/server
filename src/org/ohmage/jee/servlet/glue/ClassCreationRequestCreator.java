package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassCreationRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * Creates a new class creation request object.
 * 
 * @author John Jenkins
 *
 */
public class ClassCreationRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(ClassCreationRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassCreationRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a new class creation request object.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating a class creation request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return new ClassCreationRequest(
				token,
				httpRequest.getParameter(InputKeys.CLASS_URN),
				httpRequest.getParameter(InputKeys.CLASS_NAME),
				httpRequest.getParameter(InputKeys.DESCRIPTION));
	}
}
