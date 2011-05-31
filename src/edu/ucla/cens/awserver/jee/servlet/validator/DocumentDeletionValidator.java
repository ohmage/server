package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates that the document deletion request contains all the necessary
 * parameters.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentDeletionValidator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentDeletionValidator() {
		// Do nothing.
	}
	
	/**
	 * Checks that all the required parameters exist and that their size isn't
	 * overly large.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String documentId = httpRequest.getParameter(InputKeys.DOCUMENT_ID);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if((authToken == null) || (authToken.length() != 36)) {
			_logger.warn("The auth token is missing or the wrong length.");
			return false;
		}
		else if((documentId == null) || (documentId.length() != 36)) {
			_logger.warn("The document ID is null.");
			return false;
		}
		else if(client == null) {
			_logger.warn("The client is missing.");
			return false;
		}
		
		return true;
	}
}