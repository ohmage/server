package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * When a new campaign is being created, this validates that the initial
 * running state is one of the valid running states.
 * 
 * @author John Jenkins
 */
public class CampaignCreationRunningStateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignCreationRunningStateValidator.class);
	
	private String[] _states;
	
	/**
	 * Basic constructor that takes the annotator that is called if there is
	 * an error.
	 * 
	 * @param annotator The annotator that is used when this fails.
	 * 
	 * @param states All possible initial states of a campaign.
	 */
	public CampaignCreationRunningStateValidator(AwRequestAnnotator annotator, String[] states) {
		super(annotator);
		
		_states = states;
	}

	/**
	 * Checks that the running state in the request against all plausible
	 * running states.
	 * 
	 * Note: Specific to CampaignCreationAwRequest objects.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating initial running state.");
		
		CampaignCreationAwRequest ccAwRequest;
		try {
			ccAwRequest = (CampaignCreationAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Attempting validation on a non-CampaignCreationAwRequest object.");
			throw new ValidatorException("Validator is specific to CampaignCreationAwRequest objects.");
		}
		
		String initialRunningState = ccAwRequest.getRunningState();
		for(int i = 0; i < _states.length; i++) {
			if(_states[i].equals(initialRunningState)) {
				return true;
			}
		}
		
		return false;
	}
}
