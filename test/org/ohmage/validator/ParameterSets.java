package org.ohmage.validator;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ohmage.domain.Document;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Builds the parameter collections/maps used as the parameters across multiple
 * validators.
 * 
 * @author John Jenkins
 */
public class ParameterSets {
	private static final Collection<String> emptyValues = new LinkedList<String>();

	private static final Collection<String> validUrns = new LinkedList<String>();
	private static final Collection<String> invalidUrns = new LinkedList<String>();
	
	private static final Collection<String> validUrnLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnLists = new LinkedList<String>();
	
	private static final Collection<String> validUrnCampaignRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnCampaignRoleLists = new LinkedList<String>();
	
	private static final Collection<String> validUrnDocumentRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnDocumentRoleLists = new LinkedList<String>();
	
	private static final Map<Date, String> dateToString = new HashMap<Date, String>();
	private static final Map<Date, String> dateTimeToString = new HashMap<Date, String>();

	private ParameterSets() {}
	
	/**
	 * Initializes all of the collections/maps.
	 */
	public static void init() {
		initEmptyValues();
		initUrnValues();
		initDateValues();
	}
	
	/**
	 * Returns the collection of null or whitespace-only strings.
	 * 
	 * @return The collection of null or whitespace-only strings.
	 */
	public static Collection<String> getEmptyValues() {
		return Collections.unmodifiableCollection(emptyValues);
	}
	
	/**
	 * Returns the collection valid URNs.
	 * 
	 * @return The collection of valid URNs.
	 */
	public static Collection<String> getValidUrns() {
		return Collections.unmodifiableCollection(validUrns);
	}
	
	/**
	 * Returns the collection of invalid URNs.
	 * 
	 * @return The collection of invalid URNs.
	 */
	public static Collection<String> getInvalidUrns() {
		return Collections.unmodifiableCollection(invalidUrns);
	}
	
	/**
	 * Returns the collection of valid URN lists.
	 * 
	 * @return The collection of valid URN lists.
	 */
	public static Collection<String> getValidUrnLists() {
		return Collections.unmodifiableCollection(validUrnLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid URN lists.
	 * 
	 * @return The unmodifiable collection of invalid URN lists.
	 */
	public static Collection<String> getInvalidUrnLists() {
		return Collections.unmodifiableCollection(invalidUrnLists);
	}
	
	/**
	 * Returns the unmodifiable collection of valid URN, campaign role lists.
	 * 
	 * @return The unmodifiable collection of valid URN, campaign role lists.
	 */
	public static Collection<String> getValidUrnCampaignRoleLists() {
		return Collections.unmodifiableCollection(validUrnCampaignRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid URN, campaign role lists.
	 * 
	 * @return The unmodifiable collection of invalid URN, campaign role lists.
	 */
	public static Collection<String> getInvalidUrnCampaignRoleLists() {
		return Collections.unmodifiableCollection(invalidUrnCampaignRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of valid URN, document role lists.
	 * 
	 * @return The unmodifiable collection of valid URN, document role lists.
	 */
	public static Collection<String> getValidUrnDocumentRoleLists() {
		return Collections.unmodifiableCollection(validUrnDocumentRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid URN, document role lists.
	 * 
	 * @return The unmodifiable collection of invalid URN, document role lists.
	 */
	public static Collection<String> getInvalidUrnDocumentRoleLists() {
		return Collections.unmodifiableCollection(invalidUrnDocumentRoleLists);
	}
	
	/**
	 * Returns a map of Date objects and their corresponding String 
	 * representation.
	 * 
	 * @return A map of Date objects and their corresponding String
	 * 		   representation.
	 */
	public static Map<Date, String> getDateToString() {
		return Collections.unmodifiableMap(dateToString);
	}
	
	/**
	 * Returns a map of Date objects and their corresponding String
	 * representation that includes both their date and time values.
	 * 
	 * @return A map of Date objects and their corresponding String
	 * 		   representation that includes both their date and time values.
	 */
	public static Map<Date, String> getDateTimeToString() {
		return Collections.unmodifiableMap(dateTimeToString);
	}
	
	/**
	 * Populates the empty values collection with all of the empty or 
	 * whitespace-only string values that we will use for testing.
	 */
	private static void initEmptyValues() {
		// Build the empty values.
		emptyValues.add(null);
		emptyValues.add("");
		emptyValues.add(" ");
		emptyValues.add("\t");
		emptyValues.add(" \t ");
		emptyValues.add("\n");
		emptyValues.add(" \n ");
		emptyValues.add(" \n\t ");
		emptyValues.add(" \t\n ");
		emptyValues.add(" \n \t ");
		emptyValues.add(" \t \n ");
	}
	
	/**
	 * Populates the valid and invalid URN collections with all of the valid
	 * and invalid URN values that we will use for testing.
	 */
	private static void initUrnValues() {
		// This will build a URN of the minimum length and ending with a colon.
		StringBuilder urnBuilder = new StringBuilder("urn:");
		invalidUrns.add(urnBuilder.toString());
		
		// This will build a URN of the minimum length but without any values.
		StringBuilder urnWithoutValuesBuilder = new StringBuilder(urnBuilder);

		// Compose both URN builders.
		for(int i = 1; i < StringUtils.NUM_URN_SEGMENTS; i++) {
			// Add the dummy character.
			urnBuilder.append('a');
			
			// If this is the final segment, add it to the collection of good
			// URNs.
			if(i == (StringUtils.NUM_URN_SEGMENTS - 1)) {
				validUrns.add(urnBuilder.toString());
			}
			// Otherwise, add it to the collection of bad URNs as it is still 
			// not long enough.
			else {
				invalidUrns.add(urnBuilder.toString());
			}
			
			// Add the dividor.
			urnBuilder.append(':');
			urnWithoutValuesBuilder.append(':');
			
			// No matter the length, no URN may end with a dividor character.
			invalidUrns.add(urnBuilder.toString());
		}
		// Now, we have a URN builder with the exact number of characters and
		// ending with a dividor character and ready to be cloned to create new
		// valid or invalid URNs.
		
		// For the URN builder with no values, add it to the list of invalid
		// URNs.
		invalidUrns.add(urnWithoutValuesBuilder.toString());
		
		// Create a URN with a space before the next character.
		StringBuilder urnWithSpaceBuilder = new StringBuilder(urnBuilder);
		urnWithSpaceBuilder.append(" c");
		invalidUrns.add(urnWithSpaceBuilder.toString());
		
		// Append a new segment onto the end just to be sure that it isn't only
		// checking the last segment for a space.
		urnWithSpaceBuilder.append(":d");
		invalidUrns.add(urnWithSpaceBuilder.toString());
		
		// Create a URN with a space within its character string.
		urnWithSpaceBuilder = new StringBuilder(urnBuilder);
		urnWithSpaceBuilder.append("c c");
		invalidUrns.add(urnWithSpaceBuilder.toString());

		// Append a new segment onto the end just to be sure that it isn't only
		// checking the last segment for a space.
		urnWithSpaceBuilder.append(":d");
		invalidUrns.add(urnWithSpaceBuilder.toString());
		
		// Create a URN with a '#' before the next character.
		StringBuilder urnWithInvalidCharacterBuilder = new StringBuilder(urnBuilder);
		urnWithInvalidCharacterBuilder.append("#c");
		invalidUrns.add(urnWithInvalidCharacterBuilder.toString());
		
		// Append a new segment onto the end just to be sure that it isn't only
		// checking the last segment for invalid characters.
		urnWithInvalidCharacterBuilder.append(":d");
		invalidUrns.add(urnWithInvalidCharacterBuilder.toString());
		
		// Create a URN with a '#' within its character string.
		urnWithInvalidCharacterBuilder = new StringBuilder(urnBuilder);
		urnWithInvalidCharacterBuilder.append("c#c");
		invalidUrns.add(urnWithInvalidCharacterBuilder.toString());

		// Append a new segment onto the end just to be sure that it isn't only
		// checking the last segment for invalid characters.
		urnWithInvalidCharacterBuilder.append(":d");
		invalidUrns.add(urnWithInvalidCharacterBuilder.toString());
		
		// Now, initialize all of the URN lists.
		initUrnLists();
		
		// Now, initialize all of the URN-role lists.
		initUrnRoleLists();
	}
	
	/**
	 * Populates the valid and invalid URN list collections.
	 */
	private static void initUrnLists() {
		validUrnLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		validUrnLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		validUrnLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		
		StringBuilder validUrnListBuilder = new StringBuilder();
		for(String validUrn : validUrns) {
			validUrnLists.add(validUrn);
			validUrnLists.add(InputKeys.LIST_ITEM_SEPARATOR + validUrn + InputKeys.LIST_ITEM_SEPARATOR);
			
			validUrnListBuilder.append(validUrn);
			validUrnLists.add(validUrnListBuilder.toString());
			
			validUrnListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
			validUrnLists.add(validUrnListBuilder.toString());
		}
		
		StringBuilder invalidUrnListBuilder = new StringBuilder();
		for(String invalidUrn : invalidUrns) {
			invalidUrnLists.add(invalidUrn);
			invalidUrnLists.add(InputKeys.LIST_ITEM_SEPARATOR + invalidUrn + InputKeys.LIST_ITEM_SEPARATOR);
			
			invalidUrnListBuilder.append(invalidUrn);
			invalidUrnLists.add(invalidUrnListBuilder.toString());
			
			invalidUrnListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
			invalidUrnLists.add(invalidUrnListBuilder.toString());
		}
		
		for(String validUrn : validUrns) {
			for(String invalidUrn : invalidUrns) {
				invalidUrnLists.add(validUrn + InputKeys.LIST_ITEM_SEPARATOR + invalidUrn);
				invalidUrnLists.add(invalidUrn + InputKeys.LIST_ITEM_SEPARATOR + validUrn);
			}
		}
	}
	
	/**
	 * Populates the valid and invalid URN, campaign role and URN, document
	 * role collections.
	 */
	private static void initUrnRoleLists() {
		validUrnCampaignRoleLists.add(InputKeys.ENTITY_ROLE_SEPARATOR);
		validUrnDocumentRoleLists.add(InputKeys.ENTITY_ROLE_SEPARATOR);
		
		validUrnCampaignRoleLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		
		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);		
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUrnCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR);
		validUrnDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR);
		
		StringBuilder validUrnCampaignRoleListBuilder = new StringBuilder();
		StringBuilder validUrnDocumentRoleListBuilder = new StringBuilder();
		for(String validUrn : validUrns) {
			for(Campaign.Role role : Campaign.Role.values()) {
				validUrnCampaignRoleListBuilder.append(validUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				validUrnCampaignRoleLists.add(validUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			validUrnCampaignRoleLists.add(validUrnCampaignRoleListBuilder.toString());
			
			for(Document.Role role : Document.Role.values()) {
				validUrnDocumentRoleListBuilder.append(validUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				validUrnDocumentRoleLists.add(validUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			validUrnDocumentRoleLists.add(validUrnDocumentRoleListBuilder.toString());
		}
		
		StringBuilder invalidUrnCampaignRoleListBuilder = new StringBuilder();
		for(String invalidUrn : invalidUrns) {
			for(Campaign.Role role : Campaign.Role.values()) {
				invalidUrnCampaignRoleListBuilder.append(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				invalidUrnCampaignRoleLists.add(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			invalidUrnCampaignRoleLists.add(invalidUrnCampaignRoleListBuilder.toString());
			
			for(Document.Role role : Document.Role.values()) {
				validUrnDocumentRoleListBuilder.append(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				invalidUrnDocumentRoleLists.add(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			invalidUrnDocumentRoleLists.add(validUrnDocumentRoleListBuilder.toString());
		}
	}
	
	/**
	 * Populates the date map with Date objects and their corresponding String
	 * representations for testing.
	 */
	private static void initDateValues() {
		Calendar epochCalendar = Calendar.getInstance();
		// This is the epoch because we are 8 hours behind and that's how our
		// current system bases its calculations. In the future, we may want to
		// get rid of the date values and go only with the epoch millis.
		epochCalendar.setTimeInMillis(28800000);
		
		Calendar currCalendar = Calendar.getInstance();
		
		// Test at the epoch.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		dateToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-01");
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-01 00:00:00");

		// Test where the month is one greater which causes the year to roll  
		// over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.YEAR, 1971);
		dateToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-01");
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-01 00:00:00");

		// Test where the day is one greater which causes the month to roll 
		// over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.MONTH, 1);
		dateToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-32");
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-32 00:00:00");

		// Test where the month and day are both one greater which causes the 
		// year and month to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.YEAR, 1971);
		currCalendar.set(Calendar.MONTH, 1);
		dateToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-32");
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-32 00:00:00");
		
		// Test where the hour is greater and causes the day to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.DAY_OF_MONTH, 2);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-01 24:00:00");

		// Test where the month, day, and hour are all one greater which causes 
		// the year, month, and day to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.YEAR, 1971);
		currCalendar.set(Calendar.MONTH, 1);
		currCalendar.set(Calendar.DAY_OF_MONTH, 2);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-32 24:00:00");
		
		// Test where the minute is greater and causes the hour to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.HOUR_OF_DAY, 1);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-01 00:60:00");

		// Test where the month, day, hour, and minute are all one greater 
		// which causes the year, month, day, and hour to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.YEAR, 1971);
		currCalendar.set(Calendar.MONTH, 1);
		currCalendar.set(Calendar.DAY_OF_MONTH, 2);
		currCalendar.set(Calendar.HOUR_OF_DAY, 1);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-32 24:60:00");
		
		// Test where the second is greater and causes the minute to roll over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.MINUTE, 1);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-01-01 00:00:60");

		// Test where the month, day, hour, minute, and second are all one  
		// greater which causes the year, month, day, hour, and minute to roll
		// over.
		currCalendar.setTimeInMillis(epochCalendar.getTimeInMillis());
		currCalendar.set(Calendar.YEAR, 1971);
		currCalendar.set(Calendar.MONTH, 1);
		currCalendar.set(Calendar.DAY_OF_MONTH, 2);
		currCalendar.set(Calendar.HOUR_OF_DAY, 1);
		currCalendar.set(Calendar.MINUTE, 1);
		dateTimeToString.put(new Date(currCalendar.getTimeInMillis()), "1970-13-32 24:60:60");
	}
}