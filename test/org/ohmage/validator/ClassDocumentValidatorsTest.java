package org.ohmage.validator;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the class-document validators.
 * 
 * @author John Jenkins
 */
public class ClassDocumentValidatorsTest extends TestCase {
	/**
	 * Tests the class ID, document role validator.
	 */
	@Test
	public void testValidateClassIdAndDocumentRoleList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassDocumentValidators.validateClassIdAndDocumentRoleList(emptyValue));
			}
			
			try {
				ClassDocumentValidators.validateClassIdAndDocumentRoleList("Invalid value.");
				fail("The campaign ID, document role list was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			for(String validUrnDocumentRoleList : ParameterSets.getValidUrnDocumentRoleLists()) {
				try {
					ClassDocumentValidators.validateClassIdAndDocumentRoleList(validUrnDocumentRoleList);
				}
				catch(ValidationException e) {
					fail("A valid URN, campaign role list failed validation: " + validUrnDocumentRoleList);
				}
			}
			
			for(String invalidUrnDocumentRoleList : ParameterSets.getInvalidUrnDocumentRoleLists()) {
				try {
					ClassDocumentValidators.validateClassIdAndDocumentRoleList(invalidUrnDocumentRoleList);
					fail("An invalid URN, campaign role list passed validation: " + invalidUrnDocumentRoleList);
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