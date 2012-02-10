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
 * Tests the visualizations validators.
 * 
 * @author John Jenkins
 */
public class VisualizationValidatorsTest extends TestCase {
	/**
	 * Tests the width validator.
	 */
	@Test
	public void testValidateWidth() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(VisualizationValidators.validateWidth(emptyValue));
			}
			
			try {
				VisualizationValidators.validateWidth("Invalid value.");
				fail("The width was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				VisualizationValidators.validateWidth("-1");
				fail("The width was negative.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Integer oversizedImage = VisualizationValidators.MAX_IMAGE_DIMENSION + 1;
			
			try {
				VisualizationValidators.validateWidth(oversizedImage.toString());
				fail("The width was greater than the maximum allowed dimension.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(new Integer(1), VisualizationValidators.validateWidth((new Integer(1)).toString()));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the height validator.
	 */
	@Test
	public void testValidateHeight() {

		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(VisualizationValidators.validateHeight(emptyValue));
			}
			
			try {
				VisualizationValidators.validateHeight("Invalid value.");
				fail("The width was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				VisualizationValidators.validateHeight("-1");
				fail("The width was negative.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Integer oversizedImage = VisualizationValidators.MAX_IMAGE_DIMENSION + 1;
			
			try {
				VisualizationValidators.validateHeight(oversizedImage.toString());
				fail("The width was greater than the maximum allowed dimension.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(new Integer(1), VisualizationValidators.validateHeight((new Integer(1)).toString()));
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
				Assert.assertNull(VisualizationValidators.validateStartDate(emptyValue));
			}
			
			try {
				VisualizationValidators.validateStartDate("Invalid value.");
				fail("The start date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<Date, String> dateToString = ParameterSets.getDateToString();
			for(Date date : dateToString.keySet()) {
				Assert.assertEquals(date, VisualizationValidators.validateStartDate(dateToString.get(date)));
			}
			
			Map<Date, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(String dateString : dateTimeToString.values()) {
				try {
					VisualizationValidators.validateStartDate(dateString);
					fail("A date-time passed validation when only dates are allowed: " + dateString);
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
				Assert.assertNull(VisualizationValidators.validateEndDate(emptyValue));
			}
			
			try {
				VisualizationValidators.validateEndDate("Invalid value.");
				fail("The start date was an invalid value.");
			}
			catch(ValidationException e) { 
				// Passed.
			}

			Map<Date, String> dateToString = ParameterSets.getDateToString();
			for(Date date : dateToString.keySet()) {
				Assert.assertEquals(date, VisualizationValidators.validateEndDate(dateToString.get(date)));
			}
			
			Map<Date, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(String dateString : dateTimeToString.values()) {
				try {
					VisualizationValidators.validateEndDate(dateString);
					fail("A date-time passed validation when only dates are allowed: " + dateString);
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
