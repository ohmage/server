package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Checks that the URN is a valid URN.
 * 
 * @author John Jenkins
 */
public class CampaignUrnValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignUrnValidator.class);
	
	/**
	 * Basic constructor that takes an annotator to respond with if something
	 * fails.
	 * 
	 * @param annotator Annotates the error if one should arise.
	 */
	public CampaignUrnValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks that the URN in the request exists and is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the campaign URN.");
		
		String urn = awRequest.getCampaignUrn();
		if(! urn.startsWith("urn:")) {
			awRequest.setFailedRequest(true);
			getAnnotator().annotate(awRequest, "Invalid URN.");
			return false;
		}

		return true;
	}
}
