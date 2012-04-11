/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.test.ParameterSets;
import org.ohmage.validator.AuditValidators.ResponseType;

/**
 * Tests the audit validators.
 * 
 * @author John Jenkins
 */
public class AuditValidatorsTest extends TestCase {
	/**
	 * Tests the request type validator.
	 */
	@Test
	public void testValidateRequestType() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateRequestType(emptyValue));
			}
			
			try {
				AuditValidators.validateRequestType("Invalid value.");
				fail("The request type was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			RequestType[] requestTypes = RequestType.values();
			for(int i = 0; i < requestTypes.length; i++) {
				Assert.assertEquals(
						requestTypes[i], 
						AuditValidators.validateRequestType(
								requestTypes[i].toString()
							)
					);
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * Tests the URI validator.
	 */
	@Test
	public void testValidateUri() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateUri(emptyValue));
			}
			
			// Not sure what else to test here as the URI is so liberal.
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the client validator.
	 */
	@Test
	public void testValidateClient() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateClient(emptyValue));
			}
			
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < AuditValidators.MAX_CLIENT_LENGTH; i++) {
				builder.append('a');
			}
			builder.append('b');
			
			try {
				AuditValidators.validateClient(builder.toString());
				fail("The client value was too long.");
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
	 * Tests the device ID validator.
	 */
	@Test
	public void testValidateDeviceId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateDeviceId(emptyValue));
			}
			
			// Not sure what else to test here as a device ID can be about 
			// anything.
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the response type validator.
	 */
	@Test
	public void testValidateResponseType() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateResponseType(emptyValue));
			}
			
			try {
				AuditValidators.validateResponseType("Invalid value.");
				fail("The response type was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			ResponseType[] responseTypes = AuditValidators.ResponseType.values();
			for(int i = 0; i < responseTypes.length; i++) {
				Assert.assertEquals(
						responseTypes[i], 
						AuditValidators.validateResponseType(
								responseTypes[i].toString()
							)
					);
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the error code validator.
	 */
	@Test
	public void testValidateErrorCode() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateErrorCode(emptyValue));
			}
			
			try {
				AuditValidators.validateErrorCode("Invalid value.");
				fail("The error code was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}
			
			ErrorCode[] errorCodes = Annotator.ErrorCode.values();
			for(int i = 0; i < errorCodes.length; i++) {
				Assert.assertEquals(
						errorCodes[i], 
						AuditValidators.validateErrorCode(
								errorCodes[i].toString()
							)
					);
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the start date validator.
	 */
	@Test
	public void testValidateStartDate() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateStartDate(emptyValue));
			}
			
			try {
				AuditValidators.validateStartDate("Invalid value.");
				fail("The start date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<DateTime, String> dateToString = ParameterSets.getDateToString();
			for(DateTime date : dateToString.keySet()) {
				Assert.assertEquals(date, AuditValidators.validateStartDate(dateToString.get(date)));
			}
			
			Map<DateTime, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(DateTime date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, AuditValidators.validateStartDate(dateTimeToString.get(date)));
			}

			Collection<String> invalidDates = ParameterSets.getInvalidDates();
			for(String invalidDate : invalidDates) {
				try {
					AuditValidators.validateStartDate(invalidDate);
					fail("The start date was not a valid date: " + invalidDate);
				}
				catch(ValidationException e) {
					// Passed.
				}
			}

			Collection<String> invalidDateTimes = ParameterSets.getInvalidDateTimes();
			for(String invalidDateTime : invalidDateTimes) {
				try {
					AuditValidators.validateStartDate(invalidDateTime);
					fail("The start date-time was not a valid date-time: " + invalidDateTime);
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
	 * Tests the end date validator.
	 */
	@Test
	public void testValidateEndDate() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(AuditValidators.validateEndDate(emptyValue));
			}
			
			try {
				AuditValidators.validateEndDate("Invalid value.");
				fail("The end date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<DateTime, String> dateToString = ParameterSets.getDateToString();
			for(DateTime date : dateToString.keySet()) {
				Assert.assertEquals(date, AuditValidators.validateEndDate(dateToString.get(date)));
			}
			
			Map<DateTime, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(DateTime date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, AuditValidators.validateEndDate(dateTimeToString.get(date)));
			}

			Collection<String> invalidDates = ParameterSets.getInvalidDates();
			for(String invalidDate : invalidDates) {
				try {
					AuditValidators.validateEndDate(invalidDate);
					fail("The end date was not a valid date: " + invalidDate);
				}
				catch(ValidationException e) {
					// Passed.
				}
			}

			Collection<String> invalidDateTimes = ParameterSets.getInvalidDateTimes();
			for(String invalidDateTime : invalidDateTimes) {
				try {
					AuditValidators.validateEndDate(invalidDateTime);
					fail("The end date-time was not a valid date-time: " + invalidDateTime);
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
