package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassDeletionRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * Creates a new class deletion request.
 * 
 * @author John Jenkins
 */
public class ClassDeletionRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(ClassDeletionRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassDeletionRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a new class deletion request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating a class deletion request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return new ClassDeletionRequest(
				token,
				httpRequest.getParameter(InputKeys.CLASS_URN));
	}
}