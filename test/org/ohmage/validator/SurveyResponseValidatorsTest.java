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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.test.ParameterSets;

/**
 * Tests the survey response validators.
 * 
 * @author John Jenkins
 */
public class SurveyResponseValidatorsTest extends TestCase {
	/**
	 * Tests the survey ID validator.
	 */
	@Test
	public void testValidateSurveyIds() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateSurveyIds(emptyValue));
			}
			
			for(String simpleValidList : ParameterSets.getSimpleValidLists()) {
				try {
					SurveyResponseValidators.validateSurveyIds(simpleValidList);
				}
				catch(ValidationException e) {
					fail("A valid list failed validation.");
				}
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the prompt ID validator.
	 */
	@Test
	public void testValidatePromptIds() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validatePromptIds(emptyValue));
			}
			
			for(String simpleValidList : ParameterSets.getSimpleValidLists()) {
				try {
					SurveyResponseValidators.validatePromptIds(simpleValidList);
				}
				catch(ValidationException e) {
					fail("A valid list failed validation.");
				}
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the survey response ID validator.
	 */
	@Test
	public void testValidateSurveyResponseId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateSurveyResponseId(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateSurveyResponseId("Invalid value.");
				fail("The survey response ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			// This may be any long value as it is simply a database ID.
			UUID uuid = UUID.randomUUID();
			Assert.assertEquals(uuid, SurveyResponseValidators.validateSurveyResponseId(uuid.toString()));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the privacy state validator.
	 */
	@Test
	public void testValidatePrivacyState() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validatePrivacyState(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validatePrivacyState("Invalid value.");
				fail("The privacy state was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(SurveyResponse.PrivacyState privacyState: SurveyResponse.PrivacyState.values()) {
				Assert.assertEquals(privacyState, SurveyResponseValidators.validatePrivacyState(privacyState.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the username list validator.
	 */
	@Test
	public void testValidateUsernames() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateUsernames(emptyValue));
			}

			for(String validUsernameList : ParameterSets.getValidUsernameLists()) {
				try {
					SurveyResponseValidators.validateUsernames(validUsernameList);
				}
				catch(ValidationException e) {
					fail("A validation exception was thrown for a valid username list: " + validUsernameList);
				}
				
				String validUsernameListWithAll = validUsernameList + InputKeys.LIST_ITEM_SEPARATOR + SurveyResponseReadRequest.URN_SPECIAL_ALL;
				try {
					Set<String> usernameList = SurveyResponseValidators.validateUsernames(validUsernameListWithAll);
					if(usernameList.size() == 1) {
						if(! SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(usernameList.iterator().next())) {
							fail("The username list contained '" + 
									SurveyResponseReadRequest.URN_SPECIAL_ALL +
									"', and the validator only returned one element, but it was not the expected element: " + 
									usernameList.iterator().next());
						}
					}
					else {
						fail("The username list contained '" + SurveyResponseReadRequest.URN_SPECIAL_ALL + "' but returned more than one result: " + usernameList);
					}
				}
				catch(ValidationException e) {
					fail("A validation exception was thrown for a valid username list: " + validUsernameListWithAll);
				}

				String allWithValidUsernameList = SurveyResponseReadRequest.URN_SPECIAL_ALL + InputKeys.LIST_ITEM_SEPARATOR + validUsernameList;
				try {
					Set<String> usernameList = SurveyResponseValidators.validateUsernames(allWithValidUsernameList);
					if(usernameList.size() == 1) {
						if(! SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(usernameList.iterator().next())) {
							fail("The username list contained '" + 
									SurveyResponseReadRequest.URN_SPECIAL_ALL +
									"', and the validator only returned one element, but it was not the expected element: " + 
									usernameList.iterator().next());
						}
					}
					else {
						fail("The username list contained '" + SurveyResponseReadRequest.URN_SPECIAL_ALL + "' but returned more than one result: " + usernameList);
					}
				}
				catch(ValidationException e) {
					fail("A validation exception was thrown for a valid username list: " + allWithValidUsernameList);
				}
			}
			
			for(String invalidUsernameList : ParameterSets.getInvalidUsernameLists()) {
				try {
					SurveyResponseValidators.validateUsernames(invalidUsernameList);
					fail("An invalid username list passed validation: " + invalidUsernameList);
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
	 * Tests the column list validator.
	 */
	@Test
	public void testValidateColumnList() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateColumnList(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateColumnList("Invalid value.");
				fail("The column list was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			try {
				Set<SurveyResponse.ColumnKey> columnList = 
					SurveyResponseValidators.validateColumnList(InputKeys.LIST_ITEM_SEPARATOR);
				
				if(columnList.size() != 0) {
					fail("The column list was not empty: " + columnList);
				}
			}
			catch(ValidationException e) {
				fail("A list containing only a '" + InputKeys.LIST_ITEM_SEPARATOR + "' threw an exception: " + e.getMessage());
			}
			
			try {
				Set<SurveyResponse.ColumnKey> columnList = 
					SurveyResponseValidators.validateColumnList(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
				
				if(columnList.size() != 0) {
					fail("The column list was not empty: " + columnList);
				}
			}
			catch(ValidationException e) {
				fail("A list containing two '" + InputKeys.LIST_ITEM_SEPARATOR + "'s threw an exception: " + e.getMessage());
			}
			
			try {
				Set<SurveyResponse.ColumnKey> columnList = 
					SurveyResponseValidators.validateColumnList(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
				
				if(columnList.size() != 0) {
					fail("The column list was not empty: " + columnList);
				}
			}
			catch(ValidationException e) {
				fail("A list containing three '" + InputKeys.LIST_ITEM_SEPARATOR + "'s threw an exception: " + e.getMessage());
			}

			int columnKeysAdded = 0;
			StringBuilder columnListBuilder = new StringBuilder();
			for(SurveyResponse.ColumnKey columnKey : SurveyResponse.ColumnKey.values()) {
				Set<SurveyResponse.ColumnKey> columnList =
					SurveyResponseValidators.validateColumnList(columnKey.toString());
				
				if(columnList.size() != 1) {
					fail("The column key was not returned: " + columnList.size());
				}
				else {
					if(! columnKey.equals(columnList.iterator().next())) {
						fail("The column key returned was not the one we gave: " + columnList.iterator().next().toString());
					}
				}
				
				columnListBuilder.append(columnKey + InputKeys.LIST_ITEM_SEPARATOR);
				columnKeysAdded++;
				columnList = SurveyResponseValidators.validateColumnList(columnListBuilder.toString());
				if(columnList.size() != columnKeysAdded) {
					fail("The list of column keys returned doesn't match the number we have added.");
				}
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the output format validator.
	 */
	@Test
	public void testValidateOutputFormat() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateOutputFormat(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateOutputFormat("Invalid value.");
				fail("The survey response output format was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(SurveyResponse.OutputFormat outputFormat : SurveyResponse.OutputFormat.values()) {
				Assert.assertEquals(outputFormat, SurveyResponseValidators.validateOutputFormat(outputFormat.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the sort order validator.
	 */
	@Test
	public void testValidateSortOrder() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateSortOrder(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateSortOrder("Invalid value.");
				fail("The survey response sort order was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Set<List<SortParameter>> permutations = 
				getUniqueSets(Arrays.asList(SortParameter.values()));
			
			for(List<SortParameter> permutation : permutations) {
				int length = permutation.size();
				StringBuilder permutationStringBuilder = new StringBuilder();
				
				int count = 1;
				for(SortParameter sortParameter : permutation) {
					permutationStringBuilder.append(sortParameter.toString());
					
					if(count != length) {
						permutationStringBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
					}
				}
				
				String permutationString = permutationStringBuilder.toString();
				
				Assert.assertEquals(permutation, SurveyResponseValidators.validateSortOrder(permutationString));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the function validator.
	 */
	@Test
	public void testValidateFunction() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateFunction(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateFunction("Invalid value.");
				fail("The survey response function was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(SurveyResponse.Function function : SurveyResponse.Function.values()) {
				Assert.assertEquals(function, SurveyResponseValidators.validateFunction(function.toString()));
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
				Assert.assertNull(SurveyResponseValidators.validateStartDate(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateStartDate("Invalid value.");
				fail("The start date was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}

			Map<DateTime, String> dateToString = ParameterSets.getDateToString();
			for(DateTime date : dateToString.keySet()) {
				Assert.assertEquals(date, SurveyResponseValidators.validateStartDate(dateToString.get(date)));
			}
			
			Map<DateTime, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(DateTime date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, SurveyResponseValidators.validateStartDate(dateTimeToString.get(date)));
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
				Assert.assertNull(SurveyResponseValidators.validateEndDate(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateEndDate("Invalid value.");
				fail("The end date was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}

			Map<DateTime, String> dateToString = ParameterSets.getDateToString();
			for(DateTime date : dateToString.keySet()) {
				Assert.assertEquals(date, SurveyResponseValidators.validateEndDate(dateToString.get(date)));
			}
			
			Map<DateTime, String> dateTimeToString = ParameterSets.getDateTimeToString();
			for(DateTime date : dateTimeToString.keySet()) {
				Assert.assertEquals(date, SurveyResponseValidators.validateEndDate(dateTimeToString.get(date)));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the suppress metadata value validator.
	 */
	@Test
	public void testValidateSuppressMetadata() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateSuppressMetadata(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateSuppressMetadata("Invalid value.");
				fail("The suppress metadata value was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, SurveyResponseValidators.validateSuppressMetadata("true"));
			Assert.assertEquals(false, SurveyResponseValidators.validateSuppressMetadata("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the return ID value validator.
	 */
	@Test
	public void testValidateReturnId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateReturnId(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateReturnId("Invalid value.");
				fail("The return ID value was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, SurveyResponseValidators.validateReturnId("true"));
			Assert.assertEquals(false, SurveyResponseValidators.validateReturnId("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the pretty print value validator.
	 */
	@Test
	public void testValidatePrettyPrint() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validatePrettyPrint(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validatePrettyPrint("Invalid value.");
				fail("The pretty print value was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, SurveyResponseValidators.validatePrettyPrint("true"));
			Assert.assertEquals(false, SurveyResponseValidators.validatePrettyPrint("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the collapse value validator.
	 */
	@Test
	public void testValidateCollapse() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(SurveyResponseValidators.validateCollapse(emptyValue));
			}
			
			try {
				SurveyResponseValidators.validateCollapse("Invalid value.");
				fail("The collapse value was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, SurveyResponseValidators.validateCollapse("true"));
			Assert.assertEquals(false, SurveyResponseValidators.validateCollapse("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
	
	/**
	 * Generates all of the permutations of the given list of SortParameter
	 * parameters.
	 * 
	 * @param parameters The list of sort parameters.
	 * 
	 * @return A set of lists where each list is a unique permutation of the
	 * 		   sort parameters.
	 * 
	 * @complexity O(n^2) where 'n' is the number of sort order parameters in 
	 * 			   the list.
	 */
	private Set<List<SortParameter>> getUniqueSets(final List<SortParameter> parameters) {
		// This should never happen.
		if(parameters.size() == 0) {
			return new HashSet<List<SortParameter>>();
		}
		
		// Base case.
		if(parameters.size() == 1) {
			List<SortParameter> singleList = new ArrayList<SortParameter>(1);
			singleList.add(parameters.iterator().next());
			HashSet<List<SortParameter>> singleSet = new HashSet<List<SortParameter>>(1);
			singleSet.add(singleList);
			
			return singleSet;
		}
		
		// Create the result which will be a set to guarantee that we don't 
		// have duplicate permutations.
		Set<List<SortParameter>> result = new HashSet<List<SortParameter>>();
		
		// Create the new list without the head item.
		List<SortParameter> paramsWithoutFirst = new ArrayList<SortParameter>(parameters);
		SortParameter firstSortParameter = parameters.get(0);
		paramsWithoutFirst.remove(firstSortParameter);
		
		// Create all permutations of the list without the head item, then 
		// iterate over the results.
		for(List<SortParameter> currList : getUniqueSets(paramsWithoutFirst)) {
			// We need to insert the current "first" sort parameter into every
			// space between all of those created thus far.
			int size = currList.size();
			for(int i = 0; i <= size; i++) {
				List<SortParameter> newList = new ArrayList<SortParameter>(currList);
				newList.add(i, firstSortParameter);
				result.add(newList);
			}
		}
		
		return result;
	}
}
