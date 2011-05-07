package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.Campaign;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaUploadAwRequest;
import edu.ucla.cens.awserver.request.SurveyUploadAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service for validating the campaign_creation_timestamp sent with certain upload requests. In rare cases, a campaign may be
 * removed from the system and replaced with a newer version that uses the same URN. (The campaign may only be removed if it
 * has zero uploads attached to it.) This validation makes sure that the client attempting to upload is using the campaign with
 * the same creation timestamp as the one stored in the db.
 * 
 * @author Joshua Selsky
 */
public class CampaignCreationTimeValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignCreationTimeValidationService.class);
	
	public CampaignCreationTimeValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("checking the campaign_creation_time in the AwRequest against the campaign's creation time");
		
		Campaign campaign = awRequest.getUser().getCampaignUserRoleMap().get(awRequest.getCampaignUrn()).getCampaign();
		
		// FIXME
		
		if(awRequest instanceof SurveyUploadAwRequest) {
			
			if(! campaign.getCampaignCreationTimestamp().equals(((SurveyUploadAwRequest) awRequest).getCampaignCreationTimestamp())) {
				getAnnotator().annotate(awRequest, "campaign " + ((SurveyUploadAwRequest) awRequest).getCampaignUrn() + " was sent with a campaign " +
					"creation timestamp that does not match what is stored on the server");
			}
			
		} else if (awRequest instanceof MediaUploadAwRequest) {
			
			if(! campaign.getCampaignCreationTimestamp().equals(((MediaUploadAwRequest) awRequest).getCampaignCreationTimestamp())) {
				getAnnotator().annotate(awRequest, "campaign " + ((MediaUploadAwRequest) awRequest).getCampaignUrn() + " was sent with a campaign " +
					"creation timestamp that does not match what is stored on the server");
			}
			
		} else {
			
			throw new ServiceException("found an unsupported AwRequest subclass: " + awRequest.getClass().getName());
		}
	}
}
