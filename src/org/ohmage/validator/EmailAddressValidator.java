package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;

/**
 * Validates that the parameterized email address is a valid email address.
 * 
 * @author John Jenkins
 */
public class EmailAddressValidator extends AbstractAnnotatingRegexpValidator {
	private static final Logger _logger = Logger.getLogger(EmailAddressValidator.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this validator.
	 * 
	 * @param annotator The annotator to respond with should the validation 
	 * 					fail.
	 * 
	 * @param regExp The regular expression to use to match against the 
	 * 				 parameterized email address.
	 * 
	 * @param key The key to use to get the email address from the request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public EmailAddressValidator(AwRequestAnnotator annotator, String regExp, String key, boolean required) {
		super(regExp, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that the email address is a valid email address.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String emailAddress;
		try {
			emailAddress = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				emailAddress = (String) awRequest.getToValidateValue(_key);
				
				if(emailAddress == null) {
					if(_required) {
						throw new ValidatorException("Missing required key: " + _key);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required key: " + _key);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating an email address from the key: " + _key);
		
		if(! _regexpPattern.matcher(emailAddress).matches()) {
			getAnnotator().annotate(awRequest, "The email address is invalid.");
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(_key, emailAddress, true);
		return true;
	}
}