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

import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

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
			
			Map<DateTime, String> dateToStringMap = ParameterSets.getDateToString();
			for(DateTime date : dateToStringMap.keySet()) {
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
			
			Map<DateTime, String> dateTimeToStringMap = ParameterSets.getDateTimeToString();
			for(DateTime date : dateTimeToStringMap.keySet()) {
				Assert.assertEquals(date, DateValidators.validateISO8601DateTime(dateTimeToStringMap.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}
