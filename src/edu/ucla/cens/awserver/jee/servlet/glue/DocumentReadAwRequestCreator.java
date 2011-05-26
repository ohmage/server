package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentReadAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

public class DocumentReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(DocumentReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentReadAwRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Creates a request for document creation 
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating request for document read.");
		
		try {
			DocumentReadAwRequest mRequest = new DocumentReadAwRequest(request.getParameter(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS),
																	   request.getParameter(InputKeys.CAMPAIGN_URN_LIST),
																	   request.getParameter(InputKeys.CLASS_URN_LIST));
			mRequest.setUserToken(request.getParameter(InputKeys.AUTH_TOKEN));
			
			return mRequest;
		}
		catch(IllegalArgumentException e) {
			_logger.error("Error creating document read request.", e);
			throw e;
		}
	}

}
