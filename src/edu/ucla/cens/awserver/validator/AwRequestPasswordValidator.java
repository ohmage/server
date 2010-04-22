package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the password from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestPasswordValidator extends AbstractAnnotatingRegexpValidator {
	
	public AwRequestPasswordValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the password property of the User within the AwRequest is null, the empty string, all 
	 * whitespace, or if it contains numbers or special characters aside from ".".  
	 */
	public boolean validate(AwRequest awRequest) {
		
		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getPassword())) {
			
			getAnnotator().annotate(awRequest, "empty password found");
			return false;
		
		}
		
		String password = awRequest.getUser().getPassword();
		
		if(! _regexpPattern.matcher(password).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect character found in password");
			return false;
		}
		
		return true;
	}
}
