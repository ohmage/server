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
public class CampaignRunningStateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignRunningStateValidator.class);
	
	private String[] _states;
	
	/**
	 * Basic constructor that takes the annotator that is called if there is
	 * an error.
	 * 
	 * @param annotator The annotator that is used when this fails.
	 * 
	 * @param states All possible initial states of a campaign.
	 */
	public CampaignRunningStateValidator(AwRequestAnnotator annotator, String[] states) {
		super(annotator);
		
		_states = states;
	}

	/**
	 * Checks that there is a running state and that it is one of the running
	 * states that this object knows about.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating running state.");
		
		String initialRunningState = (String) awRequest.getToValidate().get(CampaignCreationAwRequest.KEY_RUNNING_STATE);
		for(int i = 0; i < _states.length; i++) {
			if(_states[i].equals(initialRunningState)) {
				awRequest.addToProcess(CampaignCreationAwRequest.KEY_RUNNING_STATE, initialRunningState, true);
				return true;
			}
		}
		
		awRequest.setFailedRequest(true);
		getAnnotator().annotate(awRequest, "Invalid initial running state.");
		return false;
	}
}
