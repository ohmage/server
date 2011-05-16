package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Performs basic validation on a URN.
 * 
 * @author John Jenkins
 */
public class GenericUrnValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(GenericUrnValidator.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Creates a generic URN validator that uses the parameterized key and
	 * whether or not the key is required.
	 * 
	 * @param annotator The annotator to use if the validation fails.
	 * 
	 * @param key The key to use to get the value from the maps.
	 * 
	 * @param required Whether or not this validation is required.
	 * 
	 * @throws IllegalArgumentException The exception to throw if there is an 
	 */
	public GenericUrnValidator(AwRequestAnnotator annotator, String key, boolean required) throws IllegalArgumentException {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or an empty string.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Gets the URN first from the toProcess map in case another validator has
	 * already validated it to some degree and potentially modified it or, if
	 * it doesn't exist there, gets it from the toValidate map. Then, uses the
	 * basic URN validation to ensure that it is a valid URN.
	 * 
	 * If everything is successful, it places the validated URN in the
	 * toProcess map, potentially overwriting the old URN it just got from
	 * there. 
	 * 
	 * @throws ValidatorException Thrown if the value for '_key' is missing in
	 * 							  both maps.
	 */
	@Override
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		// Get the URN from the request based on the '_key'.
		String urn;
		try {
			// First, check if it's in the toProcess map, because someone else
			// may have already done some validation and altered it in some
			// way.
			urn = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				// If not in the toProcess map, check if its in the toValidate
				// map and hasn't had any validation done on it yet.
				urn = (String) awRequest.getToValidateValue(_key);
			}
			catch(IllegalArgumentException innerException) {
				// It wasn't in either map. If it's not required, 
				if(_required) {
					throw new ValidatorException("The required parameter '" + _key + "'.");
				}
				return true;
			}
		}
		_logger.info("Validating URN for key '" + _key + "'.");
		
		if(! StringUtils.isValidUrn(urn)) {
			getAnnotator().annotate(awRequest, "Invalid URN found for key '" + _key + "': " + urn);
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(_key, urn, true);
		return true;
	}
}