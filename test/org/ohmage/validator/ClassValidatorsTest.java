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

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the class validators.
 * 
 * @author John Jenkins
 */
public class ClassValidatorsTest extends TestCase {
	/**
	 * Tests the class ID validator.
	 */
	@Test
	public void testValidateClassId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassValidators.validateClassId(emptyValue));
			}
			
			try {
				ClassValidators.validateClassId("Invalid value.");
				fail("The class ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUrn : ParameterSets.getValidUrns()) {
				try {
					ClassValidators.validateClassId(validUrn);
				}
				catch(ValidationException e) {
					fail("A valid URN was declared invalid: " + validUrn);
				}
			}
			
			for(String invalidUrn : ParameterSets.getInvalidUrns()) {
				try {
					ClassValidators.validateClassId(invalidUrn);
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
	 * Tests the class ID list validator.
	 */
	@Test
	public void testValidateClassIdList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassValidators.validateClassIdList(emptyValue));
			}
			
			try {
				ClassValidators.validateClassIdList("Invalid value.");
				fail("The class ID list was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(String validUrnList : ParameterSets.getValidUrnLists()) {
				try {
					ClassValidators.validateClassIdList(validUrnList);
				}
				catch(ValidationException e) {
					fail("A valid URN list was declared invalid: " + validUrnList);
				}
			}
			
			for(String invalidUrnList : ParameterSets.getInvalidUrnLists()) {
				try {
					ClassValidators.validateClassIdList(invalidUrnList);
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
	 * Tests the class name validator.
	 */
	@Test
	public void testValidateName() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassValidators.validateName(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < ClassValidators.MAX_NAME_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				ClassValidators.validateName(nameBuilder.toString());
				fail("A name whose length is too long passed validation: " + nameBuilder.toString());
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
	 * Tests the class description validator.
	 */
	@Test
	public void testValidateDescription() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassValidators.validateDescription(emptyValue));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the class role validator.
	 */
	@Test
	public void testValidateClassRole() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ClassValidators.validateClassRole(emptyValue));
			}
			
			try {
				ClassValidators.validateClassRole("Invalid value.");
				fail("The class role was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(Clazz.Role role : Clazz.Role.values()) {
				Assert.assertEquals(role, ClassValidators.validateClassRole(role.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * Tests the class roster validator.
	 */
	@Test
	public void testValidateClassRoster() {
		try {
			Assert.assertNull(ClassValidators.validateClassRoster(null));
			Assert.assertNull(ClassValidators.validateClassRoster(new byte[0]));
			
			try {
				ClassValidators.validateClassRole("Invalid value.");
				fail("The class roster was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			// TODO: Create a roster for testing.
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}
