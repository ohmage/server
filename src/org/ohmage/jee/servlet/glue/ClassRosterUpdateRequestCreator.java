package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;

/**
 * Builds a new class roster update request from a HttpServletRequest.
 * 
 * @author John Jenkins
 */
public class ClassRosterUpdateRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(ClassRosterUpdateRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassRosterUpdateRequestCreator() {
		// Do nothing.
	}

	/**
	 * Builds a new class roster update request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Building new class roster update request.");
		
		return (AwRequest) httpRequest.getAttribute("awRequest");
	}
}