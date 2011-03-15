package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service that checks that the currently logged in user has access to the campaign they are attempting to access.
 * 
 * @author selsky
 */
public class CampaignAccessCheckService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignAccessCheckService.class);
	
	public CampaignAccessCheckService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("verifying that the logged-in user has access to the campaign specified in the query");
		
		if(! awRequest.getUser().getCampaignRoles().keySet().contains(awRequest.getCampaignName())) {
			_logger.warn("user attempting to query against a campaign they do not belong to. user: " + 
				awRequest.getUser().getUserName() + " campaign: " + awRequest.getCampaignName());
			getAnnotator().annotate(awRequest, "user attempt to query a campaign they do not belong to");
		}
	}
}
