/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request;

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
