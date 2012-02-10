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
 * Tests the user, class validators.
 * 
 * @author John Jenkins
 */
public class UserClassValidatorsTest extends TestCase {
	/**
	 * Tests the user, class role validator.
	 */
	@Test
	public void testValidateUserAndClassRoleList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserClassValidators.validateUserAndClassRoleList(emptyValue));
			}
			
			try {
				UserClassValidators.validateUserAndClassRoleList("Invalid value.");
				fail("The username, class role was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUsernameClassRoleList : ParameterSets.getValidUsernameClassRoleLists()) {
				try {
					UserClassValidators.validateUserAndClassRoleList(validUsernameClassRoleList);
				}
				catch(ValidationException e) {
					fail("A valid username, class role '" + validUsernameClassRoleList + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidUsernameClassRoleList : ParameterSets.getInvalidUsernameClassRoleLists()) {
				try {
					UserCampaignValidators.validateUserAndCampaignRole(invalidUsernameClassRoleList);
					fail("An invalid username, class role '" + invalidUsernameClassRoleList + "' passed validation.");
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
