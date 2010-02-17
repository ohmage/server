package edu.ucla.cens.awserver.validator;

import java.util.regex.Pattern;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the userName from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestUserNameValidator extends AbstractAnnotatingValidator {
	// private String _allowedCharacters;
	private Pattern _regexpPattern;

	/**
	 * Creates an instance of this class using allowedCharacters as the set of characters that userNames are validated against. 
	 *
	 * @throws IllegalArgumentException if the passed in regexp is null, empty, or all whitespace
	 * @throws PatternSyntaxException if the passed in regexp is invalid
	 */
	public AwRequestUserNameValidator(String regexp, AwRequestAnnotator failedValidationStrategy) {
		super(failedValidationStrategy);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(regexp)) {
			throw new IllegalArgumentException("a null, empty, or all-whitespace string is not allowed");
		}
		
		_regexpPattern = Pattern.compile(regexp);
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
		
		if(! _regexpPattern.matcher(userName).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect character found in user name: " + userName);
			return false;
		}
		
		return true;
	}
}
