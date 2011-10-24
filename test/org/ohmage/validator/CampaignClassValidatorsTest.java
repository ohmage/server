package org.ohmage.validator;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;

/**
 * Tests the campaign-class validators.
 * 
 * @author John Jenkins
 */
public class CampaignClassValidatorsTest extends TestCase {
	private Collection<String> emptyValues;
	
	/**
	 * Sets up the empty string values.
	 */
	public CampaignClassValidatorsTest() {
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
	 * Tests the class ID, campaign role list validator.
	 */
	@Test
	public void testValidateClassesAndRoles() {
		try {
			for(String emptyValue : emptyValues) {
				Assert.assertNull(CampaignClassValidators.validateClassesAndRoles(emptyValue));
			}
			
			try {
				CampaignClassValidators.validateClassesAndRoles("Invalid value.");
				fail("The class ID, campaign role list was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(";").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",,,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(";,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",;").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(";,;").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(";,;,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",;,;").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",;,;,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(";,;,;").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",,;,;,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",;,,;,").size());
			Assert.assertEquals(0, CampaignClassValidators.validateClassesAndRoles(",;,;,,").size());
			
			// The class ID and campaign roles are validated through their 
			// respective validators, so if they pass validation then the rest
			// of this function passes validation.
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}