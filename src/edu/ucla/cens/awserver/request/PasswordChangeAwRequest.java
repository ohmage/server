package edu.ucla.cens.awserver.request;

import java.util.Map;

/**
 * Request builder to change a user's password.
 * 
 * @author John Jenkins
 */
public class PasswordChangeAwRequest extends ResultListAwRequest {
	
	/**
	 * Creates a new password change request and sets the new password to be
	 * validated.
	 * 
	 * @param newPassword The new password for this user.
	 */
	public PasswordChangeAwRequest(String newPassword) throws IllegalArgumentException {
		if(newPassword == null) {
			throw new IllegalArgumentException("Missing required new password.");
		}
			
		Map<String, Object> toValidate = this.getToValidate();
		toValidate.put(InputKeys.NEW_PASSWORD, newPassword);
	}
}
