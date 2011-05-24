package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Basic validation for a document read request.
 * 
 * @author John Jenkins
 */
public class DocumentReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentReadValidator() {
		// Do nothing.
	}

	/**
	 * Validates that the required parameters are present and that their sizes
	 * are rational.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String personalDocuments = httpRequest.getParameter(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
		
		if((authToken == null) || (authToken.length() != 36)) {
			_logger.error("The auth token is null or of the wrong length.");
			return false;
		}
		else if((personalDocuments == null) || StringUtils.isEmptyOrWhitespaceOnly(personalDocuments)) {
			_logger.error("The personal documents boolean is missing or empty.");
			return false;
		}
		
		return true;
	}

}
