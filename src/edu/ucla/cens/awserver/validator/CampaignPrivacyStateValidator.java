package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Validates that the privacy state in the current request is one of the
 * privacy states given when this class was created.
 * 
 * @author John Jenkins
 */
public class CampaignPrivacyStateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignPrivacyStateValidator.class);
	
	private String[] _states;
	
	/**
	 * Creates a new validator for the initail privacy state of a campaign.
	 * 
	 * @param annotator An annotator for when the validation fails.
	 *  
	 * @param states All possible initial privacy states.
	 */
	public CampaignPrivacyStateValidator(AwRequestAnnotator annotator, String[] states) {
		super(annotator);
		
		_states = states;
	}
	
	/**
	 * Checks to ensure that the request contains a privacy state and that it
	 * is one of the given privacy states that this object knows about.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating privacy state.");
		
		String initialPrivacyState = (String) awRequest.getToValidate().get(CampaignCreationAwRequest.KEY_PRIVACY_STATE);
		for(int i = 0; i < _states.length; i++) {
			if(_states[i].equals(initialPrivacyState)) {
				awRequest.addToProcess(CampaignCreationAwRequest.KEY_PRIVACY_STATE, initialPrivacyState, true);
				return true;
			}
		}
		
		awRequest.setFailedRequest(true);
		getAnnotator().annotate(awRequest, "Invalid initial privacy state.");
		return false;
	}
}
