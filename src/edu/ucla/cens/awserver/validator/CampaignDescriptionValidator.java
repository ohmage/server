package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Validates that the description of a campaign is valid.
 * 
 * @author John Jenkins
 */
public class CampaignDescriptionValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignDescriptionValidator.class);
	
	/**
	 * Builds this validator with the annotator specified in the configuration.
	 * 
	 * @param annotator An annotator should this validation fail.
	 */
	public CampaignDescriptionValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * There isn't anything that can be wrong with the description, but this
	 * is an opportunity to put it in the toProcess map.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the campaign description.");
		
		try {
			awRequest.addToProcess(CampaignCreationAwRequest.KEY_DESCRIPTION, awRequest.getToValidate().get(CampaignCreationAwRequest.KEY_DESCRIPTION), true);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException(e);
		}

		return true;
	}
}
