package org.ohmage.validator;

import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;

/**
 * Tests the campaign-class validators.
 * 
 * @author John Jenkins
 */
public class CampaignClassValidatorsTest {
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
				fail("The request type was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			try {
				CampaignClassValidators.validateClassesAndRoles(",");
				fail("The request type was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}