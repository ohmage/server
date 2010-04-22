package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class AwRequestCampaignIdValidator extends AbstractAnnotatingValidator {
	
	public AwRequestCampaignIdValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if AwRequest.getUser().getCurrentCampaignId() 
	 */
	public boolean validate(AwRequest awRequest) {
		
		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getCurrentCampaignId())) {
			
			getAnnotator().annotate(awRequest, "empty current campaign id found");
			return false;
		
		}
		
		String campaignId = awRequest.getUser().getCurrentCampaignId();
		
		try {
			
			Integer.parseInt(campaignId);
			
		} catch (NumberFormatException nfe) {
		
			getAnnotator().annotate(awRequest, "non-numeric current campaign id found: " + campaignId);
			return false;
			
		}
				
		return true;
	}
}
