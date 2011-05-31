package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentCreationAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

public class DocumentCreationAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(DocumentCreationAwRequestCreator.class);

	/**
	 * Default constructor.
	 */
	public DocumentCreationAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * Gets the DocumentCreationAwRequest object out of the request where it
	 * was stashed during HTTP validation to prevent us from parsing the
	 * document twice.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new request object for creating a new document.");

		DocumentCreationAwRequest awRequest;
		try {
			awRequest = (DocumentCreationAwRequest) request.getAttribute("awRequest");
		}
		catch(ClassCastException e) {
			throw new IllegalStateException("Invalid awRequest object in HTTPServlet. Must be DocumentCreationAwRequest.");
		}
		if(awRequest == null) {
			throw new IllegalStateException("Missing awRequest in HTTPServlet - Did the HTTPValidator run?");
		}
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return awRequest;
	}
}