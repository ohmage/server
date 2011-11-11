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