/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.validator;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the user, document validators.
 * 
 * @author John Jenkins
 */
public class UserDocumentValidatorsTest extends TestCase {
	/**
	 * Tests the username, document role validator.
	 */
	@Test
	public void testValidateUsernameAndDocumentRoleList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserDocumentValidators.validateUsernameAndDocumentRoleList(emptyValue));
			}
			
			try {
				UserDocumentValidators.validateUsernameAndDocumentRoleList("Invalid value.");
				fail("The username, class role was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUsernameDocumentRoleList : ParameterSets.getValidUsernameDocumentRoleLists()) {
				try {
					UserDocumentValidators.validateUsernameAndDocumentRoleList(validUsernameDocumentRoleList);
				}
				catch(ValidationException e) {
					fail("A valid username, class role '" + validUsernameDocumentRoleList + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidUsernameDocumentRoleList : ParameterSets.getInvalidUsernameDocumentRoleLists()) {
				try {
					UserDocumentValidators.validateUsernameAndDocumentRoleList(invalidUsernameDocumentRoleList);
					fail("An invalid username, class role '" + invalidUsernameDocumentRoleList + "' passed validation.");
				}
				catch(ValidationException e) {
					// Passed.
				}
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}
