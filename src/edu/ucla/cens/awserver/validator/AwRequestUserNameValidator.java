package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the userName from the User within the AwRequest.
 * 
 * @author selsky
 */
public class AwRequestUserNameValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(AwRequestUserNameValidator.class);
	
	public AwRequestUserNameValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the userName property of the User within the AwRequest is null, the empty string, all 
	 * whitespace, or if it contains numbers or special characters aside from ".".  
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the user's username follows our convention.");
		
		if(null == awRequest.getUser()) { // logical error!
			
			throw new ValidatorException("User object not found in AwRequest");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUser().getUserName())) {
			
			getAnnotator().annotate(awRequest, "empty user name found");
			return false;
		
		}
		
		String userName = awRequest.getUser().getUserName();
		
		if(! _regexpPattern.matcher(userName).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect user name: " + userName);
			return false;
		}
		
		return true;
	}
}
