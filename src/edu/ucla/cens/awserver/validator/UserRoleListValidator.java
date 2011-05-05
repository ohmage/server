package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Validates that all the usernames and roles in the request are valid
 * usernames and roles respectively.
 * 
 * @author John Jenkins
 */
public class UserRoleListValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(UserRoleListValidator.class);
	
	private AwRequestAnnotator _invalidUsernameAnnotator;
	
	private String _key;
	private boolean _required;
	
	/**
	 * Builds the validator with a general annotator if there is an unknown
	 * error and an invalid username annotator that is used if any of the
	 * usernames in the list are invalid.
	 * 
	 * It is also created with a regular expression that is used to check the
	 * usernames, a key that is used to retrieve the list from the toValidate
	 * map, and a switch as to whether the key must exist in the toValidate 
	 * array.
	 * 
	 * @param generalAnnotator A general annotator used when there is an
	 * 						   unknown error.
	 * 
	 * @param invalidUsernameAnnotator An annotator to use when one of the
	 * 								   usernames in the request is invalid.
	 * 
	 * @param regexp A regular expression to be used to check the usernames.
	 * 
	 * @param key The key that indicates which value in the toValidate array
	 * 			  contains the list.
	 * 
	 * @param required Whether or not this validation is required if the value
	 * 				   doesn't exist in the map.
	 */
	public UserRoleListValidator(AwRequestAnnotator generalAnnotator, AwRequestAnnotator invalidUsernameAnnotator, String regexp, String key, boolean required) {
		super(regexp, generalAnnotator);
		
		if(invalidUsernameAnnotator == null) {
			throw new IllegalArgumentException("An invalidUsername annotator is required.");
		}
		else if(key == null) {
			throw new IllegalArgumentException("A key is required from the list of InputKeys.");
		}
		
		_invalidUsernameAnnotator = invalidUsernameAnnotator;
		
		_key = key;
		_required = required;
	}

	/**
	 * Checks each user-role pair one-at-a-time ensuring that the username is
	 * a valid username and that the role is a valid role.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the list of users and their roles for key: " + _key);
		
		// Attempt to get the value.
		String userAndRoleList;
		try {
			userAndRoleList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			// It wasn't in the toProcess map meaning someone else has already
			// tried to validate it, so I am falling back to the toValidate
			// map.
			userAndRoleList = (String) awRequest.getToValidate().get(_key);
			
			if(userAndRoleList == null) {
				// It didn't exist in the toValidate map.
				if(_required) {
					// This is a logical error because we shouldn't have
					// accepted the request without the parameter or we
					// forgot to add it to the correct map.
					throw new ValidatorException("Expcected value for key " + _key + " didn't exist in either the toProcess or toValidate maps.");
				}
				else
				{
					// It doesn't exist and wasn't required.
					return true;
				}
			}
		}
		
		// Split all the username-role couples into their own entities.
		String[] userAndRoleArray = userAndRoleList.split(",");
		for(int i = 0; i < userAndRoleArray.length; i++) {
			String userAndRole = userAndRoleArray[i];
			
			// Check that each entity contains exactly one user and exactly
			// one role.
			String[] userAndRoleSplit = userAndRole.split(":");
			if(userAndRoleSplit.length != 2) {
				getAnnotator().annotate(awRequest, "Invalid " + _key + " value at index: " + i);
				awRequest.setFailedRequest(true);
				return false;
			}
			
			// Validate user.
			String user = userAndRoleSplit[0];
			if(! _regexpPattern.matcher(user).matches()) {
				_invalidUsernameAnnotator.annotate(awRequest, "Invalid username in request at index: " + i);
				awRequest.setFailedRequest(true);
				return false;
			}
			
			// Validate role.
			// No validation on the role is done here. Instead we just ensure
			// that the form of the requests makes sense and that the format
			// of the usernames is acceptable.
		}
		
		awRequest.addToProcess(_key, userAndRoleList, true);
		return true;
	}
}