package org.ohmage.validator;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the campaign-document validators.
 * 
 * @author John Jenkins
 */
public class CampaignDocumentValidatorsTest extends TestCase {
	/**
	 * Tests the campaign ID, document role list validator.
	 */
	@Test
	public void testValidateCampaignIdAndDocumentRoleList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(emptyValue));
			}
			
			try {
				CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList("Invalid value.");
				fail("The campaign ID, document role list was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			for(String validUrnDocumentRoleList : ParameterSets.getValidUrnDocumentRoleLists()) {
				try {
					CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(validUrnDocumentRoleList);
				}
				catch(ValidationException e) {
					fail("A valid URN, campaign role list failed validation: " + validUrnDocumentRoleList);
				}
			}
			
			for(String invalidUrnDocumentRoleList : ParameterSets.getInvalidUrnDocumentRoleLists()) {
				try {
					CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(invalidUrnDocumentRoleList);
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