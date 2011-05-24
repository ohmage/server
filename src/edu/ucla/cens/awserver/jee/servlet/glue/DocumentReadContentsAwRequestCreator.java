package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentReadContentsAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates a request to read the contents of a document.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(DocumentReadContentsAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentReadContentsAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates the document read request from the HTTP request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		String authToken = request.getParameter(InputKeys.AUTH_TOKEN);
		String documentId = request.getParameter(InputKeys.DOCUMENT_ID);
		
		DocumentReadContentsAwRequest mRequest;
		try {
			mRequest = new DocumentReadContentsAwRequest(documentId);
			mRequest.setUserToken(authToken);
		}
		catch(IllegalArgumentException e) {
			_logger.error("The document ID is invalid, but passed HTTP validation.");
			throw e;
		}
		
		return mRequest;
	}

}
