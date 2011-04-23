package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignDeletionAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates a new CampaignDeletionAwRequest object.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(CampaignDeletionAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public CampaignDeletionAwRequestCreator() {
		// Does nothing.
	}

	/**
	 * Creates a new CampaignDeletionAwRequest object and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new AwRequest object for deleting a campaign.");
		
		String token = request.getParameter(InputKeys.AUTH_TOKEN);
		String urn = request.getParameter(InputKeys.CAMPAIGN_URN);

		CampaignDeletionAwRequest awRequest = new CampaignDeletionAwRequest();
		awRequest.setUserToken(token);
		awRequest.setCampaignUrn(urn);
		
		return awRequest;
	}
}
