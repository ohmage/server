package org.ohmage.validator;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;

/**
 * Tests the campaign-document validators.
 * 
 * @author John Jenkins
 */
public class CampaignDocumentValidatorsTest extends TestCase {
	private Collection<String> emptyValues;
	
	/**
	 * Sets up the empty string values.
	 */
	public CampaignDocumentValidatorsTest() {
		emptyValues = new LinkedList<String>();
		emptyValues.add(null);
		emptyValues.add("");
		emptyValues.add(" ");
		emptyValues.add("\t");
		emptyValues.add(" \t ");
		emptyValues.add("\n");
		emptyValues.add(" \n ");
	}

	/**
	 * Tests the campaign ID, document role list validator.
	 */
	@Test
	public void testValidateCampaignIdAndDocumentRoleList() {
		try {
			for(String emptyValue : emptyValues) {
				Assert.assertNull(CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(emptyValue));
			}
			
			try {
				CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList("Invalid value.");
				fail("The campaign ID, document role list was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(";").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",,,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(";,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",;").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(";,;").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(";,;,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",;,;").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",;,;,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(";,;,;").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",,;,;,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",;,,;,").size());
			Assert.assertEquals(0, CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(",;,;,,").size());
			
			// The campaign ID and document roles are validated through their 
			// respective validators, so if they pass validation there then the
			// rest of this function passes validation.
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

}
