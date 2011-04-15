package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignDeletionAwRequest;

/**
 * Checks that the URN is a valid URN.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionCampaignUrnValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignDeletionCampaignUrnValidator.class);
	
	/**
	 * Basic constructor that takes an annotator to response to if something fails.
	 * 
	 * @param annotator Annotates the error if one should arise.
	 */
	public CampaignDeletionCampaignUrnValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks that the URN in the request is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the campaign URN.");
		
		CampaignDeletionAwRequest cdAwRequest;
		try {
			cdAwRequest = (CampaignDeletionAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Attempting validation on a non-CampaignDeletionAwRequest object.");
			throw new ValidatorException("Validator is specific to CampaignDeletionAwRequest objects.");
		}
		
		String urn = cdAwRequest.getRequestUrn();
		if(! urn.startsWith("urn:")) {
			awRequest.setFailedRequest(true);
			getAnnotator().annotate(awRequest, "Invalid URN.");
			return false;
		}
		
		return true;
	}
}
