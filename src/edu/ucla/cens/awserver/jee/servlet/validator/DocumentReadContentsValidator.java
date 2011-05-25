package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates that the required parameters are present and aren't overly large.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentReadContentsValidator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentReadContentsValidator() {
		// Do nothing.
	}

	/**
	 * Checks that the required parameters exist and that their sizes are
	 * appropriate.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String documentId = httpRequest.getParameter(InputKeys.DOCUMENT_ID);
		
		if((authToken == null) || (authToken.length() != 36)) {
			_logger.info("The auth token is missing or an incorrect size.");
			return false;
		}
		else if((documentId == null) || (documentId.length() != 36)) {
			_logger.info("The document's ID is missing or an incorrect size.");
			return false;
		}
		
		return true;
	}

}
