package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the userToken (a UUID) from the AwRequest based on the regexp provided at construction time. 
 * 
 * @author selsky
 */
public class AwRequestUserTokenValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(AwRequestUserTokenValidator.class);
	
	public AwRequestUserTokenValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if a user token (a UUID) is not present in the AwRequest or is malformed.  
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the user token follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequest.getUserToken())) {
			
			getAnnotator().annotate(awRequest, "empty user token found");
			return false;
		
		}
		
		String userToken = awRequest.getUserToken();
		
		if(! _regexpPattern.matcher(userToken).matches()) {
		
			getAnnotator().annotate(awRequest, "incorrect user token: " + userToken);
			return false;
		}
		
		return true;
	}
}
