package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassRosterReadRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * Builds a new class roster read request.
 * 
 * @author John Jenkins
 */
public class ClassRosterReadRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(ClassRosterReadRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassRosterReadRequestCreator() {
		// Do nothing.
	}

	/**
	 * Builds a new class roster read request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Building a new class roster read request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return new ClassRosterReadRequest(token, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
	}
}