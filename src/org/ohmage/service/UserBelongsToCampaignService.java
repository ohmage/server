package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that the user belongs to the campaign in the list.
 * 
 * @author John Jenkins
 */
public class UserBelongsToCampaignService extends AbstractAnnotatingService {
	private static final Logger _logger = Logger.getLogger(UserBelongsToCampaignService.class);
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator with which to respond should the user not
	 * 					belong to this campaign.
	 */
	public UserBelongsToCampaignService(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * Checks if the user belongs to the campaign and, if not, reports an 
	 * error.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that the user belongs to the campaign.");
		
		if(! awRequest.getUser().getCampaignUserRoleMap().keySet().contains(awRequest.getCampaignUrn())) {
			getAnnotator().annotate(awRequest, "The user doesn't belong to the campaign.");
		}
	}
}