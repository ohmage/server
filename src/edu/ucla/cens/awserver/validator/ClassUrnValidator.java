package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates a class URN.
 * 
 * @author John Jenkins
 */
public class ClassUrnValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(ClassUrnValidator.class);
	
	/**
	 * Sets up the annotator to reply to the user with if the URN is invalid.
	 * 
	 * @param annotator The information that will be returned to the user if
	 * 					the class URN is invalid.
	 */
	public ClassUrnValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * Checks that the URN begins with "urn:".
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating a class URN.");
		
		String classUrn = (String) awRequest.getToValidate().get(InputKeys.CLASS_URN);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(classUrn)) {
			_logger.error("Missing the class URN. This should have been caught previously.");
			throw new ValidatorException("Missing class URN.");
		}
		
		if(! classUrn.startsWith("urn:")) {
			getAnnotator().annotate(awRequest, "The class URN must begin with 'urn:'.");
			awRequest.setFailedRequest(true);
			return false;
		}

		awRequest.addToProcess(InputKeys.CLASS_URN, classUrn, true);
		return true;
	}

}
