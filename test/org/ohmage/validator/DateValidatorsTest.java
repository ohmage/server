package org.ohmage.validator;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;

/**
 * Tests the date validators.
 * 
 * @author John Jenkins
 */
public class DateValidatorsTest extends TestCase {
	/**
	 * Tests the date validator.
	 */
	@Test
	public void testValidateISO8601Date() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DateValidators.validateISO8601Date(emptyValue));
			}
			
			try {
				DateValidators.validateISO8601Date("Invalid value.");
				fail("The date was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Map<Date, String> dateToStringMap = ParameterSets.getDateToString();
			for(Date date : dateToStringMap.keySet()) {
				Assert.assertEquals(date, DateValidators.validateISO8601Date(dateToStringMap.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the date-time validator.
	 */
	@Test
	public void testValidateISO8601DateTime() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DateValidators.validateISO8601DateTime(emptyValue));
			}
			
			try {
				DateValidators.validateISO8601DateTime("Invalid value.");
				fail("The date-time was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Map<Date, String> dateTimeToStringMap = ParameterSets.getDateTimeToString();
			for(Date date : dateTimeToStringMap.keySet()) {
				Assert.assertEquals(date, DateValidators.validateISO8601DateTime(dateTimeToStringMap.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

}
