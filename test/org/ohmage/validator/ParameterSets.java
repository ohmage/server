package org.ohmage.validator;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ohmage.domain.Clazz;
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
	
	private static final Collection<String> validUsernames = new LinkedList<String>();
	private static final Collection<String> invalidUsernames = new LinkedList<String>();
	
	private static final Collection<String> validPlainTextPasswords = new LinkedList<String>();
	private static final Collection<String> invalidPlainTextPasswords = new LinkedList<String>();
	
	private static final Collection<String> validHashedPasswords = new LinkedList<String>();
	private static final Collection<String> invalidHashedPasswords = new LinkedList<String>();
	
	private static final Collection<String> validUsernameLists = new LinkedList<String>();
	private static final Collection<String> invalidUsernameLists = new LinkedList<String>();

	private static final Collection<String> validUrns = new LinkedList<String>();
	private static final Collection<String> invalidUrns = new LinkedList<String>();
	
	private static final Collection<String> validUrnLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnLists = new LinkedList<String>();
	
	private static final Collection<String> validUrnCampaignRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnCampaignRoleLists = new LinkedList<String>();
	
	private static final Collection<String> validUrnDocumentRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUrnDocumentRoleLists = new LinkedList<String>();
	
	private static final Collection<String> validUsernameCampaignRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUsernameCampaignRoleLists = new LinkedList<String>();
	
	private static final Collection<String> validUsernameClassRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUsernameClassRoleLists = new LinkedList<String>();
	
	private static final Collection<String> validUsernameDocumentRoleLists = new LinkedList<String>();
	private static final Collection<String> invalidUsernameDocumentRoleLists = new LinkedList<String>();
	
	private static final Collection<String> simpleValidLists = new LinkedList<String>();
	
	private static final Map<Date, String> dateToString = new HashMap<Date, String>();
	private static final Map<Date, String> dateTimeToString = new HashMap<Date, String>();

	private ParameterSets() {}
	
	/**
	 * Initializes all of the collections/maps.
	 */
	public static void init() {
		initEmptyValues();
		initUsernames();
		initPasswords();
		initUrnValues();
		initSimpleValidLists();
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
	 * Returns the collection of valid usernames.
	 * 
	 * @return The collection of valid usernames.
	 */
	public static Collection<String> getValidUsernames() {
		return Collections.unmodifiableCollection(validUsernames);
	}
	
	/**
	 * Returns the collection of invalid usernames.
	 * 
	 * @return The collection of invalid usernames.
	 */
	public static Collection<String> getInvalidUsernames() {
		return Collections.unmodifiableCollection(invalidUsernames);
	}
	
	/**
	 * Returns the collection of valid plain text passwords.
	 * 
	 * @return The collection of valid plain text passwords.
	 */
	public static Collection<String> getValidPlainTextPasswords() {
		return Collections.unmodifiableCollection(validPlainTextPasswords);
	}
	
	/**
	 * Returns the collection of invalid passwords.
	 * 
	 * @return The collection of invalid passwords.
	 */
	public static Collection<String> getInvalidPlainTextPasswords() {
		return Collections.unmodifiableCollection(invalidPlainTextPasswords);
	}
	
	/**
	 * Returns the collection of valid hashed passwords.
	 * 
	 * @return The collection of valid hashed passwords.
	 */
	public static Collection<String> getValidHashedPasswords() {
		return Collections.unmodifiableCollection(validHashedPasswords);
	}
	
	/**
	 * Returns the collection of invalid hashed passwords.
	 * 
	 * @return The collection of invalid hashed passwords.
	 */
	public static Collection<String> getInvalidHashedPasswords() {
		return Collections.unmodifiableCollection(invalidHashedPasswords);
	}
	
	/**
	 * Returns the collection of valid username lists.
	 * 
	 * @return The collection of valid username lists.
	 */
	public static Collection<String> getValidUsernameLists() {
		return Collections.unmodifiableCollection(validUsernameLists);
	}
	
	/**
	 * Returns the collection of invalid username lists.
	 * 
	 * @return The collection of invalid username lists.
	 */
	public static Collection<String> getInvalidUsernameLists() {
		return Collections.unmodifiableCollection(invalidUsernameLists);
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
	 * Returns the unmodifiable collection of valid username, campaign role
	 * lists.
	 * 
	 * @return The unmodifiable collection of valid username, campaign role
	 * 		   lists.
	 */
	public static Collection<String> getValidUsernameCampaignRoleLists() {
		return Collections.unmodifiableCollection(validUsernameCampaignRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid username, campaign role
	 * lists.
	 * 
	 * @return The unmodifiable collection of invalid username, campaign role
	 * 		   lists.
	 */
	public static Collection<String> getInvalidUsernameCampaignRoleLists() {
		return Collections.unmodifiableCollection(invalidUsernameCampaignRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of valid username, class role lists.
	 * 
	 * @return The unmodifiable collection of valid username, class role lists.
	 * 		   lists.
	 */
	public static Collection<String> getValidUsernameClassRoleLists() {
		return Collections.unmodifiableCollection(validUsernameClassRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid username, class role
	 * lists.
	 * 
	 * @return The unmodifiable collection of invalid username, class role
	 */
	public static Collection<String> getInvalidUsernameClassRoleLists() {
		return Collections.unmodifiableCollection(invalidUsernameClassRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of valid username, document role 
	 * lists.
	 * 
	 * @return The unmodifiable collection of valid username, document role 
	 * 		   lists.
	 */
	public static Collection<String> getValidUsernameDocumentRoleLists() {
		return Collections.unmodifiableCollection(validUsernameDocumentRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of invalid username, document role
	 * lists.
	 * 
	 * @return The unmodifiable collection of invalid username, document role
	 * 		   lists.
	 */
	public static Collection<String> getInvalidUsernameDocumentRoleLists() {
		return Collections.unmodifiableCollection(invalidUsernameDocumentRoleLists);
	}
	
	/**
	 * Returns the unmodifiable collection of valid lists that contain no
	 * specific data.
	 * 
	 * @return The unmodifiable collection of valid lists that contain no
	 * 		   specific data.
	 */
	public static Collection<String> getSimpleValidLists() {
		return Collections.unmodifiableCollection(simpleValidLists);
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
		
		System.out.println("Empty values generated:");
		for(String emptyValue : emptyValues) {
			System.out.println("\t" + emptyValue);
		}
		System.out.println();
	}
	
	/**
	 * Populates the valid and invalid username lists.
	 */
	private static void initUsernames() {
		// Too short.
		invalidUsernames.add("123");
		// Too long.
		invalidUsernames.add("12345678901234567890123456");
		// Contains valid characters, but none of the required ones.
		invalidUsernames.add("....");
		
		// Check the minimum length.
		validUsernames.add("aaaa");
		// Check the maximum length.
		validUsernames.add("aaaaaaaaaaaaaaaaaaaaaaaaa");
		// Check all of the valid characters.
		validUsernames.add("abcdefghijklmnopqrstuvwxy");
		validUsernames.add("z0123456789._@+-");
		// Check that the username may begin with a valid, but not required
		// character.
		validUsernames.add("_Bob");
		
		System.out.println("Valid usernames generated:");
		for(String validUsername : validUsernames) {
			System.out.println("\t" + validUsername);
		}
		System.out.println();
		
		System.out.println("Invalid usernames generated:");
		for(String invalidUsername : invalidUsernames) {
			System.out.println("\t" + invalidUsername);
		}
		System.out.println();
		
		// Create the username lists.
		initUsernameLists();
		
		// Create the username-role lists.
		initUsernameRoleLists();
	}
	
	/**
	 * Populates the valid and invalid username lists.
	 */
	private static void initUsernameLists() {
		validUsernameLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		
		StringBuilder validUsernameListBuilder = new StringBuilder();
		for(String validUsername : validUsernames) {
			validUsernameLists.add(validUsername);
			
			validUsernameListBuilder.append(validUsername);
			validUsernameListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
			validUsernameLists.add(validUsernameListBuilder.toString());
		}
		
		StringBuilder invalidUsernameListBuilder = new StringBuilder();
		for(String invalidUsername : invalidUsernames) {
			invalidUsernameLists.add(invalidUsername);
			
			invalidUsernameListBuilder.append(invalidUsername);
			invalidUsernameListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
			invalidUsernameLists.add(invalidUsernameListBuilder.toString());
		}
		
		System.out.println("Valid username lists generated:");
		for(String validUsernameList : validUsernameLists) {
			System.out.println("\t" + validUsernameList);
		}
		System.out.println();
		
		System.out.println("Invalid username lists generated:");
		for(String invalidUsernameList : invalidUsernameLists) {
			System.out.println("\t" + invalidUsernameList);
		}
		System.out.println();
	}
	
	/**
	 * Populates the valid and invalid username, role lists.
	 */
	private static void initUsernameRoleLists() {
		validUsernameCampaignRoleLists.add(InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameClassRoleLists.add(InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameDocumentRoleLists.add(InputKeys.ENTITY_ROLE_SEPARATOR);
		
		validUsernameCampaignRoleLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		
		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);	
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);	
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);	

		validUsernameCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.ENTITY_ROLE_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR);

		validUsernameCampaignRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameClassRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR);
		validUsernameDocumentRoleLists.add(
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR + 
				InputKeys.ENTITY_ROLE_SEPARATOR + 
				InputKeys.LIST_ITEM_SEPARATOR +
				InputKeys.LIST_ITEM_SEPARATOR);
		
		StringBuilder validUsernameCampaignRoleListBuilder = new StringBuilder();
		StringBuilder validUsernameClassRoleListBuilder = new StringBuilder();
		StringBuilder validUsernameDocumentRoleListBuilder = new StringBuilder();
		for(String validUsername : validUsernames) {
			for(Campaign.Role role : Campaign.Role.values()) {
				validUsernameCampaignRoleLists.add(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				validUsernameCampaignRoleListBuilder.append(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				validUsernameCampaignRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				validUsernameCampaignRoleLists.add(validUsernameCampaignRoleListBuilder.toString());
			}
			
			for(Clazz.Role role : Clazz.Role.values()) {
				validUsernameClassRoleLists.add(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				validUsernameClassRoleListBuilder.append(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				validUsernameClassRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				validUsernameClassRoleLists.add(validUsernameClassRoleListBuilder.toString());
			}
			
			for(Document.Role role : Document.Role.values()) {
				validUsernameDocumentRoleLists.add(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				validUsernameDocumentRoleListBuilder.append(validUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				validUsernameDocumentRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				validUsernameDocumentRoleLists.add(validUsernameDocumentRoleListBuilder.toString());
			}
		}
		
		StringBuilder invalidUsernameCampaignRoleListBuilder = new StringBuilder();
		StringBuilder invalidUsernameClassRoleListBuilder = new StringBuilder();
		StringBuilder invalidUsernameDocumentRoleListBuilder = new StringBuilder();
		for(String invalidUsername : invalidUsernames) {
			for(Campaign.Role role : Campaign.Role.values()) {
				invalidUsernameCampaignRoleLists.add(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				invalidUsernameCampaignRoleListBuilder.append(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				invalidUsernameCampaignRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				invalidUsernameCampaignRoleLists.add(invalidUsernameCampaignRoleListBuilder.toString());
			}
			
			for(Clazz.Role role : Clazz.Role.values()) {
				invalidUsernameClassRoleLists.add(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				invalidUsernameClassRoleListBuilder.append(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				invalidUsernameClassRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				invalidUsernameClassRoleLists.add(invalidUsernameClassRoleListBuilder.toString());
			}
			
			for(Document.Role role : Document.Role.values()) {
				invalidUsernameDocumentRoleLists.add(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				
				invalidUsernameDocumentRoleListBuilder.append(invalidUsername + InputKeys.ENTITY_ROLE_SEPARATOR + role);
				invalidUsernameDocumentRoleListBuilder.append(InputKeys.LIST_ITEM_SEPARATOR);
				invalidUsernameDocumentRoleLists.add(invalidUsernameDocumentRoleListBuilder.toString());
			}
		}
		
		System.out.println("Valid username, campaign role lists generated:");
		for(String validUsernameCamaignRoleList : validUsernameCampaignRoleLists) {
			System.out.println("\t" + validUsernameCamaignRoleList);
		}
		System.out.println();
		
		System.out.println("Invalid username, campaign role lists generated:");
		for(String invalidUsernameCamaignRoleList : invalidUsernameCampaignRoleLists) {
			System.out.println("\t" + invalidUsernameCamaignRoleList);
		}
		System.out.println();
		
		System.out.println("Valid username, class role lists generated:");
		for(String validUsernameClassRoleList : validUsernameClassRoleLists) {
			System.out.println("\t" + validUsernameClassRoleList);
		}
		System.out.println();
		
		System.out.println("Invalid username, class role lists generated:");
		for(String invalidUsernameClassRoleList : invalidUsernameClassRoleLists) {
			System.out.println("\t" + invalidUsernameClassRoleList);
		}
		System.out.println();
	}
	
	/**
	 * Populates the valid and invalid password lists.
	 */
	private static void initPasswords() {
		// Check the minimum length.
		validPlainTextPasswords.add("aaAA00..");
		// Check the maximum length.
		validPlainTextPasswords.add("aaaaAAAA0000....");
		// Check all of the valid characters.
		validPlainTextPasswords.add("aA0.abcdefghijkl");
		validPlainTextPasswords.add("aA0.mnopqrstuvwx");
		validPlainTextPasswords.add("aA0.yz0123456789");
		validPlainTextPasswords.add("aA0.,.<>:[]!@#$%");
		validPlainTextPasswords.add("aA0.^&*+-/=?_{}|");
		
		// Too short.
		invalidPlainTextPasswords.add("aA.4567");
		// Too long.
		invalidPlainTextPasswords.add("aA.45678901234567");
		// Missing a lower case character.
		invalidPlainTextPasswords.add("AAAA00..");
		// Missing an upper case character.
		invalidPlainTextPasswords.add("aaaa00..");
		// Missing a number.
		invalidPlainTextPasswords.add("aaAAAA..");
		// Missing a special character.
		invalidPlainTextPasswords.add("aaAA0000");
		
		// Test all valid characters.
		validHashedPasswords.add("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456");
		validHashedPasswords.add("789.$6789012345678901234567890123456789012345678901234567890");

		// Too short.
		invalidHashedPasswords.add("12345678901234567890123456789012345678901234567890123456789");
		// Too long.
		invalidHashedPasswords.add("1234567890123456789012345678901234567890123456789012345678901");
		
		// TODO: We may want to restrict the first character being a '$', 
		// followed by a plausible version, followed by another '$', followed
		// by a plausible hash value, followed by a final '$', but in order to
		// do this we would need to update our hashed password validator. This
		// may be a good idea, but I am currently only putting down the ground
		// work and don't want to deal.
		
		System.out.println("Valid plain text passwords generated:");
		for(String validPassword : validPlainTextPasswords) {
			System.out.println("\t" + validPassword);
		}
		System.out.println();
		
		System.out.println("Invalid plain text passwords generated:");
		for(String invalidPassword : invalidPlainTextPasswords) {
			System.out.println("\t" + invalidPassword);
		}
		System.out.println();
		
		System.out.println("Valid hashed passwords generated:");
		for(String validHashedPassword : validHashedPasswords) {
			System.out.println("\t" + validHashedPassword);
		}
		System.out.println();
		
		System.out.println("Invalid hashed passwords generated:");
		for(String invalidHashedPassword : invalidHashedPasswords) {
			System.out.println("\t" + invalidHashedPassword);
		}
		System.out.println();
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
		
		System.out.println("Valid URNs generated:");
		for(String validUrn : validUrns) {
			System.out.println("\t" + validUrn);
		}
		System.out.println();
		
		System.out.println("Invalid URNs generated:");
		for(String invalidUrn : invalidUrns) {
			System.out.println("\t" + invalidUrn);
		}
		System.out.println();
		
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
		
		System.out.println("Valid URN lists generated:");
		for(String validUrnList : validUrnLists) {
			System.out.println("\t" + validUrnList);
		}
		System.out.println();
		
		System.out.println("Invalid URN lists generated:");
		for(String invalidUrnList : invalidUrnLists) {
			System.out.println("\t" + invalidUrnList);
		}
		System.out.println();
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
		StringBuilder invalidUrnDocumentRoleListBuilder = new StringBuilder();
		for(String invalidUrn : invalidUrns) {
			for(Campaign.Role role : Campaign.Role.values()) {
				invalidUrnCampaignRoleListBuilder.append(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				invalidUrnCampaignRoleLists.add(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			invalidUrnCampaignRoleLists.add(invalidUrnCampaignRoleListBuilder.toString());
			
			for(Document.Role role : Document.Role.values()) {
				invalidUrnDocumentRoleListBuilder.append(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role + InputKeys.LIST_ITEM_SEPARATOR);
				invalidUrnDocumentRoleLists.add(invalidUrn + InputKeys.ENTITY_ROLE_SEPARATOR + role);
			}
			invalidUrnDocumentRoleLists.add(invalidUrnDocumentRoleListBuilder.toString());
		}
		
		System.out.println("Valid URN, campaign role lists generated:");
		for(String validUrnCampaignRoleList : validUrnCampaignRoleLists) {
			System.out.println("\t" + validUrnCampaignRoleList);
		}
		System.out.println();
		
		System.out.println("Invalid URN, campaign role lists generated:");
		for(String invalidUrnCampaignRoleList : invalidUrnCampaignRoleLists) {
			System.out.println("\t" + invalidUrnCampaignRoleList);
		}
		System.out.println();
		
		System.out.println("Valid URN, document role lists generated:");
		for(String validUrnDocumentRoleList : validUrnDocumentRoleLists) {
			System.out.println("\t" + validUrnDocumentRoleList);
		}
		System.out.println();
		
		System.out.println("Invalid URN, document role lists generated:");
		for(String invalidUrnDocumentRoleList : invalidUrnDocumentRoleLists) {
			System.out.println("\t" + invalidUrnDocumentRoleList);
		}
		System.out.println();
	}
	
	/**
	 * Populates the collection of simple, valid lists. The lists may or may 
	 * not contain meaningful data, but they will never be null or whitespace
	 * only.
	 */
	public static void initSimpleValidLists() {
		simpleValidLists.add(InputKeys.LIST_ITEM_SEPARATOR);
		simpleValidLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		simpleValidLists.add(InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR + InputKeys.LIST_ITEM_SEPARATOR);
		
		System.out.println("Valid, simple lists generated:");
		for(String simpleValidList : simpleValidLists) {
			System.out.println("\t" + simpleValidList);
		}
		System.out.println();
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
		
		System.out.println("Valid dates generated:");
		for(String dates : dateToString.values()) {
			System.out.println("\t" + dates);
		}
		System.out.println();
		
		System.out.println("Valid date-times generated:");
		for(String dateTime : dateTimeToString.values()) {
			System.out.println("\t" + dateTime);
		}
		System.out.println();
	}
}