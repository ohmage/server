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
 * Tests the user, campaign validators.
 * 
 * @author John Jenkins
 */
public class UserCampaignValidatorsTest extends TestCase {
	/**
	 * Tests the user, campaign role validator.
	 */
	@Test
	public void testValidateUserAndCampaignRole() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserCampaignValidators.validateUserAndCampaignRole(emptyValue));
			}
			
			try {
				UserCampaignValidators.validateUserAndCampaignRole("Invalid value.");
				fail("The username, campaign role was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUsernameCampaignRoleList : ParameterSets.getValidUsernameCampaignRoleLists()) {
				try {
					UserCampaignValidators.validateUserAndCampaignRole(validUsernameCampaignRoleList);
				}
				catch(ValidationException e) {
					fail("A valid username, campaign role failed validation.");
				}
			}
			
			for(String invalidUsernameCampaignRoleList : ParameterSets.getInvalidUsernameCampaignRoleLists()) {
				try {
					UserCampaignValidators.validateUserAndCampaignRole(invalidUsernameCampaignRoleList);
					fail("An invalid username, campaign role passed validation.");
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
