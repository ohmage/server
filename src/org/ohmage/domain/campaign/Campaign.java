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
package org.ohmage.domain.campaign;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.config.grammar.custom.ConditionParseException;
import org.ohmage.config.grammar.custom.ConditionValidator;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Prompt.LabelValuePair;
import org.ohmage.domain.campaign.Prompt.Type;
import org.ohmage.domain.campaign.prompt.AudioPrompt;
import org.ohmage.domain.campaign.prompt.ChoicePrompt;
import org.ohmage.domain.campaign.prompt.CustomChoicePrompt;
import org.ohmage.domain.campaign.prompt.DocumentPrompt;
import org.ohmage.domain.campaign.prompt.HoursBeforeNowPrompt;
import org.ohmage.domain.campaign.prompt.MultiChoiceCustomPrompt;
import org.ohmage.domain.campaign.prompt.MultiChoicePrompt;
import org.ohmage.domain.campaign.prompt.NumberPrompt;
import org.ohmage.domain.campaign.prompt.PhotoPrompt;
import org.ohmage.domain.campaign.prompt.RemoteActivityPrompt;
import org.ohmage.domain.campaign.prompt.SingleChoiceCustomPrompt;
import org.ohmage.domain.campaign.prompt.SingleChoicePrompt;
import org.ohmage.domain.campaign.prompt.TextPrompt;
import org.ohmage.domain.campaign.prompt.TimestampPrompt;
import org.ohmage.domain.campaign.prompt.VideoPrompt;
import org.ohmage.exception.DomainException;
import org.ohmage.request.InputKeys;
import org.ohmage.util.DateTimeUtils;
import org.ohmage.util.StringUtils;

/**
 * An immutable representation of a campaign.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class Campaign {
	private static final String XML_ID = "/campaign/campaignUrn";
	private static final String XML_NAME = "/campaign/campaignName";
	
	private static final String XML_ICON_URL = "/campaign/iconUrl";
	private static final String XML_AUTHORED_BY = "/campaign/authoredBy";
	
	private static final String XML_SURVEYS = "/campaign/surveys";
	private static final String XML_SURVEY = "survey";
	private static final String XML_SURVEY_ID = "id";
	private static final String XML_SURVEY_TITLE = "title";
	private static final String XML_SURVEY_DESCRIPTION = "description";
	private static final String XML_SURVEY_INTRO_TEXT = "introText";
	private static final String XML_SURVEY_SUBMIT_TEXT = "submitText";
	private static final String XML_SURVEY_ANYTIME = "anytime";
	private static final String XML_SURVEY_CONTENT_LIST = "contentList";
	private static final String XML_CONTENT_LIST_ITEMS = "prompt | repeatableSet | message";
	
	private static final String XML_PROMPT_ID = "id";
	private static final String XML_PROMPT_CONDITION = "condition";
	private static final String XML_PROMPT_UNIT = "unit";
	private static final String XML_PROMPT_TEXT = "promptText";
	private static final String XML_PROMPT_EXPLANATION_TEXT = "explanationText";
	private static final String XML_PROMPT_SKIPPABLE = "skippable";
	private static final String XML_PROMPT_SKIP_LABEL = "skipLabel";
	private static final String XML_PROMPT_DISPLAY_LABEL = "displayLabel";
	private static final String XML_PROMPT_TYPE = "promptType";
	private static final String XML_PROMPT_PROPERTIES = "properties";
	private static final String XML_PROMPT_PROPERTY = "property";
	private static final String XML_PROPERTY_KEY = "key";
	private static final String XML_PROPERTY_LABEL = "label";
	private static final String XML_PROPERTY_VALUE = "value";
	private static final String XML_PROMPT_DEFAULT = "default";
	
	private static final String XML_REPEATABLE_SET_ID = "id";
	private static final String XML_REPEATABLE_SET_CONDITION = "condition";
	private static final String XML_REPEATABLE_SET_TERMINATION_QUESTION = "terminationQuestion";
	private static final String XML_REPEATABLE_SET_TERMINATION_TRUE_LABEL = "terminationTrueLabel";
	private static final String XML_REPEATABLE_SET_TERMINATION_FALSE_LABEL = "terminationFalseLabel";
	private static final String XML_REPEATABLE_SET_TERMINATION_SKIP_ENABLED = "terminationSkipEnabled";
	private static final String XML_REPEATABLE_SET_TERMINATION_SKIP_LABEL = "terminationSkipLabel";
	private static final String XML_REPEATABLE_SET_PROMPTS = "prompts";
	
	private static final String XML_MESSAGE_ID = "id";
	private static final String XML_MESSAGE_CONDITION = "condition";
	private static final String XML_MESSAGE_TEXT = "messageText";
	
	private static final String JSON_KEY_ID = "campaign_id";
	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_DESCRIPTION = "description";
	private static final String JSON_KEY_ICON_URL = "icon_url";
	private static final String JSON_KEY_AUTHORED_BY = "authored_by";
	private static final String JSON_KEY_RUNNING_STATE = "running_state";
	private static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	private static final String JSON_KEY_CREATION_TIMESTAMP = "creation_timestamp";
	private static final String JSON_KEY_XML = "xml";
	private static final String JSON_KEY_SURVEYS = "surveys";
	
	private static final String JSON_KEY_CLASSES = "classes";
	
	private static final String JSON_KEY_ROLES = "user_role_campaign";
	private static final String JSON_KEY_SUPERVISOR = "supervisor";
	private static final String JSON_KEY_AUTHOR = "author";
	private static final String JSON_KEY_ANALYST = "analyst";
	private static final String JSON_KEY_PARTICIPANT = "participant";
	
	private static final Pattern VALID_CHARACTERS_PATTERN = 
			Pattern.compile("[a-zA-Z0-9_]+");
	
	private static final int MAX_ID_LENGTH = 255;
	private static final int MAX_NAME_LENGTH = 255;
	private static final int MAX_ICON_URL_LENGTH = 255;
	
	/**
	 * The configuration's unique identifier.
	 */
	private final String id;
	/**
	 * The configuration's name.
	 */
	private final String name;
	/**
	 * An optional description for this configuration.
	 */
	private final String description;
	
	/**
	 * The date and time that this configuration was created.
	 */
	private final DateTime creationTimestamp;
	/**
	 * The map of survey unique identifiers to Survey objects for this 
	 * configuration.
	 * 
	 * Note: Under certain situations this may be an empty list. It should 
	 * never be null, however.
	 */
	private final Map<String, Survey> surveyMap;
	/**
	 * The XML file as a string that defines this configuration.
	 */
	private final String xml;
	
	/**
	 * The URL of an image that can be displayed as an icon to the user for 
	 * this campaign.
	 */
	private final URL iconUrl;
	
	/**
	 * Some identifying information about the user that authored this 
	 * configuration.
	 */
	private final String authoredBy;
	
	/**
	 * Campaign privacy states.
	 * 
	 * @author John Jenkins
	 */
	public static enum PrivacyState {
		PRIVATE,
		SHARED;
		
		/**
		 * Converts a String value into a PrivacyState or throws an exception
		 * if there is no comparable privacy state.
		 * 
		 * @param privacyState The privacy state to be converted into a 
		 * 					   PrivacyState enum.
		 * 
		 * @return A comparable PrivacyState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									PrivacyState enum.
		 */
		public static PrivacyState getValue(String privacyState) {
			return valueOf(privacyState.toUpperCase());
		}
		
		/**
		 * Converts the privacy state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	/**
	 * The current privacy state of this configuration.
	 */
	private final PrivacyState privacyState;
	
	/**
	 * Campaign running states.
	 * 
	 * @author John Jenkins
	 */
	public static enum RunningState {
		RUNNING,
		STOPPED;
		
		/**
		 * Converts a String value into a RunningState or throws an exception 
		 * if there is no comparable running state.
		 * 
		 * @param runningState The running state to be converted into a 
		 * 					   RunningState enum.
		 * 
		 * @return A comparable RunningState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									RunningState enum.
		 */
		public static RunningState getValue(String runningState) {
			return valueOf(runningState.toUpperCase());
		}
		
		/**
		 * Converts the running state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	/**
	 * The current running state of this configuration.
	 */
	private final RunningState runningState;
	
	/**
	 * Campaign roles.
	 * 
	 * @author John Jenkins
	 */
	public static enum Role {
		SUPERVISOR,
		AUTHOR,
		ANALYST,
		PARTICIPANT;
		
		/**
		 * Converts a String value into a Role or throws an exception if there
		 * is no comparable role.
		 * 
		 * @param role The role to be converted into a Role enum.
		 * 
		 * @return A comparable Role enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									Role enum.
		 */
		public static Role getValue(String role) {
			return valueOf(role.toUpperCase());
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final Map<String, Collection<Role>> userRoles;

	/**
	 * The output formats for reading campaigns.
	 * 
	 * @author John Jenkins
	 */
	public static enum OutputFormat { 
		SHORT, 
		LONG, 
		XML;
		
		/**
		 * Converts a string representing an output format into an OutputFormat
		 * enum.
		 * 
		 * @param outputFormat The output format as a string.
		 * 
		 * @return The OutputFormat enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comperable
		 * 									OutputFormat enum.
		 */
		public static OutputFormat getValue(final String outputFormat) {
			return valueOf(outputFormat.toUpperCase());
		}
		
		/**
		 * Converts the output format to a nice, human-readable format.
		 * 
		 * @return The output format as a nice, human-readable format. 
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	} 
	
	private final List<String> classes;
	
	/**
	 * The list of campaign masks for the requesting user. This list should
	 * remain sorted at all times based on the
	 * {@link CampaignMask#compareTo(CampaignMask)} definition.
	 */
	private final List<CampaignMask> masks = new LinkedList<CampaignMask>();
	
	/**
	 * Creates a new configuration object that represents the configuration
	 * defined by the 'xml'.
	 * 
	 * @param id This configuration's unique identifier.
	 * 
	 * @param name This configuration's name.
	 * 
	 * @param description This configuration's description. This may be null if
	 * 					  the configuration has no description.
	 * 
	 * @param runningState This configuration's current running state.
	 * 
	 * @param privacyState This configuration's current privacy state.
	 * 
	 * @param creationTimestamp The date and time this configuration was 
	 * 							created.
	 * 
	 * @param surveyMap The map of survey unique identifiers to survey objects.
	 * 
	 * @param xml This configuration as an XML file.
	 * 
	 * @throws DomainException One of the parameters is invalid.
	 */
	public Campaign(
			final String id, 
			final String name, 
			final String description,
			final URL iconUrl, 
			final String authoredBy, 
			final RunningState runningState, 
			final PrivacyState privacyState, 
			final DateTime creationTimestamp, 
			final Map<String, Survey> surveyMap, 
			final String xml) 
			throws DomainException {

		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new DomainException("The ID cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new DomainException("The name cannot be null or whitespace only.");
		}
		if(runningState == null) {
			throw new DomainException("The running state cannot be null.");
		}
		if(privacyState == null) {
			throw new DomainException("The privacy state cannot be null.");
		}
		if(creationTimestamp == null) {
			throw new DomainException("The creation timestamp cannot be null.");
		}
		if(null == surveyMap) {
			throw new DomainException("The survey map cannot be null or empty only.");
		}
		
		this.id = id;
		this.name = name;
		this.description = description;
		
		this.iconUrl = iconUrl;
		this.authoredBy = authoredBy;
		
		this.runningState = runningState;
		this.privacyState = privacyState;
		
		this.creationTimestamp = new DateTime(creationTimestamp);  
		
		this.xml = xml;
		
		this.surveyMap = surveyMap; // TODO deep copy?
		
		userRoles = new HashMap<String, Collection<Role>>();
		classes = new LinkedList<String>();
	}
	
	/**
	 * Creates a Campaign object from the JSON information.
	 * 
	 * @param id The campaign's identifier.
	 * 
	 * @param information The information about the campaign.
	 * 
	 * @throws DomainException One of the parameters is invalid.
	 */
	public Campaign(
			final String id, 
			final JSONObject information) 
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new DomainException("The ID is null or whitespace only.");
		}
		else if(information == null) {
			throw new DomainException("The information is null.");
		}
		
		String tDescription = null;
		try {
			tDescription = information.getString(JSON_KEY_DESCRIPTION);
		}
		catch(JSONException e) {
			// The description is optional.
		}
		description = tDescription;
		
		try {
			runningState = RunningState.getValue(information.getString(JSON_KEY_RUNNING_STATE));
		}
		catch(JSONException e) {
			throw new DomainException("The running state is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The running state is invalid.", e);
		}

		try {
			privacyState = PrivacyState.getValue(information.getString(JSON_KEY_PRIVACY_STATE));
		}
		catch(JSONException e) {
			throw new DomainException("The privacy state is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The privacy state is invalid.", e);
		}
		
		try {
			creationTimestamp = 
					DateTimeUtils.getDateTimeFromString(
						information.getString(JSON_KEY_CREATION_TIMESTAMP));
			
			if(creationTimestamp == null) {
				throw new DomainException("The creation timestamp is invalid.");
			}
		}
		catch(JSONException e) {
			throw new DomainException("The creation timestamp is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The date-time could not be parsed.",
					e);
		}
		
		Map<String, Collection<Role>> tUserRoles = new HashMap<String, Collection<Role>>();
		try {
			JSONObject roles = information.getJSONObject(JSON_KEY_ROLES);
			
			Iterator<?> keys = roles.keys();
			while(keys.hasNext()) {
				Role role = Role.getValue((String) keys.next());
				
				JSONArray usernames = roles.getJSONArray(role.toString());
				int numUsernames = usernames.length();
				for(int i = 0; i < numUsernames; i++) {
					String username = usernames.getString(i);
					
					Collection<Role> currUserRoles = tUserRoles.get(username);
					if(currUserRoles == null) {
						currUserRoles = new LinkedList<Role>();
						tUserRoles.put(username, currUserRoles);
					}
					currUserRoles.add(role);
				}
			}
		}
		catch(JSONException e) {
			// The user-role map is optional.
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("Unknown role.", e);
		}
		userRoles = tUserRoles;
		
		List<String> tClasses = new LinkedList<String>();
		try {
			JSONArray classesJson = information.getJSONArray(JSON_KEY_CLASSES);
			int numClasses = classesJson.length();
			for(int i = 0; i < numClasses; i++) {
				tClasses.add(classesJson.getString(i));
			}
		}
		catch(JSONException e) {
			// The classes list is optional.
		}
		classes = tClasses;
		
		// Attempt to get this information from the XML first, and, if the XML
		// is missing, attempt to retrieve it from the JSON.
		String tId = null;
		String tName = null;
		URL tIconUrl = null;
		String tAuthoredBy = null;
		String tXml = null;
		Map<String, Survey> tSurveyMap = new HashMap<String, Survey>(0);
		try {
			tXml = information.getString(JSON_KEY_XML);
			
			Document document;
			try {
				document = (new Builder()).build(new StringReader(tXml));
			} 
			catch(IOException e) {
				// This should only be thrown if it can't read the 'xml', but
				// given that it is already in memory this should never happen.
				throw new DomainException("XML was unreadable.", e);
			}
			catch(XMLException e) {
				throw new DomainException("No usable XML parser could be found.", e);
			}
			catch(ValidityException e) {
				throw new DomainException("The XML is invalid.", e);
			}
			catch(ParsingException e) {
				throw new DomainException("The XML is not well formed.", e);
			}
			
			Element root = document.getRootElement();
			
			tId = getId(root, id);
			tName = getName(root, null);

			tIconUrl = getIconUrl(root);
			tAuthoredBy = getAuthoredBy(root);
			
			tSurveyMap = getSurveys(root);
		}
		catch(JSONException noXml) {
			tId = id;
			
			try {
				tName = information.getString(JSON_KEY_NAME);
			}
			catch(JSONException e) {
				throw new DomainException("The campaign's name was missing from the JSON.", e);
			}
			
			try {
				tIconUrl = new URL(information.getString(JSON_KEY_ICON_URL));
			}
			catch(JSONException e) {
				// The icon URL is optional.
			}
			catch(MalformedURLException e) {
				throw new DomainException("The icon URL is not a valid URL.", e);
			}
			
			try {
				tAuthoredBy = information.getString(JSON_KEY_AUTHORED_BY);
			}
			catch(JSONException e) {
				// The authored by value is optional.
			}
		}
		this.id = tId;
		name = tName;
		
		iconUrl = tIconUrl;
		authoredBy = tAuthoredBy;
		
		xml = tXml;
		surveyMap = tSurveyMap;
	}
	
	/**
	 * Creates a Campaign object with the given XML by parsing it and
	 * setting the appropriate values.
	 * 
	 * @param description The optional description of the configuration.
	 * 
	 * @param runningState The configuration's current running state.
	 * 
	 * @param privacyState The configuration's current privacy state.
	 * 
	 * @param creationTimestamp The configuration's creation date and time.
	 * 
	 * @param xml The configuration defining XML.
	 * 
	 * @throws DomainException If any of the parameters are invalid.
	 */
	public Campaign(
			final String id,
			final String name,
			final String description,
			final RunningState runningState, 
			final PrivacyState privacyState, 
			final Date creationTimestamp, 
			final String xml) 
			throws DomainException {
		
		if(runningState == null) {
			throw new DomainException("The running state is null.");
		}
		else if(privacyState == null) {
			throw new DomainException("The privacy state is null.");
		}
		else if(creationTimestamp == null) {
			throw new DomainException("The creation timestamp is null.");
		}
		else if(xml == null) {
			throw new DomainException("The XML is null.");
		}
		
		Document document;
		try {
			document = (new Builder()).build(new StringReader(xml));
		} 
		catch(IOException e) {
			// This should only be thrown if it can't read the 'xml', but
			// given that it is already in memory this should never happen.
			throw new DomainException("XML was unreadable.", e);
		}
		catch(XMLException e) {
			throw new DomainException("No usable XML parser could be found.", e);
		}
		catch(ValidityException e) {
			throw new DomainException("The XML is invalid.", e);
		}
		catch(ParsingException e) {
			throw new DomainException("The XML is not well formed.", e);
		}
		
		Element root = document.getRootElement();
		
		this.id = getId(root, id);
		this.name = getName(root, name);
		this.description = description;
		
		iconUrl = getIconUrl(root);
		authoredBy = getAuthoredBy(root);
		
		// Process all of the surveys.
		surveyMap = getSurveys(root);
		
		this.runningState = runningState;
		this.privacyState = privacyState;
		
		this.creationTimestamp = new DateTime(creationTimestamp);
		
		this.xml = root.toXML();
		
		userRoles = new HashMap<String, Collection<Role>>();
		classes = new LinkedList<String>();
	}
	
	/**
	 * Validates that some XML contains all required components of an ohmage
	 * XML document and that all values, even optional ones that are given, are
	 * valid values.
	 * 
	 * @param xml The XML as a String.
	 * 
	 * @return Returns a map with one key (the ID) and one value (the name).
	 * 
	 * @throws DomainException Thrown if the XML is not valid.
	 */
	public static Map<String, String> validateXml(
			final String xml) 
			throws DomainException {
		
		Document document;
		try {
			document = (new Builder()).build(new StringReader(xml));
		} 
		catch(IOException e) {
			// This should only be thrown if it can't read the 'xml', but
			// given that it is already in memory this should never happen.
			throw new DomainException("XML was unreadable.", e);
		}
		catch(XMLException e) {
			throw new DomainException("No usable XML parser could be found.", e);
		}
		catch(ValidityException e) {
			throw new DomainException("The XML is invalid.", e);
		}
		catch(ParsingException e) {
			throw new DomainException("The XML is not well formed.", e);
		}
		
		Element root = document.getRootElement();
		
		String id = getId(root, null);
		String name = getName(root, null);
		getIconUrl(root);
		getAuthoredBy(root);
		getSurveys(root);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put(id, name);
		return result;
	}

	/**
	 * Returns the configuration's unique identifier.
	 * 
	 * @return The configuration's unique identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the configuration's name.
	 * 
	 * @return The configuration's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the configuration's description.
	 * 
	 * @return The configuration's description. This may be null.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the URL for the icon for this configuration.
	 * 
	 * @return The URL for the icon for this configuration. This may be null.
	 */
	public URL getIconUrl() {
		return iconUrl;
	}
	
	/**
	 * Returns the string representing the creator of this campaign.
	 * 
	 * @return The string representing the creator of this campaign.
	 */
	public String getAuthoredBy() {
		return authoredBy;
	}

	/**
	 * Returns the configuration's current running state.
	 * 
	 * @return The configuration's current running state.
	 */
	public RunningState getRunningState() {
		return runningState;
	}

	/**
	 * Returns the configuration's current privacy state.
	 * 
	 * @return The configuration's current privacy state.
	 */
	public PrivacyState getPrivacyState() {
		return privacyState;
	}

	/**
	 * Returns the date and time the configuration was created.
	 * 
	 * @return The date and time the configuration was created.
	 */
	public DateTime getCreationTimestamp() {
		if(masks.size() == 0) {
			return creationTimestamp;
		}
		else {
			return masks.get(masks.size() - 1).getCreationTime();
		}
	}
	
	/**
	 * Returns the configuration defined as an XML string.
	 * 
	 * @return The configuration defined as an XML string.
	 * 
	 * @throws DomainException The XML was not valid.
	 */
	public String getXml() throws DomainException {
		String maskedXml = xml;
		
		// Only apply masks if any exist.
		if(masks.size() != 0) {
			// Parse the XML document.
			Document document;
			try {
				document = (new Builder()).build(new StringReader(xml));
			} 
			catch(IOException e) {
				// This should only be thrown if it can't read the 'xml', but
				// given that it is already in memory this should never happen.
				throw new DomainException("XML was unreadable.", e);
			}
			catch(XMLException e) {
				throw
					new DomainException(
						"No usable XML parser could be found.",
						e);
			}
			catch(ValidityException e) {
				throw new DomainException("The XML is invalid.", e);
			}
			catch(ParsingException e) {
				throw new DomainException("The XML is not well formed.", e);
			}
			
			// Get the root of the document.
			Element root = document.getRootElement();
			
			// The only relevant mask is the one that was created last.
			CampaignMask mask = masks.get(masks.size() - 1);
			
			// Get the list of survey and prompt IDs to retain.
			Map<String, Set<String>> surveyPromptMap = mask.getSurveyPromptMap();
			
			// Get all of the survey nodes.
			Nodes surveys = root.query("/campaign/surveys/survey");
			
			// Cycle through the surveys and remove those whose ID is not in
			// the map.
			int numSurveys = surveys.size();
			for(int i = 0; i < numSurveys; i++) {
				// Get the individual survey.
				Node survey = surveys.get(i);
				
				// Get the ID for this survey.
				Nodes surveyIdNode = survey.query("./id");
				if(surveyIdNode.size() == 0) {
					throw new DomainException("The survey is missing its ID.");
				}
				else if(surveyIdNode.size() > 1) {
					throw new DomainException("The survey has multiple IDs.");
				}
				String surveyId = surveyIdNode.get(0).getValue();
				
				// Get the desired prompt IDs for this survey.
				Set<String> promptIds = surveyPromptMap.get(surveyId);
				
				// If this ID is in the map, remove the undesired prompts.
				if(surveyPromptMap.containsKey(surveyId)) {
					// Get all of the prompts for this survey.
					Nodes prompts = survey.query("./contentList/prompt");
					
					// Cycle through the prompts removing those that are not in
					// this list.
					int numPrompts = prompts.size();
					for(int j = 0; j < numPrompts; j++) {
						// Get the individual prompt.
						Node prompt = prompts.get(j);
						
						// Get the ID for this prompt.
						Nodes promptIdNode = prompt.query("./id");
						if(promptIdNode.size() == 0) {
							throw
								new DomainException(
									"The prompt is missing its ID.");
						}
						else if(promptIdNode.size() > 1) {
							throw
								new DomainException(
									"The prompt has multiple IDs.");
						}
						String promptId = promptIdNode.get(0).getValue();
						
						// If the prompt is not in the mask, remove it.
						if(! promptIds.contains(promptId)) {
							prompt.getParent().removeChild(prompt);
						}
					}
				}
				// Otherwise, remove the survey completely.
				else {
					survey.getParent().removeChild(survey);
				}
			}
			
			// Finally, generate a new XML string from the masked XML.
			maskedXml = root.toXML();
		}
		
		return maskedXml;
	}
	
	/**
	 * Adds a user and an associated role to the list of users and their roles.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param role The user's configuration role.
	 * 
	 * @throws DomainException Thrown if the username or role is null.
	 */
	public void addUser(
			final String username, 
			final Role role) 
			throws DomainException {
		
		if(username == null) {
			throw new DomainException("The username is null.");
		}
		else if(role == null) {
			throw new DomainException("The role is null.");
		}
		
		Collection<Role> roles = userRoles.get(username);
		if(roles == null) {
			roles = new LinkedList<Role>();
			userRoles.put(username, roles);
		}
		roles.add(role);
	}
	
	/**
	 * Adds the map of usernames to their respective roles to the internal map.
	 * 
	 * @param usersAndRoles A map of usernames to a collection of their roles
	 * 						in this campaign.
	 */
	public void addUsers(final Map<String, Collection<Role>> usersAndRoles) {
		if(usersAndRoles == null) {
			return;
		}
		
		userRoles.putAll(usersAndRoles);
	}
	
	/**
	 * Returns the usernames of all the supervisors in a campaign.
	 * 
	 * @return A list of usernames for all of the supervisors of a campaign.
	 */
	public List<String> getSupervisors() {
		List<String> usernames = new LinkedList<String>();
		
		for(String username : userRoles.keySet()) {
			if(userRoles.get(username).contains(Campaign.Role.SUPERVISOR)) {
				usernames.add(username);
			}
		}
		
		return usernames;
	}
	
	/**
	 * Returns the usernames of all the authors in a campaign.
	 * 
	 * @return A list of usernames for all of the authors of a campaign.
	 */
	public List<String> getAuthors() {
		List<String> usernames = new LinkedList<String>();
		
		for(String username : userRoles.keySet()) {
			if(userRoles.get(username).contains(Campaign.Role.AUTHOR)) {
				usernames.add(username);
			}
		}
		
		return usernames;
	}
	
	/**
	 * Returns the usernames of all the analysts in a campaign.
	 * 
	 * @return A list of usernames for all of the analysts of a campaign.
	 */
	public List<String> getAnalysts() {
		List<String> usernames = new LinkedList<String>();
		
		for(String username : userRoles.keySet()) {
			if(userRoles.get(username).contains(Campaign.Role.ANALYST)) {
				usernames.add(username);
			}
		}
		
		return usernames;
	}
	
	/**
	 * Returns the usernames of all the participants in a campaign.
	 * 
	 * @return A list of usernames for all of the participants of a campaign.
	 */
	public List<String> getParticipants() {
		List<String> usernames = new LinkedList<String>();
		
		for(String username : userRoles.keySet()) {
			if(userRoles.get(username).contains(Campaign.Role.PARTICIPANT)) {
				usernames.add(username);
			}
		}
		
		return usernames;
	}
	
	/**
	 * Adds a collection of classes to the already existing collection of 
	 * classes.
	 * 
	 * @param classIds The collection of class IDs to add.
	 * 
	 * @throws DomainException Thrown if the class ID list is null.
	 */
	public void addClasses(
			final Collection<String> classIds) 
			throws DomainException {
		
		if(classIds == null) {
			throw new DomainException("The class ID list is null.");
		}
		
		classes.addAll(classIds);
	}
	
	/**
	 * Adds the campaign masks to this campaign.
	 * 
	 * @param masks The collection of masks to add.
	 */
	public void addMasks(final Collection<CampaignMask> masks) {
		if(masks == null) {
			return;
		}
		
		// Add the new masks, if any.
		this.masks.addAll(masks);
		
		// Sort them.
		Collections.sort(this.masks);
	}
	
	/**
	 * Returns the most recent mask for this user and campaign.
	 * 
	 * @return The most recent mask for this user and campaign or null if the
	 *         user has no masks.
	 */
	public CampaignMask getLatestMask() {
		if(masks.size() == 0) {
			return null;
		}
		else {
			return masks.get(masks.size() - 1);
		}
	}

	/**
	 * Returns the map of survey IDs to Survey objects.
	 * 
	 * @return The map of survey IDs to Survey objects.
	 */
	public Map<String, Survey> getSurveys() {
		return Collections.unmodifiableMap(surveyMap);
	}

	/**
	 * Returns whether or not a survey with a given identifier exists.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @return Whether or not a survey with the given ID exists.
	 * 
	 * @throws DomainException Thrown if the survey ID is null.
	 */
	public boolean surveyIdExists(
			final String surveyId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		
		return surveyMap.containsKey(surveyId);
	}
	
	/**
	 * Returns the title for a given survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @return The survey's title.
	 * 
	 * @throws DomainException Thrown if the survey ID is null.
	 */
	public String getSurveyTitleFor(
			final String surveyId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		
		if(surveyIdExists(surveyId)) {
			return surveyMap.get(surveyId).getTitle();
		}
		return null;
	}
	
	/**
	 * Returns the description for a given survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @return The survey's description.
	 * 
	 * @throws DomainException Thrown if the survey ID is null.
	 */
	public String getSurveyDescriptionFor(
			final String surveyId) 
			throws DomainException 
			{
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		
		if(surveyIdExists(surveyId)) {
			return surveyMap.get(surveyId).getDescription();
		}
		return null;
	}
	
	/**
	 * Retrieves the RepeatableSet object from a survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param repeatableSetId The repeatable set's configuration-unique 
	 * 						  identifier.
	 * 
	 * @return The RepeatableSet object from the survey.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration.
	 */
	public RepeatableSet getRepeatableSet(
			final String surveyId, 
			final String repeatableSetId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		else if(repeatableSetId == null) {
			throw new DomainException("The repeatable set ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new DomainException("The survey ID is unknown.");
		}
		
		Map<Integer, SurveyItem> prompts = survey.getSurveyItems();
		for(SurveyItem prompt : prompts.values()) {
			if(prompt instanceof RepeatableSet) {
				RepeatableSet repeatableSet = (RepeatableSet) prompt;
				
				if(repeatableSetId.equals(repeatableSet.getId())) {
					return repeatableSet;
				}
				
				RepeatableSet result = repeatableSet.getRepeatableSet(repeatableSetId);
				if(result != null) {
					return result;
				}
			}
		}
        
        return null;
	}

	/**
	 * Retrieves the prompt from the given survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return A Prompt object representing the prompt.
	 * 
	 * @throws DomainException Thrown if the survey ID or the prompt ID are  
	 * 						   null or if the survey doesn't exist in this
	 * 						   campaign.
	 */
	public Prompt getPrompt(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		else if(promptId == null) {
			throw new DomainException("The prompt ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new DomainException("The survey ID is unknown.");
		}
		
		return survey.getPrompt(promptId);
	}
	
	/**
	 * Returns whether or not a prompt may be skipped.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return Whether or not the prompt may be skipped.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   campaign or if the prompt doesn't exist in that 
	 * 						   survey.
	 */
	public boolean isPromptSkippable(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.skippable();
		}
	}

	/**
	 * Returns whether or not a prompt exists in a survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return Whether or not the prompt exists in the survey.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration.
	 */
	public boolean promptExists(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		return getPrompt(surveyId, promptId) != null;
	}

	/**
	 * Retrieves a prompt's type.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The prompt's type.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in
	 * 						   that survey.
	 * 
	 * @see org.ohmage.domain.campaign.Prompt.Type Prompt.Type
	 */
	public Type getPromptType(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException("No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getType();
		}
	}
	
	/**
	 * Returns whether or not the prompt is in a repeatable set.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return Whether or not the prompt is in a repeatable set.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in
	 * 						   that survey.
	 */
	public boolean isPromptInRepeatableSet(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		else if(promptId == null) {
			throw new DomainException("The prompt ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new DomainException("The survey ID is unknown.");
		}
		
		Map<Integer, SurveyItem> prompts = survey.getSurveyItems();
		for(SurveyItem prompt : prompts.values()) {
			if(prompt instanceof RepeatableSet) {
				Prompt currPrompt = ((RepeatableSet) prompt).getPrompt(promptId);
				
				if(currPrompt != null) {
					return true;
				}
			}
		}
        
        return false;
	}
	
	/**
	 * Returns whether or not the prompt contains single choice values.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return Whether or not the prompt contains single choice values.
	 * 
	 * @throws DomainException Thrown if the prompt ID is null or if the prompt
	 * 						   ID is not known.
	 */
	public boolean promptContainsSingleChoiceValues(
			final String promptId) 
			throws DomainException {
		
		if(promptId == null) {
			throw new DomainException("The prompt ID is null.");
		}
		
		Survey survey = getSurveyForPromptId(promptId);
		if(survey == null) {
			throw new DomainException("Unknown prompt ID: " + promptId);
		}
		
		SurveyItem abstractPrompt = survey.getSurveyItem(promptId);
		if(abstractPrompt == null) {
			// This is impossible given that we just got the survey ID from 
			// this prompt ID.
			throw new DomainException("The prompt ID has changed.");
		}
		
		if(abstractPrompt instanceof Prompt) {
			Prompt prompt = (Prompt) abstractPrompt;
			
			if(prompt instanceof SingleChoicePrompt) {
				SingleChoicePrompt singleChoicePrompt = (SingleChoicePrompt) prompt;
				
				return singleChoicePrompt.getChoices().values().size() != 0;
			}
		}
		
		return false;
	}

	/**
	 * Retrieves the number of prompts in some survey.
	 * 
	 * @param surveyId The surveys configuration-unique identifier.
	 * 
	 * @return The number of prompts in some survey.
	 * 
	 * @throws DomainException Thrown if the survey ID is null or if the survey
	 * 						   ID is unknown.
	 */
	public int getNumberOfPromptsInSurvey(
			final String surveyId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new DomainException(
					"There is no survey with the ID: " + surveyId);
		}
		
		return survey.getNumPrompts();
	}
	
	/**
	 * Retrieves the number of prompts in some repeatable set.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param repeatableSetId The repeatable set's configuration-unique 
	 * 						  identifier.
	 * 
	 * @return The number of prompts in the repeatable set.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID is 
	 * 						   null or if the survey ID or prompt ID are 
	 * 						   unknown or if the repeatable set ID doesn't 
	 * 						   refer to a repeatable set.
	 */
	public int numberOfPromptsInRepeatableSet(
			final String surveyId, 
			final String repeatableSetId) 
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		else if(repeatableSetId == null) {
			throw new DomainException("The repeatable set ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new DomainException("There is no survey with the ID: " + surveyId);
		}
		
		SurveyItem prompt = survey.getSurveyItems().get(repeatableSetId);
		if(prompt instanceof RepeatableSet) {
			RepeatableSet repeatableSet = (RepeatableSet) prompt;
			
			return repeatableSet.getNumPrompts();
		}
		else if(prompt == null) {
			throw new DomainException("There is no prompt with the ID: " + repeatableSetId);
		}
		else {
			throw new DomainException("The prompt is not a repeatable set: " + repeatableSetId);
		}
	}
	
	/**
	 * Retrieves the survey ID for some prompt.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The configuration-unique identifier for the survey to which the 
	 * 		   prompt belongs.
	 * 
	 * @throws DomainException Thrown if the prompt ID is null or unknown.
	 */
	public String getSurveyIdForPromptId(
			final String promptId) 
			throws DomainException {
		
		for(Survey survey : surveyMap.values()) {
			if(survey.getSurveyItem(promptId) != null) {
				return survey.getId();
			}
		}
		
		throw new DomainException("The prompt doesn't exist: " + promptId);
	}
	
	/**
	 * Retrieves the Survey object for some prompt.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The configuration-unique identifier for the survey to which the 
	 * 		   prompt belongs.
	 * 
	 * @throws DomainException Thrown if the prompt ID is null or unknown.
	 */
	public Survey getSurveyForPromptId(
			final String promptId) 
			throws DomainException {
		
		for(Survey survey : surveyMap.values()) {
			if(survey.getSurveyItem(promptId) != null) {
				return survey;
			}
		}
		
		throw new DomainException("The prompt doesn't exist: " + promptId);
	}

	/**
	 * Retrieves the prompt text for some prompt in some survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The prompt text for the prompt in the survey.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in 
	 * 						   that survey.
	 */
	public String getPromptTextFor(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getText();
		}
	}

	/**
	 * Retrieves the display label for some some prompt in some survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The display type for the prompt in the survey.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in 
	 * 						   that survey.
	 */
	public String getDisplayLabelFor(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getDisplayLabel();
		}
	}
	
	/**
	 * Retrieves the value for the choice, 'key', of the prompt in the survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @param key The choice's label.
	 * 
	 * @return The choice's value.
	 * 
	 * @throws DomainException Thrown if the survey ID or prompt ID are null or
	 * 						   if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in 
	 * 						   that survey or isn't a ChoicePrompt or if the 
	 * 						   label is unknown.
	 */
	public String getValueForChoiceKey(
			final String surveyId, 
			final String promptId, 
			final String key) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return getChoiceValueFrom(prompt, key);
		}
	}
	
	/**
	 * Retrieves the "value" from the key-value-label trio of a response.
	 * 
	 * @param prompt The ChoicePrompt.
	 * 
	 * @param label The response label.
	 * 
	 * @return The value associated with that response label. This may be null.
	 * 	
	 * @throws DomainException Thrown if the Prompt isn't a ChoicePrompt or the
	 * 						   label isn't know for the prompt.
	 */
	private String getChoiceValueFrom(
			final Prompt prompt, 
			final String label) 
			throws DomainException {
		Map<Integer, LabelValuePair> choices;
		if(prompt instanceof CustomChoicePrompt) {
			choices = ((CustomChoicePrompt) prompt).getAllChoices();
		}
		else if(prompt instanceof ChoicePrompt) {
			choices = ((ChoicePrompt) prompt).getChoices();
		}
		else {
			throw new DomainException("The prompt isn't a choice prompt.");
		}
		
		for(LabelValuePair valueLabelPair : choices.values()) {
			if(valueLabelPair.getLabel().equals(label)) {
				return valueLabelPair.getValue().toString();
			}
		}
		
		throw new DomainException("The label is not known.");
	}

	/**
	 * Returns the label for some choice key.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @param key The choice's key.
	 * 
	 * @return The choice's label.
	 * 
	 * @throws DomainException Thrown if the survey ID or repeatable set ID are
	 * 						   null or if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in 
	 * 						   that survey or isn't a ChoicePrompt or if the 
	 * 						   label is unknown.
	 */
	public String getLabelForChoiceKey(
			final String surveyId, 
			final String promptId, 
			final String key) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return getChoiceLabelFrom(prompt, key);
		}
	}
	
	/**
	 * Returns the mapping of choice keys to their value/label pairs.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The mapping of choice keys to their value/label pairs.
	 * 
	 * @throws DomainException Thrown if the survey ID or prompt ID are null or
	 * 						   if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in
	 * 						   that survey or if that prompt isn't a 
	 * 						   ChoicePrompt.
	 */
	public Map<Integer, LabelValuePair> getChoiceGlossaryFor(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			if(prompt instanceof CustomChoicePrompt) {
				return ((CustomChoicePrompt) prompt).getAllChoices();
			}
			else if(prompt instanceof ChoicePrompt) {
				return ((ChoicePrompt) prompt).getChoices();
			}
			else {
				throw new DomainException(
						"The prompt is not a choice prompt.");
			}
		}
	}
	
	/**
	 * Returns the unit value for some prompt.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The mapping of choice keys to their value/label pairs.
	 * 
	 * @throws DomainException Thrown if the survey ID or prompt ID are null or
	 * 						   if the survey doesn't exist in this 
	 * 						   configuration or if the prompt doesn't exist in 
	 * 						   that survey.
	 */
	public String getUnitFor(
			final String surveyId, 
			final String promptId) 
			throws DomainException {
		
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new DomainException(
					"No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getUnit().trim();
		}
	}
	
	/**
	 * Creates a JSONObject from this campaign.
	 * 
	 * @param withId Whether or not to include the ID.
	 * 
	 * @param withClasses Whether or not to include the list of classes.
	 * 
	 * @param withRoles Whether or not to include the list of roles for all of
	 * 					the users.
	 * 
	 * @param withParticipants If 'withRoles' is true, whether or not to 
	 * 						   include the list of participants.
	 * 
	 * @param withAnalysts If 'withRoles' is true, whether or not to include 
	 * 					   the list of analysts.
	 * 
	 * @param withAuthors If 'withRoles' is true, whether or not to include the
	 * 					  list of authors.
	 * 
	 * @param withSupervisors If 'withRoles' is true, whether or not to include
	 * 						  the list of supervisors.
	 * 
	 * @param withXml Whether or not to include the XML.
	 * 
	 * @return A JSONObject that represents this campaign.
	 * 
	 * @throws JSONException Thrown if there is an error creating the 
	 * 						 JSONObject.
	 */
	public JSONObject toJson(
			final boolean withId, 
			final boolean withClasses,
			final boolean withRoles, 
			final boolean withParticipants,
			final boolean withAnalysts, 
			final boolean withAuthors,
			final boolean withSupervisors, 
			final boolean withXml,
			final boolean withSurveys) 
			throws JSONException, DomainException{
		
		JSONObject result = new JSONObject();
		
		if(withId) {
			result.put(JSON_KEY_ID, id);
		}
		result.put(JSON_KEY_NAME, name);
		result.put(JSON_KEY_DESCRIPTION, (description == null) ? "" : description);
		result.put(JSON_KEY_ICON_URL, iconUrl);
		result.put(JSON_KEY_AUTHORED_BY, authoredBy);
		result.put(JSON_KEY_RUNNING_STATE, runningState.name().toLowerCase());
		result.put(JSON_KEY_PRIVACY_STATE, privacyState.name().toLowerCase());
		
		// If there is no mask, report the actual campaign creation timestamp.
		if(masks.size() == 0) {
			result
				.put(
					JSON_KEY_CREATION_TIMESTAMP,
					DateTimeUtils
						.getIso8601DateString(creationTimestamp, true));
		}
		// Otherwise, get the latest mask and use its creation timestamp as
		// this campaign's timestamp.
		else {
			result
				.put(
					JSON_KEY_CREATION_TIMESTAMP,
					DateTimeUtils
						.getIso8601DateString(
							masks.get(masks.size() - 1).getCreationTime(),
							true));
		}
		
		if(withClasses) {
			result.put(JSON_KEY_CLASSES, classes);
		}
		
		if(withRoles) {
			JSONObject roles = new JSONObject();
			
			if(withParticipants) {
				roles.put(JSON_KEY_PARTICIPANT, getParticipants());
			}
			if(withAnalysts) {
				roles.put(JSON_KEY_ANALYST, getAnalysts());
			}
			if(withAuthors) {
				roles.put(JSON_KEY_AUTHOR, getAuthors());
			}
			if(withSupervisors) {
				roles.put(JSON_KEY_SUPERVISOR, getSupervisors());
			}
			
			result.put(JSON_KEY_ROLES, roles);
		}
		
		if(withXml) {
			// Returned the masked XML.
			result.put(JSON_KEY_XML, getXml());
		}
		
		if(withSurveys) {
			// If any masks exist, get the most recent one's survey list.
			Map<String, Set<String>> surveyPromptMap = null;
			if(masks.size() > 0) {
				surveyPromptMap = masks.get(masks.size() - 1).getSurveyPromptMap();
			}
			
			JSONArray surveysArray = new JSONArray();
			for(Survey survey : surveyMap.values()) {
				// If the campaign is being masked and this survey isn't part
				// of the mask, skip it.
				if(
					(surveyPromptMap != null) && 
					(! surveyPromptMap.containsKey(survey.getId()))) {
						
					continue;
				}
				
				surveysArray.put( 
					survey.toJson(
						true,	// ID
						true,	// Title
						true,	// Description
						true,	// Intro Text
						true,	// Submit Text
						true,	// Anytime
						true,	// Prompts
						surveyPromptMap.get(survey.getId())
					)
				);
			}
			
			result.put(JSON_KEY_SURVEYS, surveysArray);
		}
		
		return result;
	}
	
	/**
	 * Generates a hash code value for this campaign.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Determines if this Campaign object is equal to a separate object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Campaign other = (Campaign) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	/**
	 * Validates that a survey ID conforms to our specification.
	 * 
	 * @param surveyId The survey ID to validate.
	 * 
	 * @return The validated survey ID.
	 * 
	 * @throws DomainException The survey ID is invalid.
	 */
	public static final String validateSurveyId(
			final String surveyId)
			throws DomainException {
		
		if(surveyId == null) {
			throw new DomainException("The survey ID is null.");
		}
		
		String trimmedSurveyId = surveyId.trim();
		if(trimmedSurveyId.length() == 0) {
			throw new DomainException(
					"The survey's ID cannot be only whitespace.");
		}
		else if(! VALID_CHARACTERS_PATTERN.matcher(trimmedSurveyId).matches()) {
			throw new DomainException(
				"The survey's ID contains illegal characters: " + 
					trimmedSurveyId);
		}
		
		return trimmedSurveyId;
	}
	
	/**
	 * Validates that a prompt ID conforms to our specification.
	 * 
	 * @param promptId The prompt ID to validate.
	 * 
	 * @param containerId If this validation was part of a larger object, this 
	 * 					  may be provided to be appended to the exception's
	 * 					  message.
	 * 
	 * @return The validate prompt ID.
	 * 
	 * @throws DomainException The prompt ID was invalid.
	 */
	public static final String validatePromptId(
			final String promptId,
			final String containerId)
			throws DomainException {
		
		if(promptId == null) {
			throw new DomainException("The prompt ID is null.");
		}
		
		String trimmedPromptId = promptId.trim();
		if(trimmedPromptId.length() == 0) {
			throw new DomainException(
				"The prompt's ID cannot be only whitespace" +
					((containerId == null) ? "." : (": " + containerId)));
		}
		if(! VALID_CHARACTERS_PATTERN.matcher(trimmedPromptId).matches()) {
			throw new DomainException(
				"The prompt's ID contains illegal characters: " + 
					trimmedPromptId);
		}
		
		return trimmedPromptId;
	}

	/**
	 * Returns the label from some prompt response key.
	 * 
	 * @param prompt A ChoicePrompt prompt.
	 * 
	 * @param key A choice key valid for the ChoicePrompt.
	 * 
	 * @return The choice's label value.
	 * 
	 * @throws DomainException Thrown if the prompt isn't a choice prompt, the 
	 * 						   'key' isn't a number, or the key is not a valid
	 * 						   choice for the prompt.
	 */
	private String getChoiceLabelFrom(
			final Prompt prompt, 
			final String key) 
			throws DomainException {
		
		Map<Integer, LabelValuePair> choices;
		if(prompt instanceof CustomChoicePrompt) {
			choices = ((CustomChoicePrompt) prompt).getAllChoices();
		}
		else if(prompt instanceof ChoicePrompt) {
			choices = ((ChoicePrompt) prompt).getChoices();
		}
		else {
			throw new DomainException("The prompt isn't a choice prompt.");
		}
		
		Integer keyInt;
		try {
			keyInt = Integer.decode(key);
		}
		catch(NumberFormatException e) {
			throw new DomainException("The key is not a number.", e);
		}
		
		LabelValuePair vlp = choices.get(keyInt);
		if(vlp == null) {
			throw new DomainException("The key is unknown.");
		}
		else {
			return vlp.getLabel();
		}
	}
	
	/**
	 * Checks that the campaign URN exists and is a valid URN as defined by
	 * us.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's identifier.
	 * 
	 * @throws DomainException The ID is missing, there are multiple IDs, or 
	 * 						   the ID fails validation.
	 */
	private static String getId(
		final Element root, 
		final String id) 
		throws DomainException {
		
		// Get the URN.
		String urn;
		Nodes ids = root.query(XML_ID);
		if(ids.size() == 0) {
			if(id == null) {
				throw new DomainException("The campaign ID is missing.");
			}
			else {
				urn = id;
				
				Element idElement = new Element("campaignUrn");
				idElement.appendChild(urn);
				root.appendChild(idElement);
			}
		}
		else if(ids.size() > 1) {
			throw new DomainException("Multiple campaign IDs were found.");
		}
		else {
			urn = ids.get(0).getValue().trim();
			
			if((id != null) && (! id.equals(urn))) {
				throw
					new DomainException(
						"An ID was given and was part of the XML, and they " +
							"do not match.");
			}
		}
		
		// Validate the URN.
		if(! StringUtils.isValidUrn(urn)) {
			throw new DomainException("The campaign ID is not valid.");
		}
		else if(urn.length() > MAX_ID_LENGTH) {
			throw new DomainException(
					"The campaign's ID cannot be longer than " +
						MAX_ID_LENGTH +
						" characters.");
		}
		
		// Return the URN.
		return urn;
	}
	
	/**
	 * Checks that the campaign name exists and is a valid name as defined by
	 * us.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @param name The name of the campaign.
	 * 
	 * @return The campaign's name.
	 * 
	 * @throws DomainException The name is missing, there are multiple names,
	 * 						   or the name fails validation.
	 */
	private static String getName(
		final Element root,
		final String name)
		throws DomainException {
		
		// Get the name.
		String internalName;
		Nodes names = root.query(XML_NAME);
		if(names.size() == 0) {
			if(name == null) {
				throw new DomainException("The campaign name is missing.");
			}
			else {
				internalName = name;
				
				Element nameElement = new Element("campaignName");
				nameElement.appendChild(internalName);
				root.appendChild(nameElement);
			}
		}
		else if(names.size() > 1) {
			throw new DomainException("Multiple campaign names were found.");
		}
		else {
			internalName = names.get(0).getValue().trim();
			
			if(StringUtils.isEmptyOrWhitespaceOnly(internalName)) {
				throw
					new DomainException(
						"The name tag exists but the value is empty.");
			}
			else if((name != null) && (! internalName.equals(name))) {
				throw
					new DomainException(
						"The given name and name from the XML do not match.");
			}
		}
		
		if(internalName.length() > MAX_NAME_LENGTH) {
			throw new DomainException(
					"The name cannot be longer than " +
						MAX_NAME_LENGTH +
						" characters.");
		}
		
		return internalName;
	}
	
	/**
	 * Checks that the icon URL exists and is a valid URL.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's icon URL or null if one wasn't present.
	 * 
	 * @throws DomainException Thrown if the icon URL is missing, there are 
	 * 						   multiple of them, or it is not a valid URL.
	 */
	private static URL getIconUrl(final Node root) throws DomainException {
		Nodes iconUrls = root.query(XML_ICON_URL);
		if(iconUrls.size() > 1) {
			throw new DomainException("Multiple icon URLs were found.");
		}
		else if(iconUrls.size() == 1) {
			String iconUrlString = iconUrls.get(0).getValue().trim();
			
			try {
				URL result = new URL(iconUrlString);
				
				if(iconUrlString.length() > MAX_ICON_URL_LENGTH) {
					throw new DomainException(
							"The server URL cannot be longer than " +
								MAX_ICON_URL_LENGTH +
								" characters.");
				}
				
				return result;
			}
			catch(MalformedURLException e) {
				throw new DomainException("The icon URL is not a valid URL.");
			}
		}
		
		return null;
	}
	
	/**
	 * Checks that the authored by value exists.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's authored by value or null if one wasn't present.
	 * 
	 * @throws DomainException Thrown if the authored by value is missing, or 
	 * 						   there are multiple of them.
	 */
	private static String getAuthoredBy(
			final Node root) 
			throws DomainException {
		
		Nodes authoredBy = root.query(XML_AUTHORED_BY);
		if(authoredBy.size() > 1) {
			throw new DomainException("Multiple authored by values were found.");
		}
		else if(authoredBy.size() == 1) {
			String authoredByString = authoredBy.get(0).getValue().trim();
			
			if(authoredByString.length() == 0) {
				throw new DomainException("The authored by value cannot be only whitespace.");
			}
			
			return authoredByString;
		}
		
		return null;
	}
	
	/**
	 * Checks that the 'surveys' tag exists and that it contains at least one
	 * survey.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return A map of survey IDs to their Survey object.
	 * 
	 * @throws DomainException Thrown if the surveys value is missing or there
	 * 						   are multiple of them or if there are no surveys.
	 */
	private static Map<String, Survey> getSurveys(
			final Node root) 
			throws DomainException {
		
		Nodes surveysNodes = root.query(XML_SURVEYS);
		if(surveysNodes.size() == 0) {
			throw new DomainException("The surveys value is missing.");
		}
		else if(surveysNodes.size() > 1) {
			throw new DomainException("Multiple surveys values were found.");
		}
		Node surveysNode = surveysNodes.get(0);
		
		Nodes surveyNodes = surveysNode.query(XML_SURVEY);
		List<Survey> surveys = processSurveys(surveyNodes);
		
		// Need at least 1 survey
		int numSurveys = surveys.size();
		if(numSurveys == 0) {
			throw new DomainException("No surveys were found.");
		}
		
		// check of surveyID uniqueness
		Map<String, Survey> result = new HashMap<String, Survey>(numSurveys);
		for(Survey survey : surveys) {
			if(result.put(survey.getId(), survey) != null) {
				throw new DomainException(
						"Mutiple surveys have the same unique identifier: " + 
							survey.getId());
			}
		}
		
		return result;
	}
	
	/**
	 * Processes the survey Nodes and returns a list of Survey objects.
	 * 
	 * @param surveys A Nodes object pointing to all of the surveys.
	 * 
	 * @return A list of Survey objects for each survey in the XML.
	 * 
	 * @throws DomainException Thrown if one of the surveys is malformed.
	 */
	private static List<Survey> processSurveys(
			final Nodes surveys) 
			throws DomainException {
		
		int numSurveys = surveys.size();
		List<Survey> result = new ArrayList<Survey>(numSurveys);
		
		// Create a set for all of the surveys' items' ID.
		Set<String> itemIds = new HashSet<String>();
		
		// Cycle through the nodes and create Survey objects.
		for(int i = 0; i < numSurveys; i++) {
			// Create the Survey.
			Survey survey = processSurvey(surveys.get(i));
			
			// Add the survey to the list.
			result.add(survey);
			
			// Cycle through the items and check if any duplicate item IDs
			// exist.
			for(SurveyItem surveyItem : survey.getSurveyItems().values()) {
				if(! itemIds.add(surveyItem.getId())) {
					throw
						new DomainException(
							"Multiple survey items have the same ID: " + 
								surveyItem.getId());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Processes a survey Node and returns a Survey object.
	 * 
	 * @param survey A Node representing a survey.
	 * 
	 * @return A Survey object representing this survey Node from the XML.
	 * 
	 * @throws DomainException Thrown if the survey is invalid.
	 */
	private static Survey processSurvey(
			final Node survey) 
			throws DomainException {
		
		Nodes ids = survey.query(XML_SURVEY_ID);  // required
		if(ids.size() == 0) {  
			throw new DomainException("The survey ID is missing.");
		}
		else if(ids.size() > 1) {
			throw new DomainException(
					"Multiple survey IDs were found for the same survey.");
		}
		String id = validateSurveyId(ids.get(0).getValue());
		
		Nodes titles = survey.query(XML_SURVEY_TITLE);  // required
		if(titles.size() == 0) {  
			throw new DomainException("The survey title is missing: " + id);
		}
		else if(titles.size() > 1) {
			throw new DomainException(
					"Multiple survey titles were found for the same survey: " +
						id);
		}
		String title = titles.get(0).getValue().trim();
		if(title.length() == 0) {
			throw new DomainException(
					"The title cannot be only whitespace: " +
						id);
		}
		
		String description = null;
		Nodes descriptions = survey.query(XML_SURVEY_DESCRIPTION);  // optional
		if(descriptions.size() > 1) {
			throw new DomainException(
					"Multiple survey descriptions were found for the same survey: " + 
						id);
		}
		else if(descriptions.size() == 1) {
			description = descriptions.get(0).getValue().trim();
			
			if(description.length() == 0) {
				throw new DomainException(
						"The survey's description cannot be only whitespace: " +
							id);
			}
		}
		
		String introText = null;
		Nodes introTexts = survey.query(XML_SURVEY_INTRO_TEXT);  // optional
		if(introTexts.size() > 1) {
			throw new DomainException(
					"Multiple survey intro texts were found for the same survey: " + 
						id);
		}
		else if(introTexts.size() == 1) {
			introText = introTexts.get(0).getValue().trim();
			
			if(introText.length() == 0) {
				throw new DomainException(
						"The survey's intro text cannot be only whitespace: " +
							id);
			}
		}
		
		Nodes submitTexts = survey.query(XML_SURVEY_SUBMIT_TEXT);  // required
		if(submitTexts.size() == 0) {
			throw new DomainException("The survey submit text is missing.");
		}
		else if(submitTexts.size() > 1) {
			throw new DomainException(
					"Multiple survey submit texts were found for the same survey: " + 
						id);
		}
		String submitText = submitTexts.get(0).getValue().trim();
		if(submitText.length() == 0) {
			throw new DomainException(
					"The survey's submit text cannot be only whitespace: " +
						id);
		}
		
		Nodes anytimes = survey.query(XML_SURVEY_ANYTIME);  // required
		if(anytimes.size() == 0) {
			throw new DomainException(
					"The survey anytime value is missing: " + id);
		}
		else if(anytimes.size() > 1) {
			throw new DomainException(
					"Multiple survey anytime values were found for the same survey: " + 
						id);
		}
		Boolean anytime = StringUtils.decodeBoolean(anytimes.get(0).getValue().trim());
		if(anytime == null) {
			throw new DomainException(
					"The anytime value is not a valid boolean value: " + id);
		}
		
		Nodes contentLists = survey.query(XML_SURVEY_CONTENT_LIST);  // required
		if(contentLists.size() == 0) {
			throw new DomainException(
					"The survey content list is missing: " + id);
		}
		else if(contentLists.size() > 1) {
			throw new DomainException(
					"Multiple survey content lists were found: " + id);
		}
		Node contentList = contentLists.get(0);
		List<SurveyItem> promptsList = 
			processContentList(id, contentList.query(XML_CONTENT_LIST_ITEMS));
	
		// check for promptID uniqueness within each survey
		Map<Integer, SurveyItem> prompts = 
			new HashMap<Integer, SurveyItem>(promptsList.size());
		Set<String> promptIds = new HashSet<String>();
		for(SurveyItem prompt : promptsList) {
			if(promptIds.add(prompt.getId())) {
				prompts.put(prompt.getIndex(), prompt);
			}
			else {
				throw new DomainException(
						"Multiple prompts have the same unqiue identifier in a group of survey items: " + 
							prompt.getId());
			}
		}
		
		return
			new Survey(
				id, 
				title, 
				description, 
				introText, 
				submitText,
				anytime,
				prompts);
	}
	
	/**
	 * Processes the content list items from the XML.
	 * 
	 * @param surveyId The unique identifier for the survey that is contains
	 * 				   this content list.
	 * 
	 * @param contentListItems The Nodes from the XML that define the content
	 * 						   list.
	 * 
	 * @return A list of AbstractPrompts that were generated from the XML.
	 * 
	 * @throws DomainException Thrown if one of the content list items is 
	 * 						   malformed.
	 */
	private static List<SurveyItem> processContentList(
			final String surveyId,
			final Nodes contentListItems) 
			throws DomainException {
		
		int numItems = contentListItems.size();
		List<SurveyItem> result = new ArrayList<SurveyItem>(numItems);  // order matters
		
		for(int i = 0; i < numItems; i++) {
			SurveyItem.Type contentListItem;
			try {
				contentListItem = SurveyItem.Type.getValue(
						((Element) contentListItems.get(i)).getLocalName());
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						"There were unknown content list items found: " + 
							surveyId, 
						e);
			}
				
			switch(contentListItem) {
			case MESSAGE:
				result.add(
						processMessage(
								surveyId, 
								contentListItems.get(i), 
								i,
								result));
				break;
				
			case REPEATABLE_SET:
				result.add(
						processRepeatableSet(
								surveyId, 
								contentListItems.get(i), 
								i,
								result));
				break;
				
			case PROMPT:
				result.add(
						processPrompt(
								surveyId, 
								contentListItems.get(i), 
								i,
								result));
				break;
				
			default:
				throw new DomainException(
						"There are new content list items but no matching processor for survey '" + 
							surveyId + 
							"': " + 
							contentListItem);
			}
		}
		
		return result;
	}
	
	/**
	 * Creates a Message object from the message Node from the XML.
	 * 
	 * @param containerId The unique identifier for the container that contains
	 * 					  this message.
	 * 
	 * @param message The message Node from the XML.
	 * 
	 * @param index The index of this message in its list of survey items.
	 * 
	 * @return A Message object representing the message defined in the XML.
	 * 
	 * @throws DomainException The message was malformed.
	 */
	private static Message processMessage(
			final String containerId,
			final Node message, 
			final int index,
			final List<SurveyItem> alreadyProcessedItemsInSurveyItemGroup) 
			throws DomainException {
		
		Nodes ids = message.query(XML_MESSAGE_ID);  //required
		if(ids.size() == 0) {
			throw new DomainException(
					"The message ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new DomainException(
					"Multiple message IDs were found: " + containerId);
		}
		String id = ids.get(0).getValue().trim();
		if(id.length() == 0) {
			throw new DomainException(
					"The message's ID cannot be only whitespace in the survey item container: " +
						containerId);
		}
		if(! VALID_CHARACTERS_PATTERN.matcher(id).matches()) {
			throw new DomainException(
					"The message's ID contains illegal characters: " + id);
		}
		
		String condition = null;
		Nodes conditions = message.query(XML_MESSAGE_CONDITION);  // optional
		if(conditions.size() > 1) {
			throw new DomainException(
					"Multiple message conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();
			
			condition = validateCondition(
					condition, 
					id, 
					containerId, 
					alreadyProcessedItemsInSurveyItemGroup);
		}
		
		Nodes texts = message.query(XML_MESSAGE_TEXT);  // required
		if(texts.size() == 0) {
			throw new DomainException(
					"The message text is missing: " + id);
		}
		else if(texts.size() > 1) {
			throw new DomainException(
					"Multiple message texts were found: " + id);
		}
		String text = texts.get(0).getValue().trim();
		if(text.length() == 0) {
			throw new DomainException(
					"The text cannot be only whitespace: " +
						id);
		}
		
		return new Message(id, condition, index, text);
	}
	
	/**
	 * Creates a RepeatableSet object from the repeatable set Node from the 
	 * XML.
	 * 
	 * @param containerId The unique identifier for the container of this
	 * 					  repeatable set.
	 * 
	 * @param repeatableSet The repeatable set Node from the XML.
	 * 
	 * @param index The index of this repeatable set in its collection of 
	 * 				survey items.
	 * 
	 * @return A RepeatableSet object that represents the repeatable set 
	 * 		   defined in the XML.
	 * 
	 * @throws DomainException Thrown if the repeatable set is malformed or if 
	 * 						   any of the prompts in the repeatable set are 
	 * 						   malformed.
	 */
	private static RepeatableSet processRepeatableSet(
			final String containerId,
			final Node repeatableSet, 
			final int index,
			final List<SurveyItem> alreadyProcessedItemsInSurveyItemGroup) 
			throws DomainException {
		
		Nodes ids = repeatableSet.query(XML_REPEATABLE_SET_ID);
		if(ids.size() == 0) {
			throw new DomainException(
					"The repeatable set ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set IDs were found: " + containerId);
		}
		String id = ids.get(0).getValue().trim();
		if(id.length() == 0) {
			throw new DomainException(
					"The repeatable set's ID cannot be only whitespace in the survey item container: " +
						containerId);
		}
		if(! VALID_CHARACTERS_PATTERN.matcher(id).matches()) {
			throw new DomainException(
					"The repeatable set's ID contains illegal characters: " + 
						id);
		}
		
		String condition = null;
		Nodes conditions = repeatableSet.query(XML_REPEATABLE_SET_CONDITION);
		if(conditions.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();

			condition = validateCondition(
					condition, 
					id, 
					containerId, 
					alreadyProcessedItemsInSurveyItemGroup);
		}
		
		Nodes teminationQuestions =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_QUESTION);
		if(teminationQuestions.size() == 0) {
			throw new DomainException(
					"The repeatable set termination question is missing: " + 
						id);
		}
		else if(teminationQuestions.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set termination questions were found: " + 
						id);
		}
		String terminationQuestion = teminationQuestions.get(0).getValue().trim();
		if(terminationQuestion.length() == 0) {
			throw new DomainException(
					"The termination question cannot be only whitespace: " +
						id);
		}
		
		Nodes terminationTrueLabels =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_TRUE_LABEL);
		if(terminationTrueLabels.size() == 0) {
			throw new DomainException(
					"The repeatable set termination true label is missing: " + 
						id);
		}
		else if(terminationTrueLabels.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set termination true labels were found: " + 
						id);
		}
		String terminationTrueLabel = terminationTrueLabels.get(0).getValue().trim();
		if(terminationTrueLabel.length() == 0) {
			throw new DomainException(
					"The termination true label cannot be only whitespace: " +
						id);
		}
		
		Nodes terminationFalseLabels =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_FALSE_LABEL);
		if(terminationFalseLabels.size() == 0) {
			throw new DomainException(
					"The repeatable set termination false label is missing: " +
						id);
		}
		else if(terminationFalseLabels.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set termination false labels were found: " + 
						id);
		}
		String terminationFalseLabel = terminationFalseLabels.get(0).getValue().trim();
		if(terminationFalseLabel.length() == 0) {
			throw new DomainException(
					"The termination false label cannot be only whitespace: " +
						id);
		}
		
		Nodes terminationSkipEnableds =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_SKIP_ENABLED);
		if(terminationSkipEnableds.size() == 0) {
			throw new DomainException(
					"The repeatable set termination skip enabled value is missing: " + 
						id);
		}
		else if(terminationSkipEnableds.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set termination skip enabled value were found: " + 
						id);
		}
		Boolean terminationSkipEnabled = 
			StringUtils.decodeBoolean(
					terminationSkipEnableds.get(0).getValue().trim()
				);
		if(terminationSkipEnabled == null) {
			throw new DomainException(
					"The termination skip enabled value is not a valid boolean value: " + 
						id);
		}
		
		String terminationSkipLabel = null;
		Nodes terminationSkipLabels = 
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_SKIP_LABEL);
		if((terminationSkipLabels.size() == 0) && (terminationSkipEnabled)) {
			throw new DomainException(
					"A termination skip label is required if the termination skip is enabled: " +
						id);
		}
		else if(terminationSkipLabels.size() > 1) {
			throw new DomainException(
					"Multiple repeatable set termination skip labels were found: " + 
						id);
		}
		else if(terminationSkipLabels.size() == 1) {
			terminationSkipLabel = terminationSkipLabels.get(0).getValue().trim();
			
			if(terminationSkipLabel.length() == 0) {
				throw new DomainException(
						"The termination skip label cannot be only whitespace: " +
							id);
			}
		}
		
		Nodes promptsNodes = repeatableSet.query(XML_REPEATABLE_SET_PROMPTS);
		if(promptsNodes.size() == 0) {
			throw new DomainException(
					"The repeatable set doesn't contain a prompts group: " + 
						id);
		}
		if(promptsNodes.size() > 1) {
			throw new DomainException(
					"The repeatable set contains multiple prompts groups: " + 
						id);
		}
		
		Nodes promptNodes = promptsNodes.get(0).query(XML_CONTENT_LIST_ITEMS);
		List<SurveyItem> promptGroup = processContentList(id, promptNodes);
		if(promptGroup.size() == 0) {
			throw new DomainException(
					"The repeatable set doesn't contain any prompts: " + id);
		}
		
		Map<Integer, SurveyItem> promptMap = 
			new HashMap<Integer, SurveyItem>(promptGroup.size());
		Set<String> promptIds = new HashSet<String>();
		for(SurveyItem currPrompt : promptGroup) {
			if(promptIds.add(currPrompt.getId())) {
				// This is where repeatable sets prevent sub repeatable sets.
				// To allow this restriction, remove this if statement.
				if(currPrompt instanceof RepeatableSet) {
					throw new DomainException(
							"Repeatable sets may not contain repeatable sets: " + 
								id);
				}

				promptMap.put(currPrompt.getIndex(), currPrompt);
			}
			else {
				throw new DomainException(
						"A repeatable set has multiple prompts with the same unique identifier: " + 
							currPrompt.getId());
			}
		}
		
		return new RepeatableSet(id, condition, terminationQuestion,
				terminationTrueLabel, terminationFalseLabel, 
				terminationSkipEnabled, terminationSkipLabel, 
				promptMap, index);
	}
	
	// FIXME: Hackalicious. This is a hot-fix so that concurrency won't break 
	// condition parsing. We need to fix the condition parser to not be static
	// as soon as possible!
	private static final Lock parserLock = new ReentrantLock();
	
	/**
	 * Creates a Prompt object based on the XML prompt Node.
	 * 
	 * @param containerId The unique identifier for the container that contains
	 * 					  this prompt.
	 * 
	 * @param prompt The XML prompt node.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A Prompt object representing the prompt defined in the XML.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt is malformed.
	 */
	private static Prompt processPrompt(
			final String containerId, 
			final Node prompt, 
			final int index,
			final List<SurveyItem> alreadyProcessedItemsInSurveyItemGroup) 
			throws DomainException {
		
		Nodes ids = prompt.query(XML_PROMPT_ID);  // required
		if(ids.size() == 0) {
			throw new DomainException(
					"The prompt ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new DomainException(
					"Multiple prompt IDs were found: " + containerId);
		}
		String id = validatePromptId(ids.get(0).getValue(), containerId);
		
		String condition = null;
		Nodes conditions = prompt.query(XML_PROMPT_CONDITION);  //optional
		if(conditions.size() > 1) {
			throw new DomainException(
					"Multiple prompt conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();
			
			condition = validateCondition(
					condition, 
					id, 
					containerId, 
					alreadyProcessedItemsInSurveyItemGroup);
		}
		
		String unit = null;
		Nodes units = prompt.query(XML_PROMPT_UNIT);  //optional
		if(units.size() > 1) {
			throw new DomainException(
					"Multiple prompt units were found: " + id);
		}
		else if(units.size() == 1) {
			unit = units.get(0).getValue().trim();
			
			if(unit.length() == 0) {
				throw new DomainException(
						"The prompt's unit cannot be only whitespace: " +
							id);
			}
		}
		
		Nodes texts = prompt.query(XML_PROMPT_TEXT);  // required
		if(texts.size() == 0) {
			throw new DomainException(
					"The prompt text is missing: " + id);
		}
		else if(texts.size() > 1) {
			throw new DomainException(
					"Multiple prompt texts were found: " + id);
		}
		String text = texts.get(0).getValue().trim();
		if(text.length() == 0) {
			throw new DomainException(
					"The prompt's text cannot be whitespace only: " + id);
		}
		
		String explanationText = null;
		Nodes explanationTexts = prompt.query(XML_PROMPT_EXPLANATION_TEXT);  // optional
		if(explanationTexts.size() > 1) {
			throw new DomainException(
					"Multiple prompt explanation texts were found: " + id);
		}
		else if(explanationTexts.size() == 1) {
			explanationText = explanationTexts.get(0).getValue().trim();
			
			if(explanationText.length() == 0) {
				throw new DomainException(
						"The prompt's explanation text cannot be whitespace only: " + 
							id);
			}
		}
		
		Nodes skippables = prompt.query(XML_PROMPT_SKIPPABLE);  // required
		if(skippables.size() == 0) {
			throw new DomainException(
					"The prompt skippable is missing: " + id);
		}
		else if(skippables.size() > 1) {
			throw new DomainException(
					"Multiple prompt skippable values were found: " + id);
		}
		Boolean skippable = StringUtils.decodeBoolean(skippables.get(0).getValue());
		if(skippable == null) {
			throw new DomainException(
					"The prompt skippable value was not a valid boolean value: " + 
						id);
		}
		
		String skipLabel = null;
		Nodes skipLabels = prompt.query(XML_PROMPT_SKIP_LABEL);  // reqired if skippable
		if((skipLabels.size() == 0) && (skippable)) {
			throw new DomainException(
					"The skip label cannot be null if the prompt is skippable: " +
						id);
		}
		if(skipLabels.size() > 1) {
			throw new DomainException(
					"Multiple prompt skip labels were found: " + id);
		}
		else if(skipLabels.size() == 1) {
			skipLabel = skipLabels.get(0).getValue().trim();
			
			if(skipLabel.length() == 0) {
				throw new DomainException(
						"The prompt's skip label cannot be whitespace only: " +
							id);
			}
		}
		
		Nodes displayLabels = prompt.query(XML_PROMPT_DISPLAY_LABEL);  // required
		if(displayLabels.size() == 0) {
			throw new DomainException(
					"The prompt display label is missing: " + id);
		}
		else if(displayLabels.size() > 1) {
			throw new DomainException(
					"Multiple prompt display labels were found: " + id);
		}
		String displayLabel = displayLabels.get(0).getValue().trim();
		if(displayLabel.length() == 0) {
			throw new DomainException(
					"The prompt's display label cannot be whitespace only: " +
						id);
		}
		
		String defaultValue = null;
		Nodes defaultValues = prompt.query(XML_PROMPT_DEFAULT);  // optional
		if(defaultValues.size() > 1) {
			throw new DomainException(
					"Multiple default values were found: " + id);
		}
		else if(defaultValues.size() == 1) {
			defaultValue = defaultValues.get(0).getValue().trim();
		}
		
		Nodes promptTypes = prompt.query(XML_PROMPT_TYPE);
		if(promptTypes.size() == 0) {
			throw new DomainException("The prompt type is invalid: " + id);
		}
		else if(promptTypes.size() > 1) {
			throw new DomainException(
					"Multiple prompt types were found: " + id);
		}
		Prompt.Type type;
		try {
			type = Prompt.Type.valueOf(promptTypes.get(0).getValue().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The prompt type is unknown: " + id);
		}
		
		Map<String, LabelValuePair> properties;
		Nodes propertiesNodes = prompt.query(XML_PROMPT_PROPERTIES);  // optional
		if(propertiesNodes.size() > 1) {
			throw new DomainException(
					"Multiple properties groups found: " + id);
		}
		else if(propertiesNodes.size() == 1) {
			properties = getKeyValueLabelTrios(id, propertiesNodes.get(0).query(XML_PROMPT_PROPERTY));
		}
		else {
			properties = new HashMap<String, LabelValuePair>(0);
		}

		switch(type) {
		case AUDIO:
			return processAudio(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);
			
		case DOCUMENT:
			return processDocument(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case HOURS_BEFORE_NOW:
			return processHoursBeforeNow(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);
			
		case MULTI_CHOICE:
			return processMultiChoice(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case MULTI_CHOICE_CUSTOM:
			return processMultiChoiceCustom(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case NUMBER:
			return processNumber(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case PHOTO:
			return processPhoto(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case REMOTE_ACTIVITY:
			return processRemoteActivity(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case SINGLE_CHOICE:
			return processSingleChoice(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case SINGLE_CHOICE_CUSTOM:
			return processSingleChoiceCustom(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case TEXT:
			return processText(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case TIMESTAMP:
			return processTimestamp(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);

		case VIDEO:
			return processVideo(
				id,
				condition,
				unit,
				text,
				explanationText,
				skippable,
				skipLabel,
				displayLabel,
				defaultValue,
				properties,
				index);
			
		default:
			return null;
		}
	}
	
	/**
	 * Validates a condition string to ensure that it is well formed, that each
	 * of the prompts in the condition exist before this prompt and in this
	 * survey item container and are allowed to be referenced in a condition,
	 * and that the value associated with that prompt is both applicable and
	 * within the bounds of that instance of that prompt.
	 * 
	 * @param condition The condition string to be validated.
	 * 
	 * @param surveyItemId The ID of the survey item that owns this condition.
	 * 
	 * @param surveyItemContainerId The ID of the survey item that owns the
	 * 								survey item that owns this condition.
	 * 
	 * @param alreadyProcessedItemsInSurveyItemGroup The list of prompts that
	 * 												 have already been 
	 * 												 processed in the order 
	 * 												 that they were processed.
	 * 
	 * @return The condition, validated.
	 * 
	 * @throws DomainException Thrown if the condition is invalid for any 
	 * 						   reason.
	 */
	private static String validateCondition(
			final String condition,
			final String surveyItemId,
			final String surveyItemContainerId,
			final List<SurveyItem> alreadyProcessedItemsInSurveyItemGroup) 
			throws DomainException {
		
		Map<String, List<ConditionValuePair>> promptIdAndConditionValues;
		try {
			parserLock.lock();
			promptIdAndConditionValues = 
					ConditionValidator.validate(condition);
		}
		catch(ConditionParseException e) {
			throw new DomainException(e.getMessage(), e);
		}
		finally {
			parserLock.unlock();
		}
		
		for(String promptId : promptIdAndConditionValues.keySet()) {
			// Validate that the prompt exists and comes before this prompt. 
			// This can only, and must, be true if we have already validated 
			// it.
			SurveyItem conditionSurveyItem = null;
			for(SurveyItem surveyItem : alreadyProcessedItemsInSurveyItemGroup) {
				if(surveyItem.getId().equals(promptId)) {
					if(surveyItem instanceof Prompt) {
						conditionSurveyItem = surveyItem;
						break;
					}
					else {
						throw new DomainException(
							"Only prompts values may be part of a condition. The offending ID is " 
							    + promptId + " and the parent id is " + surveyItemContainerId);
					}
				}
			}
			if(conditionSurveyItem == null) {
				throw new DomainException(
						"The prompt is unknown: " + promptId);
			}
			
			// Validate all of the condition-valid pairs for the prompt.
			for(ConditionValuePair pair : promptIdAndConditionValues.get(promptId)) {
				try {
					conditionSurveyItem.validateCondition(pair);
				}
				catch(DomainException e) {
					throw new DomainException(
							"The condition was invalid for the prompt '" +
								surveyItemId +
								"' in the prompt group '" +
								surveyItemContainerId +
								"': " +
								e.getMessage(),
							e);
				}
			}
		}
		
		return condition;
	}
	
	/**
	 * Process the property Nodes from the XML and creates a mapping of all of
	 * the keys to label/value pairs.
	 * 
	 * @param properties The properties for the prompt in the XML.
	 * 
	 * @return A mapping of property keys to their label/value pairs.
	 * 
	 * @throws DomainException Thrown if a property is invalid.
	 */
	private static Map<String, LabelValuePair> getKeyValueLabelTrios(
			final String containerId, 
			final Nodes properties) 
			throws DomainException {

		int numProperties = properties.size();
		if(numProperties == 0) {
			return Collections.emptyMap();
		}

		Map<String, LabelValuePair> result = new HashMap<String, LabelValuePair>(numProperties);
		
		for(int i = 0; i < numProperties; i++) {
			Node propertyNode = properties.get(i);
			
			Nodes keys = propertyNode.query(XML_PROPERTY_KEY);  // required
			if(keys.size() == 0) {
				throw new DomainException(
						"The property key is missing: " + containerId);
			}
			else if(keys.size() > 1) {
				throw new DomainException(
						"Multiple property keys were found: " + containerId);
			}
			String key = keys.get(0).getValue().trim();
			if(key.length() == 0) {
				throw new DomainException(
						"The property key cannot be whitespace only: " +
							containerId);
			}
			
			Nodes labels = propertyNode.query(XML_PROPERTY_LABEL);  // required
			if(labels.size() == 0) {
				throw new DomainException(
						"The property label is missing: " + containerId);
			}
			else if(labels.size() > 1) {
				throw new DomainException(
						"Multiple property labels were found: " + containerId);
			}
			String label = labels.get(0).getValue().trim();
			
			Number value = null;
			Nodes values = propertyNode.query(XML_PROPERTY_VALUE);  // optional
			if(values.size() > 1) {
				throw new DomainException(
						"Multiple property values found: " + containerId);
			}
			else if(values.size() == 1) {
				String valueString = values.get(0).getValue().trim();
				// extract numerical value
				try {   
					value = Short.decode(valueString);
				}
				catch(NumberFormatException notShort) {
					try {
						value = Integer.decode(valueString);
					}
					catch(NumberFormatException notInteger) {
						try {
							value = Long.decode(valueString);
						}
						catch(NumberFormatException notLong) {
							try {
								value = Float.parseFloat(valueString);
							}
							catch(NumberFormatException notFloat) {
								try {
									value = Double.parseDouble(valueString);
								}
								catch(NumberFormatException notDouble) {
									throw new DomainException(
											"The property value is not a numeric value: " + 
												containerId);
								}
							}
						}
					}
				}
			}
			
			if(result.put(key, new LabelValuePair(label, value)) != null) {
				throw new DomainException(
						"Multiple properties with the same key were found for the container with id: " + 
							containerId);
			}
		}
		
		return result;
	}
	
	/**
	 * Processes an audio prompt and returns an AudioPrompt object.
	 * 
	 * @param id
	 *        The prompt's unique identifier.
	 * 
	 * @param condition
	 *        The condition value.
	 * 
	 * @param unit
	 *        The prompt's visualization unit.
	 * 
	 * @param text
	 *        The prompt's text value.
	 * 
	 * @param explanationText
	 *        The prompt's explanation text value.
	 * 
	 * @param skippable
	 *        Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel
	 *        The label to show to skip this prompt.
	 * 
	 * @param displayLabel
	 *        The label for this display type.
	 * 
	 * @param defaultValue
	 *        The default value given in the XML.
	 * 
	 * @param properties
	 *        The properties defined in the XML for this prompt.
	 * 
	 * @param index
	 *        The index of this prompt in its collection of survey items.
	 * 
	 * @return An AudioPrompt object.
	 * 
	 * @throws DomainException
	 *         Thrown if the required properties are missing or if any of the
	 *         parameters are invalid.
	 */
	private static AudioPrompt processAudio(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Long maxDuration = null;
		try {
			LabelValuePair maxDurationVlp =
				properties.get(AudioPrompt.XML_KEY_MAX_DURATION);
			
			if(maxDurationVlp != null) {
				maxDuration = 
					Long.decode(maxDurationVlp.getLabel());
			}
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						AudioPrompt.XML_KEY_MAX_DURATION +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		if(defaultValue != null) {
			throw new DomainException(
					"Default values are not allowed for audio prompts: " +
						id);
		}

		return new AudioPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			index,
			maxDuration);
	}
	
	/**
	 * Processes a hours-before-now prompt and returns a HoursBeforeNowPrompt
	 * object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A HoursBeforeNowPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static HoursBeforeNowPrompt processHoursBeforeNow(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel, 
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		// Create the decimal parser.
		DecimalFormat format = new DecimalFormat();
		format.setParseBigDecimal(true);
		
		BigDecimal min;
		try {
			LabelValuePair minVlp = 
				properties.get(HoursBeforeNowPrompt.XML_KEY_MIN);
			
			if(minVlp == null) {
				throw new DomainException(
						"Missing the '" +
							HoursBeforeNowPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			
			min = (BigDecimal) format.parse(minVlp.getLabel());
		}
		catch(ParseException e) {
			throw new DomainException(
					"The '" +
						HoursBeforeNowPrompt.XML_KEY_MIN +
						"' property is not a valid number: " +
						id, 
					e);
		}
		
		BigDecimal max;
		try {
			LabelValuePair maxVlp = 
				properties.get(HoursBeforeNowPrompt.XML_KEY_MAX);
			
			if(maxVlp == null) {
				throw new DomainException(
						"Missing the '" +
							HoursBeforeNowPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			
			max = (BigDecimal) format.parse(maxVlp.getLabel());
		}
		catch(ParseException e) {
			throw new DomainException(
					"The '" +
						HoursBeforeNowPrompt.XML_KEY_MAX +
						"' property is not valid number: " +
						id, 
					e);
		}
		
		BigDecimal parsedDefaultValue = null;
		try {
			if(defaultValue != null) {
				parsedDefaultValue = (BigDecimal) format.parse(defaultValue);
			}
		}
		catch(ParseException e) {
			throw new DomainException(
					"The default value is not a valid number: " +
						id);
		}
		
		return new HoursBeforeNowPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			min,
			max,
			parsedDefaultValue,
			index);
	}
	
	/**
	 * Processes a multi-choice prompt and returns a MultiChoicePrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A MultiChoicePrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static MultiChoicePrompt processMultiChoice(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new DomainException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new DomainException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		List<Integer> defaultValues = null;
		if((defaultValue != null) && (! "".equals(defaultValue))) {
			String[] values = defaultValue.split(InputKeys.LIST_ITEM_SEPARATOR);
			
			defaultValues = new ArrayList<Integer>(values.length);
			for(int i = 0; i < values.length; i++) {
				String currValue = values[i];
				
				if(! "".equals(currValue)) {
					try {
						defaultValues.add(Integer.decode(currValue));
					}
					catch(NumberFormatException e) {
						throw new DomainException(
							"One of the default values was not an integer.");
					}
				}
			}
		}

		return new MultiChoicePrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			choices,
			defaultValues,
			index);
	}
	
	/**
	 * Processes a multi-choice custom prompt and returns a 
	 * MultiChoiceCustomPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A MultiChoiceCustomPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or 
	 * 						   if any of the parameters are invalid.
	 */
	private static MultiChoiceCustomPrompt processMultiChoiceCustom(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new DomainException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new DomainException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		List<Integer> defaultValues = null;
		if((defaultValue != null) && (! "".equals(defaultValue))) {
			String[] values = defaultValue.split(InputKeys.LIST_ITEM_SEPARATOR);
			
			defaultValues = new ArrayList<Integer>(values.length);
			for(int i = 0; i < values.length; i++) {
				String currValue = values[i];
				
				if(! "".equals(currValue)) {
					try {
						defaultValues.add(Integer.decode(currValue));
					}
					catch(NumberFormatException e) {
						throw new DomainException(
							"One of the default values was not an integer.");
					}
				}
			}
		}

		return new MultiChoiceCustomPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			choices,
			new HashMap<Integer, LabelValuePair>(),
			defaultValues,
			index);
	}
	
	/**
	 * Processes a number prompt and returns a NumberPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A NumberPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static NumberPrompt processNumber(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		// Create the decimal parser.
		DecimalFormat format = new DecimalFormat();
		format.setParseBigDecimal(true);
		
		BigDecimal min;
		try {
			LabelValuePair minVlp = 
				properties.get(NumberPrompt.XML_KEY_MIN);  // required
			
			if(minVlp == null) {
				throw new DomainException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			
			min = (BigDecimal) format.parse(minVlp.getLabel());
		}
		catch(ParseException e) {
			throw new DomainException(
					"The '" +
						NumberPrompt.XML_KEY_MIN +
						"' property is not a valid number: " +
						id, 
					e);
		}
		
		BigDecimal max;
		try {
			LabelValuePair maxVlp = 
				properties.get(NumberPrompt.XML_KEY_MAX);  // required
			
			if(maxVlp == null) {
				throw new DomainException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			
			max = (BigDecimal) format.parse(maxVlp.getLabel());
		}
		catch(ParseException e) {
			throw new DomainException(
					"The '" +
						NumberPrompt.XML_KEY_MAX +
						"' property is not valid number: " +
						id, 
					e);
		}
		
		BigDecimal parsedDefaultValue = null;
		try {
			if(defaultValue != null) {
				parsedDefaultValue = (BigDecimal) format.parse(defaultValue);
			}
		}
		catch(ParseException e) {
			throw new DomainException(
					"The default value is not a valid number: " +
						id);
		}
		
		Boolean wholeNumber = null;
		LabelValuePair wholeNumberVlp = 
			properties.get(NumberPrompt.XML_KEY_WHOLE_NUMBER);  // optional
		
		if(wholeNumberVlp != null) {
			wholeNumber =
				StringUtils.decodeBoolean(wholeNumberVlp.getLabel());
			if(wholeNumber == null) {
				throw
					new DomainException(
						"The '" +
						NumberPrompt.XML_KEY_WHOLE_NUMBER +
						"' property is not a valid boolean: " +
						id);
			}
		}

		return new NumberPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			min,
			max,
			parsedDefaultValue,
			index,
			wholeNumber);
	}
	
	/**
	 * Processes a photo prompt and returns a PhotoPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A PhotoPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static PhotoPrompt processPhoto(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Integer maxDimension = null;
		try {
			LabelValuePair maxDimensionVlp =
				properties.get(PhotoPrompt.XML_KEY_MAXIMUM_DIMENSION);  // optional
			
			if(maxDimensionVlp != null) {
				maxDimension = 
					Integer.decode(maxDimensionVlp.getLabel());
			}
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						PhotoPrompt.XML_KEY_MAXIMUM_DIMENSION +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		if(defaultValue != null) {
			throw new DomainException(
					"Default values are not allowed for photo prompts: " +
						id);
		}

		return new PhotoPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			maxDimension,
			index);
	}

	/**
	 * Processes a document prompt and returns a DocumentPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A DocumentPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static DocumentPrompt processDocument(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		// TODO: Set default maxFilesize value. 
		Integer maxFilesize = null;
		try {
			LabelValuePair maxFilesizeVlp =
				properties.get(DocumentPrompt.XML_KEY_MAXIMUM_FILESIZE);  // optional
			
			if(maxFilesizeVlp != null) {
				maxFilesize = 
					Integer.decode(maxFilesizeVlp.getLabel());
			}
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						DocumentPrompt.XML_KEY_MAXIMUM_FILESIZE +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		if(defaultValue != null) {
			throw new DomainException(
					"Default values are not allowed for document prompts: " +
						id);
		}

		return new DocumentPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			maxFilesize,
			index);
	}
	

	/**
	 * Processes a remote activity prompt and returns a RemoteActivityPrompt
	 * object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A RemoteActivityPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static RemoteActivityPrompt processRemoteActivity(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel, 
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {

		LabelValuePair packageVlp = 
				properties.get(RemoteActivityPrompt.XML_KEY_PACKAGE);
		if(packageVlp == null) {
			throw new DomainException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_PACKAGE + 
						"' property: " +
						id);
		}
		String packagee = packageVlp.getLabel();
		
		LabelValuePair activityVlp = 
				properties.get(RemoteActivityPrompt.XML_KEY_ACTIVITY);
		if(activityVlp == null) {
			throw new DomainException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTIVITY + 
						"' property: " +
						id);
		}
		String activity = activityVlp.getLabel();
		
		LabelValuePair actionVlp = 
				properties.get(RemoteActivityPrompt.XML_KEY_ACTION);
		if(actionVlp == null) {
			throw new DomainException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTION + 
						"' property: " +
						id);
		}
		String action = actionVlp.getLabel();
		
		LabelValuePair autolaunchVlp = 
				properties.get(RemoteActivityPrompt.XML_KEY_AUTOLAUNCH);
		if(autolaunchVlp == null) {
			throw new DomainException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTION + 
						"' property: " +
						id);
		}
		Boolean autolaunch = 
				StringUtils.decodeBoolean(autolaunchVlp.getLabel());
		if(autolaunch == null) {
			throw new DomainException(
					"The property '" + 
						RemoteActivityPrompt.XML_KEY_AUTOLAUNCH + 
						"' is not a valid boolean: " +
						id);
		}
		
		int retries;
		try {
			LabelValuePair retriesVlp = 
					properties.get(RemoteActivityPrompt.XML_KEY_RETRIES);
			if(retriesVlp == null) {
				throw new DomainException(
						"Missing the '" + 
							RemoteActivityPrompt.XML_KEY_RETRIES + 
							"' property: " +
							id);	
			}
			retries = Integer.decode(retriesVlp.getLabel());
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						RemoteActivityPrompt.XML_KEY_RETRIES +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		int minRuns;
		try {
			LabelValuePair minRunsVlp = 
					properties.get(RemoteActivityPrompt.XML_KEY_MIN_RUNS);
			if(minRunsVlp == null) {
				throw new DomainException(
						"Missing the '" + 
							RemoteActivityPrompt.XML_KEY_MIN_RUNS + 
							"' property: " +
							id);	
			}
			minRuns = Integer.decode(minRunsVlp.getLabel());
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						RemoteActivityPrompt.XML_KEY_MIN_RUNS +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		String input = null;
		LabelValuePair inputVlp = properties.get(RemoteActivityPrompt.XML_KEY_INPUT);
		if(inputVlp != null) {
			input = inputVlp.getLabel();
		}
		
		if(defaultValue != null) {
			throw new DomainException(
					"Default values aren't allowed for remote activity prompts: " +
					id);
		}

		return new RemoteActivityPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			packagee,
			activity,
			action,
			autolaunch,
			retries,
			minRuns,
			input,
			index);
	}
	
	/**
	 * Processes a single choice prompt and returns a SingleChoicePrompt
	 * object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A SingleChoicePrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static SingleChoicePrompt processSingleChoice(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new DomainException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new DomainException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		Integer defaultKey = null;
		if(defaultValue != null) {
			try {
				defaultKey = Integer.decode(defaultValue);
			}
			catch(NumberFormatException e) {
				throw new DomainException("The default key is not an integer.");
			}
		}

		return new SingleChoicePrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			choices,
			defaultKey,
			index);
	}
	
	/**
	 * Processes a single choice custom prompt and returns a 
	 * SingleChoiceCustomPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A SingleChoiceCustomPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static SingleChoiceCustomPrompt processSingleChoiceCustom(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel, 
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new DomainException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new DomainException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		Integer defaultKey = null;
		if(defaultValue != null) {
			try {
				defaultKey = Integer.decode(defaultValue);
			}
			catch(NumberFormatException e) {
				throw new DomainException("The default key is not an integer.");
			}
		}

		return new SingleChoiceCustomPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			choices,
			new HashMap<Integer, LabelValuePair>(),
			defaultKey,
			index);
	}
	
	/**
	 * Processes a text prompt and returns a TextPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A TextPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static TextPrompt processText(
			final String id,
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel, 
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		int min;
		try {
			LabelValuePair minVlp = 
				properties.get(NumberPrompt.XML_KEY_MIN);
			
			if(minVlp == null) {
				throw new DomainException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			min = Integer.decode(minVlp.getLabel());
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						NumberPrompt.XML_KEY_MIN +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		int max;
		try {
			LabelValuePair maxVlp = 
				properties.get(NumberPrompt.XML_KEY_MAX);
			
			if(maxVlp == null) {
				throw new DomainException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			max = Integer.decode(maxVlp.getLabel());
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						NumberPrompt.XML_KEY_MAX +
						"' property is not an integer: " +
						id, 
					e);
		}

		return new TextPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			min,
			max,
			defaultValue,
			index);
	}
	
	/**
	 * Processes a text prompt and returns a TextPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A TextPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static TimestampPrompt processTimestamp(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel, 
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		if(defaultValue != null) {
			throw new DomainException(
					"Default values aren't allowed for timestamp prompts. The offending prompt id is: " +
						id);
		}

		return new TimestampPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			index);
	}
	
	/**
	 * Processes a video prompt and returns a VideoPrompt object.
	 * 
	 * @param id The prompt's unique identifier.
	 * 
	 * @param condition The condition value.
	 * 
	 * @param unit The prompt's visualization unit.
	 * 
	 * @param text The prompt's text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayLabel The label for this display type.
	 * 
	 * @param defaultValue The default value given in the XML.
	 * 
	 * @param properties The properties defined in the XML for this prompt.
	 * 
	 * @param index The index of this prompt in its collection of survey items.
	 * 
	 * @return A VideoPrompt object.
	 * 
	 * @throws DomainException Thrown if the required properties are missing or
	 * 						   if any of the parameters are invalid.
	 */
	private static VideoPrompt processVideo(
			final String id,
			final String condition, 
			final String unit, 
			final String text,
			final String explanationText, 
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, 
			final int index) 
			throws DomainException {
		
		if(defaultValue != null) {
			throw new DomainException(
				"Default values aren't allowed for video prompts: " + id);
		}
		
		Integer maxSeconds = null;
		try {
			LabelValuePair maxSecondsVlp = 
				properties.get(VideoPrompt.XML_MAX_SECONDS);
			
			if(maxSecondsVlp != null) {
				maxSeconds = Integer.decode(maxSecondsVlp.getLabel());
			}
		}
		catch(NumberFormatException e) {
			throw new DomainException(
					"The '" +
						VideoPrompt.XML_MAX_SECONDS+
						"' property is not an integer: " +
						id, 
					e);
		}
		
		return new VideoPrompt(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			maxSeconds,
			index);
	}
}
