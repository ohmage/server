package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignDeletionAwRequest;

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
	public AwRequest createFrom(HttpServletRequest request) { try{
		_logger.info("Creating new AwRequest object for deleting a campaign.");
		
		String token = request.getParameter("auth_token");
		String urn = request.getParameter("campaign_urn");
		
		CampaignDeletionAwRequest awRequest;
		try {
			awRequest = new CampaignDeletionAwRequest(urn);
		}
		catch(IllegalArgumentException e) {
			_logger.info("Invalid parameter found.", e);
			return null;
		}
		awRequest.setUserToken(token);
		
		return awRequest;
		
	}catch(Exception e) { _logger.error(e.toString()); return null; }
	}

}
