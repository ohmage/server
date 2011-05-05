package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignUpdateAwRequest;

/**
 * Creates an AwRequest object for updating a campaign.
 * 
 * @author John Jenkins
 */
public class CampaignUpdateAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(CampaignUpdateAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public CampaignUpdateAwRequestCreator() {
		// Does nothing.
	}
	
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new AwRequest object for updating a campaign.");

		CampaignUpdateAwRequest awRequest;
		try {
			awRequest = (CampaignUpdateAwRequest) request.getAttribute("awRequest");
		}
		catch(ClassCastException e) {
			throw new IllegalStateException("Invalid awRequest object in HTTPServlet. Must be CampaignCreationAwRequest.");
		}
		if(awRequest == null) {
			throw new IllegalStateException("Missing awRequest in HTTPServlet - Did the HTTPValidator run?");
		}
		
		return awRequest;
	}
}
