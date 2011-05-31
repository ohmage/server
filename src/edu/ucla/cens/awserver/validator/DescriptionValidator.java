package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates that the description of a campaign is valid.
 * 
 * @author John Jenkins
 */
public class DescriptionValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DescriptionValidator.class);
	
	private boolean _required;
	
	/**
	 * Builds this validator with the annotator specified in the configuration.
	 * 
	 * @param annotator An annotator should this validation fail.
	 */
	public DescriptionValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}

	/**
	 * There isn't anything that can be wrong with the description, but this
	 * is an opportunity to put it in the toProcess map.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String description;
		try {
			description = (String) awRequest.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch (IllegalArgumentException outerException) {
			try {
				description = (String) awRequest.getToValidateValue(InputKeys.DESCRIPTION);
				
				if(description == null) {
					if(_required) {
						_logger.error("Missing required key: " + InputKeys.DESCRIPTION);
						throw new ValidatorException("Missing required description.");
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException e) {
				if(_required) {
					_logger.error("Missing required key: " + InputKeys.DESCRIPTION);
					throw new ValidatorException("Missing required description.");
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating the description.");
		
		awRequest.addToProcess(InputKeys.DESCRIPTION, description, true);
		return true;
	}
}
