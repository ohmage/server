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