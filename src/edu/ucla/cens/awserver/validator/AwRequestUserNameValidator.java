package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the userName from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestUserNameValidator extends AbstractAnnotatingValidator {
	private String _allowedCharacters;

	/**
	 * Creates an instance of this class using allowedCharacters as the set of characters that userNames are validated against. 
	 * 
	 * @param allowedCharacters
	 * @throws IllegalArgumentException if the passed in value for allowedCharacters is null, empty, or all whitespace
	 */
	public AwRequestUserNameValidator(String allowedCharacters, AwRequestAnnotator failedValidationStrategy) {
		super(failedValidationStrategy);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(allowedCharacters)) {
			throw new IllegalArgumentException("a null, empty, or all-whitespace string is not allowed");
		}
		
		_allowedCharacters = allowedCharacters;
	}
	
	/**
	 * @throws ValidatorException if the userName property of the User within the AwRequest is null, the empty string, all 
	 * whitespace, or if it contains numbers or special characters aside from ".".  
	 */
	public boolean validate(AwRequest awRequest) {
		
		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getUserName())) {
			
			getAnnotator().annotate(awRequest, "empty user name found");
			return false;
		
		}
		
		String userName = awRequest.getUser().getUserName();
		int length = userName.length();
		
		for(int i = 0; i < length; i++) {
			
			CharSequence subSequence = userName.subSequence(i, i + 1);
			
			if(! _allowedCharacters.contains(subSequence)) {
				
				getAnnotator().annotate(awRequest, "incorrect character found in user name: " + subSequence);
				return false;
			}
		}
		
		return true;
	}
}
