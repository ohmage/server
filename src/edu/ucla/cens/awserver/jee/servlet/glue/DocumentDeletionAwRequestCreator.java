package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentDeletionAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates the document deletion request.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(DocumentDeletionAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentDeletionAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates and returns the request object.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating the document deletion request.");
		
		String authToken = request.getParameter(InputKeys.AUTH_TOKEN);
		String documentId = request.getParameter(InputKeys.DOCUMENT_ID);
		
		DocumentDeletionAwRequest documentDeletionRequest;
		try {
			documentDeletionRequest = new DocumentDeletionAwRequest(documentId);
			documentDeletionRequest.setUserToken(authToken);
		}
		catch(IllegalArgumentException e) {
			_logger.info("The document ID is invalid.");
			throw e;
		}
		
		return documentDeletionRequest;
	}
}