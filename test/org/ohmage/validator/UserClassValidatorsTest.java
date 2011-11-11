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