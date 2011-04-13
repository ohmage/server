package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

public class CampaignCreationPrivacyStateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignCreationPrivacyStateValidator.class);
	
	private String[] _states;
	
	/**
	 * Creates a new validator for the initail privacy state of a campaign.
	 * 
	 * @param annotator An annotator for when the validation fails.
	 *  
	 * @param states All possible initial privacy states.
	 */
	public CampaignCreationPrivacyStateValidator(AwRequestAnnotator annotator, String[] states) {
		super(annotator);
		
		_states = states;
	}
	
	/**
	 * Checks to ensure that the request contains a privacy state and that it
	 * is one of the possible initial privacy states.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating initial privacy state.");
		
		CampaignCreationAwRequest ccAwRequest;
		try {
			ccAwRequest = (CampaignCreationAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Attempting validation on a non-CampaignCreationAwRequest object.");
			throw new ValidatorException("Validator is specific to CampaignCreationAwRequest objects.");
		}
		
		String initialPrivacyState = ccAwRequest.getPrivacyState();
		for(int i = 0; i < _states.length; i++) {
			if(_states[i].equals(initialPrivacyState)) {
				return true;
			}
		}
		
		return false;
	}
}
