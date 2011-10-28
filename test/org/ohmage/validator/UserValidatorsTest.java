package org.ohmage.validator;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;

/**
 * Tests user validators.
 * 
 * @author John Jenkins
 */
public class UserValidatorsTest extends TestCase {
	/**
	 * Tests the username validator.
	 */
	@Test
	public void testValidateUsername() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateUsername(emptyValue));
			}
			
			try {
				UserValidators.validateUsername("Invalid value.");
				fail("The username was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUsername : ParameterSets.getValidUsernames()) {
				try {
					UserValidators.validateUsername(validUsername);
				}
				catch(ValidationException e) {
					fail("A valid username '" + validUsername + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidUsername : ParameterSets.getInvalidUsernames()) {
				try {
					UserValidators.validateUsername(invalidUsername);
					fail("An invalid username '" + invalidUsername + "' passed validation.");
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

	/**
	 * Test the username list validator.
	 */
	@Test
	public void testValidateUsernames() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateUsernames(emptyValue));
			}
			
			try {
				UserValidators.validateUsernames("Invalid value.");
				fail("The username list was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUsernameList : ParameterSets.getValidUsernameLists()) {
				try {
					UserValidators.validateUsernames(validUsernameList);
				}
				catch(ValidationException e) {
					fail("A valid username list '" + validUsernameList + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidUsernameList : ParameterSets.getInvalidUsernameLists()) {
				try {
					UserValidators.validateUsernames(invalidUsernameList);
					fail("An invalid username list '" + invalidUsernameList + "' passed validation.");
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

	/**
	 * Test the plain text password validator.
	 */
	@Test
	public void testValidatePlaintextPassword() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validatePlaintextPassword(emptyValue));
			}
			
			try {
				UserValidators.validatePlaintextPassword("Invalid value.");
				fail("The plaintext password was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validPassword : ParameterSets.getValidPlainTextPasswords()) {
				try {
					UserValidators.validatePlaintextPassword(validPassword);
				}
				catch(ValidationException e) {
					fail("A valid password '" + validPassword + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidPassword : ParameterSets.getInvalidPlainTextPasswords()) {
				try {
					UserValidators.validatePlaintextPassword(invalidPassword);
					fail("An invalid password '" + invalidPassword + "' passed validation.");
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

	/**
	 * Tests the hashed password validator.
	 */
	@Test
	public void testValidateHashedPassword() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateHashedPassword(emptyValue));
			}
			
			try {
				UserValidators.validateHashedPassword("Invalid value.");
				fail("The plaintext password was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validHashedPassword : ParameterSets.getValidHashedPasswords()) {
				try {
					UserValidators.validateHashedPassword(validHashedPassword);
				}
				catch(ValidationException e) {
					fail("A valid hashed password '" + validHashedPassword + "' failed validation: " + e.getMessage());
				}
			}
			
			for(String invalidHashedPassword : ParameterSets.getInvalidHashedPasswords()) {
				try {
					UserValidators.validateHashedPassword(invalidHashedPassword);
					fail("An invalid hashed password '" + invalidHashedPassword + "' passed validation.");
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

	/**
	 * Tests the admin valid validator.
	 */
	@Test
	public void testValidateAdminValue() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateAdminValue(emptyValue));
			}
			
			try {
				UserValidators.validateAdminValue("Invalid value.");
				fail("The admin value is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, UserValidators.validateAdminValue("true"));
			Assert.assertEquals(false, UserValidators.validateAdminValue("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the enabled value validator.
	 */
	@Test
	public void testValidateEnabledValue() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateEnabledValue(emptyValue));
			}
			
			try {
				UserValidators.validateEnabledValue("Invalid value.");
				fail("The enabled value is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, UserValidators.validateEnabledValue("true"));
			Assert.assertEquals(false, UserValidators.validateEnabledValue("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the new account value validator.
	 */
	@Test
	public void testValidateNewAccountValue() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateNewAccountValue(emptyValue));
			}
			
			try {
				UserValidators.validateNewAccountValue("Invalid value.");
				fail("The new account value is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, UserValidators.validateNewAccountValue("true"));
			Assert.assertEquals(false, UserValidators.validateNewAccountValue("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the campaign creation privilege value validator.
	 */
	@Test
	public void testValidateCampaignCreationPrivilegeValue() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateCampaignCreationPrivilegeValue(emptyValue));
			}
			
			try {
				UserValidators.validateCampaignCreationPrivilegeValue("Invalid value.");
				fail("The campaign creation value is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, UserValidators.validateCampaignCreationPrivilegeValue("true"));
			Assert.assertEquals(false, UserValidators.validateCampaignCreationPrivilegeValue("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the first name value validator.
	 */
	@Test
	public void testValidateFirstName() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateFirstName(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < UserValidators.MAX_LAST_NAME_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				UserValidators.validateFirstName(nameBuilder.toString());
				fail("A first name that was too long passed validation");
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the last name value validator.
	 */
	@Test
	public void testValidateLastName() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateLastName(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < UserValidators.MAX_LAST_NAME_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				UserValidators.validateLastName(nameBuilder.toString());
				fail("A last name that was too long passed validation");
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the organization value validator.
	 */
	@Test
	public void testValidateOrganization() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateOrganization(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < UserValidators.MAX_ORGANIZATION_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				UserValidators.validateOrganization(nameBuilder.toString());
				fail("An organization that was too long passed validation");
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the personal ID value validator.
	 */
	@Test
	public void testValidatePersonalId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validatePersonalId(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < UserValidators.MAX_PERSONAL_ID_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				UserValidators.validatePersonalId(nameBuilder.toString());
				fail("An organization that was too long passed validation");
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the email address validator.
	 */
	@Test
	public void testValidateEmailAddress() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateEmailAddress(emptyValue));
			}
			
			try {
				UserValidators.validateEmailAddress("Invalid value.");
				fail("The email address value is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("@a.aa");
				fail("Local part missing.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a@.aa");
				fail("Domain is missing.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a@a");
				fail("TLD is missing.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			 
			try {
				UserValidators.validateEmailAddress("a(@a.aa");
				fail("Invalid character, '(', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a)@a.aa");
				fail("Invalid character, ')', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a,@a.aa");
				fail("Invalid character, ',', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a:@a.aa");
				fail("Invalid character, ':', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			 
			try {
				UserValidators.validateEmailAddress("a;@a.aa");
				fail("Invalid character, ';', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a<@a.aa");
				fail("Invalid character, '<', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a>@a.aa");
				fail("Invalid character, '>', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a[@a.aa");
				fail("Invalid character, '[', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a]@a.aa");
				fail("Invalid character, ']', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a\\@a.aa");
				fail("Invalid character, '\\', present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a@@a.aa");
				fail("Multiple '@' symbols present.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress(".a@a.aa");
				fail("Begins with a period, '.'.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a.@a.aa");
				fail("Ends with a period, '.'.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a..a@a.aa");
				fail("Multiple consecutive periods exist.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			/*
			try {
				UserValidators.validateEmailAddress("12345678901234567890123456789012345678901234567890123456789012345@a.aa");
				fail("Local part is too long.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("a@a.345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234");
				fail("Domain is too long.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateEmailAddress("1234567890123456789012345678901234567890123456789012345678901234@a.34567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123");
				fail("Local part and domain are both within their length bounds, but the entire address is still too long.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			*/

			try {
				UserValidators.validateEmailAddress("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@a.aa");
			}
			catch(ValidationException e) {
				fail("Using only valid characters, the email verification still failed.");
			}
			
			/*
			try {
				UserValidators.validateEmailAddress("'`~!#$%^&*+\\-/=?{|}@a.aa");
			}
			catch(ValidationException e) {
				fail("Using only valid characters, the email verification still failed.");
			}
			*/
			
			try {
				UserValidators.validateEmailAddress("a.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@a.aa");
			}
			catch(ValidationException e) {
				fail("Using only valid characters after a period, '.', the email verification still failed.");
			}
			
			/*
			try {
				UserValidators.validateEmailAddress("a.'`~!#$%^&*+@a.aa");
			}
			catch(ValidationException e) {
				fail("Using only valid characters after a period, '.', the email verification still failed.");
			}
			*/
			
			try {
				UserValidators.validateEmailAddress("a@a.aa");
			}
			catch(ValidationException e) {
				fail("The shortest, simplest email address failed.");
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the JSON data validator.
	 */
	@Test
	public void testValidateJsonData() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(UserValidators.validateJsonData(emptyValue));
			}
			
			try {
				UserValidators.validateJsonData("Invalid value.");
				fail("The JSON data is invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				UserValidators.validateJsonData((new JSONObject()).toString());
			}
			catch(ValidationException e) {
				fail("Valid JSON data failed validation.");
			} 
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}