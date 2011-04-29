package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the username list for a new data point query.
 * 
 * @author selsky
 */
public class UserListValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(UserListValidator.class);
	
	public UserListValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("validating user list");
		
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String userListString = ((SurveyResponseReadAwRequest) awRequest).getUserListString();
		
		// _logger.info(userListString);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(userListString)) {
			
			getAnnotator().annotate(awRequest, "empty user name list found");
			return false;
		
		}
		
		// first check for the special "all users" value
		if("urn:ohmage:special:all".equals(userListString)) {
			
			return true;
			
		} else {
			
			String[] users = userListString.split(",");
			
			if(users.length > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 users in query: " + userListString);
				return false;
				
			} else {
				
				for(int i = 0; i < users.length; i++) {
					if(! _regexpPattern.matcher(users[i]).matches()) {
						getAnnotator().annotate(awRequest, "incorrect user name: " + users[i]);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
