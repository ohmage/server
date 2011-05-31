package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates a new CampaignCreationAwRequest object for handling the rest of the
 * request.
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(CampaignCreationAwRequestCreator.class);

	/**
	 * Default constructor.
	 */
	public CampaignCreationAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * Gets the CampaignCreationAwRequest object out of the request where it
	 * was stashed during HTTP validation to prevent us from parsing the XML
	 * twice.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new request object for creating a new campaign.");

		CampaignCreationAwRequest awRequest;
		try {
			awRequest = (CampaignCreationAwRequest) request.getAttribute("awRequest");
		}
		catch(ClassCastException e) {
			throw new IllegalStateException("Invalid awRequest object in HTTPServlet. Must be CampaignCreationAwRequest.");
		}
		if(awRequest == null) {
			throw new IllegalStateException("Missing awRequest in HTTPServlet - Did the HTTPValidator run?");
		}
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return awRequest;
	}
}