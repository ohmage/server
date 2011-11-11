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