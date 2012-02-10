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
 * Tests the campaign-class validators.
 * 
 * @author John Jenkins
 */
public class CampaignClassValidatorsTest extends TestCase {
	/**
	 * Tests the class ID, campaign role list validator.
	 */
	@Test
	public void testValidateClassesAndRoles() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignClassValidators.validateClassesAndRoles(emptyValue));
			}
			
			try {
				CampaignClassValidators.validateClassesAndRoles("Invalid value.");
				fail("The class ID, campaign role list was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			for(String validUrnCampaignRoleList : ParameterSets.getValidUrnCampaignRoleLists()) {
				try {
					CampaignClassValidators.validateClassesAndRoles(validUrnCampaignRoleList);
				}
				catch(ValidationException e) {
					fail("A valid URN, campaign role list was flagged invalid: " + validUrnCampaignRoleList);
				}
			}
			
			for(String invalidUrnCampaignRoleList : ParameterSets.getInvalidUrnCampaignRoleLists()) {
				try {
					CampaignClassValidators.validateClassesAndRoles(invalidUrnCampaignRoleList);
					fail("An invalid URN, campaign role list was flagged valid: " + invalidUrnCampaignRoleList);
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
