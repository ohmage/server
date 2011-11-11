package org.ohmage.validator;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the campaign validators.
 * 
 * @author John Jenkins
 */
public class CampaignValidatorsTest extends TestCase {
	/**
	 * Tests the campaign ID validator.
	 */
	@Test
	public void testValidateCampaignId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateCampaignId(emptyValue));
			}
			
			try {
				CampaignValidators.validateCampaignId("Invalid value.");
				fail("The campaign ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUrn : ParameterSets.getValidUrns()) {
				try {
					CampaignValidators.validateCampaignId(validUrn);
				}
				catch(ValidationException e) {
					fail("A valid URN was declared invalid: " + validUrn);
				}
			}
			
			for(String invalidUrn : ParameterSets.getInvalidUrns()) {
				try {
					CampaignValidators.validateCampaignId(invalidUrn);
					fail("An invalid URN passed validation: " + invalidUrn);
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
	 * Test the campaign ID list validator.
	 */
	@Test
	public void testValidateCampaignIds() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateCampaignIds(emptyValue));
			}
			
			try {
				CampaignValidators.validateCampaignIds("Invalid value.");
				fail("The campaign ID list was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUrnList : ParameterSets.getValidUrnLists()) {
				try {
					CampaignValidators.validateCampaignIds(validUrnList);
				}
				catch(ValidationException e) {
					fail("A valid URN list was declared invalid: " + validUrnList);
				}
			}
			
			for(String invalidUrnList : ParameterSets.getInvalidUrnLists()) {
				try {
					CampaignValidators.validateCampaignIds(invalidUrnList);
					fail("An invalid URN list passed validation: " + invalidUrnList);
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
	 * Test the campaign running state validator.
	 */
	@Test
	public void testValidateRunningState() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateRunningState(emptyValue));
			}
			
			try {
				CampaignValidators.validateRunningState("Invalid value.");
				fail("The campaign running state was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Campaign.RunningState[] runningStates = Campaign.RunningState.values();
			for(int i = 0; i < runningStates.length; i++) {
				Assert.assertEquals(runningStates[i], CampaignValidators.validateRunningState(runningStates[i].toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the campaign privacy state validator.
	 */
	@Test
	public void testValidatePrivacyState() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validatePrivacyState(emptyValue));
			}
			
			try {
				CampaignValidators.validatePrivacyState("Invalid value.");
				fail("The campaign privacy state was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Campaign.PrivacyState[] privacyStates = Campaign.PrivacyState.values();
			for(int i = 0; i < privacyStates.length; i++) {
				Assert.assertEquals(privacyStates[i], CampaignValidators.validatePrivacyState(privacyStates[i].toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	@Test
	public void testValidateXml() {
		// TODO: Finish implementing the test.
		//fail("Not yet implemented");
	}
	
	/**
	 * Test the campaign description validator.
	 */
	@Test
	public void testValidateDescription() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateDescription(emptyValue));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the output format validator.
	 */
	@Test
	public void testValidateOutputFormat() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateOutputFormat(emptyValue));
			}
			
			try {
				CampaignValidators.validateOutputFormat("Invalid value.");
				fail("The campaign output format was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Campaign.OutputFormat[] outputFormats = Campaign.OutputFormat.values();
			for(int i = 0; i < outputFormats.length; i++) {
				Assert.assertEquals(outputFormats[i], CampaignValidators.validateOutputFormat(outputFormats[i].toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the start date validator.
	 */
	@Test
	public void testValidateStartDate() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateStartDate(emptyValue));
			}
			
			try {
				CampaignValidators.validateStartDate("Invalid value.");
				fail("The campaign start date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<Date, String> dateToString = ParameterSets.getDateToString();
			for(Date date : dateToString.keySet()) {
				Assert.assertEquals(date, CampaignValidators.validateStartDate(dateToString.get(date)));
			}
			
			Map<Date, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(Date date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, CampaignValidators.validateStartDate(dateTimeToString.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the end date validator.
	 */
	@Test
	public void testValidateEndDate() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateEndDate(emptyValue));
			}
			
			try {
				CampaignValidators.validateEndDate("Invalid value.");
				fail("The campaign end date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<Date, String> dateToString = ParameterSets.getDateToString();
			for(Date date : dateToString.keySet()) {
				Assert.assertEquals(date, CampaignValidators.validateEndDate(dateToString.get(date)));
			}
			
			Map<Date, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(Date date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, CampaignValidators.validateEndDate(dateTimeToString.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the campaign role validator.
	 */
	@Test
	public void testValidateRole() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateRole(emptyValue));
			}
			
			try {
				CampaignValidators.validateRole("Invalid value.");
				fail("The campaign role was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Campaign.Role[] roles = Campaign.Role.values();
			for(int i = 0; i < roles.length; i++) {
				Assert.assertEquals(roles[i], CampaignValidators.validateRole(roles[i].toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the prompt ID validator.
	 */
	@Test
	public void testValidatePromptId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validatePromptId(emptyValue));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Test the uploaded JSON validator.
	 */
	@Test
	public void testValidateUploadedJson() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(CampaignValidators.validateUploadedJson(emptyValue));
			}

			try {
				CampaignValidators.validateUploadedJson("Invalid value.");
				fail("The uploaded JSON was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			JSONArray jsonArray = new JSONArray();
			
			try {
				CampaignValidators.validateUploadedJson(jsonArray.toString());
			}
			catch(ValidationException e) {
				fail("The empty JSONArray was rejected: " + e.getMessage());
			}
			
			jsonArray.put(new JSONObject());
			
			try {
				CampaignValidators.validateUploadedJson(jsonArray.toString());
			}
			catch(ValidationException e) {
				fail("The JSONArray with one JSONObject was rejected: " + e.getMessage());
			}
			
			jsonArray.put(new JSONObject());
			
			try {
				CampaignValidators.validateUploadedJson(jsonArray.toString());
			}
			catch(ValidationException e) {
				fail("The JSONArray with two JSONObjects was rejected: " + e.getMessage());
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}