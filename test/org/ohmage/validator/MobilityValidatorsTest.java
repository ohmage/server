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

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the Mobility validators.
 * 
 * @author John Jenkins
 */
public class MobilityValidatorsTest extends TestCase {
	@Test
	public void testValidateDataAsJsonArray() {
		// TODO: Finish implementing this test.
		//fail("Not yet implemented");
	}

	/**
	 * Tests the date validator.
	 */
	@Test
	public void testValidateDate() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(MobilityValidators.validateDate(emptyValue));
			}
			
			try {
				MobilityValidators.validateDate("Invalid value.");
				fail("The date was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}

			Map<Date, String> dateToString = ParameterSets.getDateToString();
			for(Date date : dateToString.keySet()) {
				Assert.assertEquals(date, MobilityValidators.validateDate(dateToString.get(date)));
			}
			
			Map<Date, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(Date date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, MobilityValidators.validateDate(dateTimeToString.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}
