package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Validates that the new username in the request follows our conventions.
 * 
 * @author John Jenkins
 */
public class NewUsernameValidator extends AbstractAnnotatingRegexpValidator {
	public static final Logger _logger = Logger.getLogger(NewUsernameValidator.class);
	
	/**
	 * Creates this validator.
	 * 
	 * @param regexp The regular expression that any new usernames must match
	 * 				 against.
	 * 
	 * @param awRequestAnnotator The annotator to respond with should the
	 * 							 validation fail.
	 */
	public NewUsernameValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}

	/**
	 * Gets the new user's username from the request and validates that it
	 * matches the regular expression.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the new user's username follows our convention.");

		String newUsername;
		try {
			newUsername = (String) awRequest.getToValidate().get(InputKeys.NEW_USERNAME);
		}
		catch(IllegalArgumentException e) {
			_logger.error("The new username is missing from the toValidate map.");
			awRequest.setFailedRequest(true);
			return false;
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(newUsername)) {
			_logger.error("The new username is missing from the toValidate map.");
			awRequest.setFailedRequest(true);
			return false;
		}
		
		if(! _regexpPattern.matcher(newUsername).matches()) {
			getAnnotator().annotate(awRequest, "Invalid new username: " + newUsername);
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(InputKeys.NEW_USERNAME, newUsername, true);
		return true;
	}
}