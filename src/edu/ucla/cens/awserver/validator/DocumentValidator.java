package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates the document.
 * 
 * Currently, there are no requirements on the document, so this just migrates
 * the value across maps.
 * 
 * @author John Jenkins
 */
public class DocumentValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DocumentValidator.class);
	
	private boolean _required;
	
	/**
	 * Sets up this validator with an annotator to use if validation fails and
	 * a flag as to whether the value is required.
	 * 
	 * @param annotator The annotator to respond with if the validation fails.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public DocumentValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}

	/**
	 * There is no validation for the document, so we just migrate it to the
	 * toProcess map in case it wasn't already there.
	 * 
	 * @throws ValidatorException Thrown if the document is missing and is
	 * 							  required.
	 */
	@Override
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		String document;
		try {
			document = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT);
		}
		catch(IllegalArgumentException outerException) {
			try {
				document = (String) awRequest.getToValidateValue(InputKeys.DOCUMENT);
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required value with key '" + InputKeys.DOCUMENT + "'.");
				}
				else {
					return true;
				}
			}
		}
		_logger.info("Validating the document.");
		
		// There is no validation to be done, so we just migrate it to the
		// toProcess map in case it wasn't already there.
		
		awRequest.addToProcess(InputKeys.DOCUMENT, document, true);
		return true;
	}

}
