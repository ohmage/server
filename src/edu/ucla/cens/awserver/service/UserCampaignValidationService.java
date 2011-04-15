package edu.ucla.cens.awserver.service;

import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Post-authentication service for making sure that the logged-in user has access to the campaign specified in the request params.
 * 
 * @author selsky
 */
public class UserCampaignValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserCampaignValidationService.class);
	
	public UserCampaignValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	
	@Override
	public void execute(AwRequest awRequest) {
		// check whether the logged in user has access to the campaign in the query params
		
		Set<String> allowedCampaigns = awRequest.getUser().getCampaignRoles().keySet();
		
		//_logger.warn("campaigns: " + allowedCampaigns);
		
		if(! allowedCampaigns.contains(awRequest.getCampaignUrn())) {
			_logger.warn("user attempting to access a campaign they do not belong to. user: " + 
				awRequest.getUser().getUserName() + " campaign: " + awRequest.getCampaignUrn());
			getAnnotator().annotate(awRequest, "user attempt to access a campaign they do not belong to");
		}
	}
}
