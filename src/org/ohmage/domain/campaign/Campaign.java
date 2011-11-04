/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
import java.net.MalformedURLException;
import java.net.URL;
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

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt.DisplayType;
import org.ohmage.domain.campaign.Prompt.LabelValuePair;
import org.ohmage.domain.campaign.Prompt.Type;
import org.ohmage.domain.campaign.prompt.ChoicePrompt;
import org.ohmage.domain.campaign.prompt.CustomChoicePrompt;
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
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * An immutable representation of a campaign.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class Campaign {
	private static final String XML_ID = "/campaign/campaignUrn";
	private static final String XML_NAME = "/campaign/campaignName";
	
	private static final String XML_SERVER_URL = "/campaign/serverUrl";
	private static final String XML_ICON_URL = "/campaign/iconUrl";
	private static final String XML_AUTHORED_BY = "/campaign/authoredBy";
	
	private static final String XML_SURVEYS = "/campaign/surveys";
	private static final String XML_SURVEY = "survey";
	private static final String XML_SURVEY_ID = "id";
	private static final String XML_SURVEY_TITLE = "title";
	private static final String XML_SURVEY_DESCRIPTION = "description";
	private static final String XML_SURVEY_INTRO_TEXT = "introText";
	private static final String XML_SURVEY_SUBMIT_TEXT = "submitText";
	private static final String XML_SURVEY_SHOW_SUMMARY = "showSummary";
	private static final String XML_SURVEY_EDIT_SUMMARY = "editSummary";
	private static final String XML_SURVEY_SUMMARY_TEXT = "summaryText";
	private static final String XML_SURVEY_ANYTIME = "anytime";
	private static final String XML_SURVEY_CONTENT_LIST = "contentList";
	private static final String XML_CONTENT_LIST_ITEMS = "prompt | repeatableSet | message";
	
	private static final String XML_PROMPT_ID = "id";
	private static final String XML_PROMPT_CONDITION = "condition";
	private static final String XML_PROMPT_UNIT = "unit";
	private static final String XML_PROMPT_TEXT = "promptText";
	private static final String XML_PROMPT_ABBREVIATED_TEXT = "abbreviatedText";
	private static final String XML_PROMPT_EXPLANATION_TEXT = "explanationText";
	private static final String XML_PROMPT_SKIPPABLE = "skippable";
	private static final String XML_PROMPT_SKIP_LABEL = "skipLabel";
	private static final String XML_PROMPT_DISPLAY_TYPE = "displayType";
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
	private static final String JSON_KEY_SERVER_URL = "server_url";
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
	private final Date creationTimestamp;
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
	 * The URL of the server that should be used to update this configuration
	 * and upload the survey responses to.
	 */
	private final URL serverUrl;
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
	private final Map<String, List<Role>> userRoles;

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
	 */
	public Campaign(String id, String name, String description, 
			URL serverUrl, URL iconUrl, String authoredBy, 
			RunningState runningState, PrivacyState privacyState, 
			Date creationTimestamp, Map<String, Survey> surveyMap, String xml) {

		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("The ID cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The name cannot be null or whitespace only.");
		}
		if(runningState == null) {
			throw new IllegalArgumentException("The running state cannot be null.");
		}
		if(privacyState == null) {
			throw new IllegalArgumentException("The privacy state cannot be null.");
		}
		if(creationTimestamp == null) {
			throw new IllegalArgumentException("The creation timestamp cannot be null.");
		}
		if(null == surveyMap) {
			throw new IllegalArgumentException("The survey map cannot be null or empty only.");
		}
		
		this.id = id;
		this.name = name;
		this.description = description;
		
		this.serverUrl = serverUrl;
		this.iconUrl = iconUrl;
		this.authoredBy = authoredBy;
		
		this.runningState = runningState;
		this.privacyState = privacyState;
		
		this.creationTimestamp = new Date(creationTimestamp.getTime());
		
		this.xml = xml;
		
		this.surveyMap = surveyMap; // TODO deep copy?
		
		userRoles = new HashMap<String, List<Role>>();
		classes = new LinkedList<String>();
	}
	
	/**
	 * Creates a Campaign object from the JSON information.
	 * 
	 * @param id The campaign's identifier.
	 * 
	 * @param information The information about the campaign.
	 */
	public Campaign(final String id, final JSONObject information) {
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("The ID is null or whitespace only.");
		}
		else if(information == null) {
			throw new IllegalArgumentException("The information is null.");
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
			throw new IllegalArgumentException("The running state is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The running state is invalid.", e);
		}

		try {
			privacyState = PrivacyState.getValue(information.getString(JSON_KEY_PRIVACY_STATE));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The privacy state is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The privacy state is invalid.", e);
		}
		
		try {
			creationTimestamp = StringUtils.decodeDateTime(information.getString(JSON_KEY_CREATION_TIMESTAMP));
			
			if(creationTimestamp == null) {
				throw new IllegalArgumentException("The creation timestamp is invalid.");
			}
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The creation timestamp is missing.", e);
		}
		
		Map<String, List<Role>> tUserRoles = new HashMap<String, List<Role>>();
		try {
			JSONObject roles = information.getJSONObject(JSON_KEY_ROLES);
			
			Iterator<?> keys = roles.keys();
			while(keys.hasNext()) {
				Role role = Role.getValue((String) keys.next());
				
				JSONArray usernames = roles.getJSONArray(role.toString());
				int numUsernames = usernames.length();
				for(int i = 0; i < numUsernames; i++) {
					String username = usernames.getString(i);
					
					List<Role> currUserRoles = tUserRoles.get(username);
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
			throw new IllegalArgumentException("Unknown role.", e);
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
		URL tServerUrl = null;
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
				throw new IllegalStateException("XML was unreadable.", e);
			}
			catch(XMLException e) {
				throw new IllegalStateException("No usable XML parser could be found.", e);
			}
			catch(ValidityException e) {
				throw new IllegalArgumentException("The XML is invalid.", e);
			}
			catch(ParsingException e) {
				throw new IllegalArgumentException("The XML is not well formed.", e);
			}
			
			Element root = document.getRootElement();
			
			tId = getId(root);
			tName = getName(root);
			
			try {
				tServerUrl = getServerUrl(root);
			}
			catch(IllegalArgumentException e) {
				// The server URL is optional, so we don't care.
			}
			
			try {
				tIconUrl = getIconUrl(root);
			}
			catch(IllegalArgumentException e) {
				// The icon URL is optional, so we don't care.
			}
			
			try {
				tAuthoredBy = getAuthoredBy(root);
			}
			catch(IllegalArgumentException e) {
				// The icon URL is optional, so we don't care.
			}
			
			// Process all of the surveys.
			tSurveyMap = getSurveys(root);
		}
		catch(JSONException noXml) {
			tId = id;
			
			try {
				tName = information.getString(JSON_KEY_NAME);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The campaign's name was missing from the JSON.", e);
			}
			
			try {
				tServerUrl = new URL(information.getString(JSON_KEY_SERVER_URL));
			}
			catch(JSONException e) {
				// The server URL is optional.
			}
			catch(MalformedURLException e) {
				throw new IllegalArgumentException("The server URL is not a valid URL.", e);
			}
			
			try {
				tIconUrl = new URL(information.getString(JSON_KEY_ICON_URL));
			}
			catch(JSONException e) {
				// The icon URL is optional.
			}
			catch(MalformedURLException e) {
				throw new IllegalArgumentException("The icon URL is not a valid URL.", e);
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
		
		serverUrl = tServerUrl;
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
	 * @throws IllegalArgumentException Thrown if the XML is invalid.
	 */
	public Campaign(final String description,
			final RunningState runningState, final PrivacyState privacyState, 
			final Date creationTimestamp, final String xml) {
		
		if(runningState == null) {
			throw new NullPointerException("The running state is null.");
		}
		else if(privacyState == null) {
			throw new NullPointerException("The privacy state is null.");
		}
		else if(creationTimestamp == null) {
			throw new NullPointerException("The creation timestamp is null.");
		}
		else if(xml == null) {
			throw new NullPointerException("The XML is null.");
		}
		
		Document document;
		try {
			document = (new Builder()).build(new StringReader(xml));
		} 
		catch(IOException e) {
			// This should only be thrown if it can't read the 'xml', but
			// given that it is already in memory this should never happen.
			throw new IllegalStateException("XML was unreadable.", e);
		}
		catch(XMLException e) {
			throw new IllegalStateException("No usable XML parser could be found.", e);
		}
		catch(ValidityException e) {
			throw new IllegalArgumentException("The XML is invalid.", e);
		}
		catch(ParsingException e) {
			throw new IllegalArgumentException("The XML is not well formed.", e);
		}
		
		Element root = document.getRootElement();
		
		id = getId(root);
		name = getName(root);
		this.description = description;
		
		URL tServerUrl = null;
		try {
			tServerUrl = getServerUrl(root);
		}
		catch(IllegalArgumentException e) {
			// The server URL is optional, so we don't care.
		}
		serverUrl = tServerUrl;
		
		URL tIconUrl = null;
		try {
			tIconUrl = getIconUrl(root);
		}
		catch(IllegalArgumentException e) {
			// The icon URL is optional, so we don't care.
		}
		iconUrl = tIconUrl;
		
		String tAuthoredBy = null;
		try {
			tAuthoredBy = getAuthoredBy(root);
		}
		catch(IllegalArgumentException e) {
			// The icon URL is optional, so we don't care.
		}
		authoredBy = tAuthoredBy;
		
		// Process all of the surveys.
		surveyMap = getSurveys(root);
		
		this.runningState = runningState;
		this.privacyState = privacyState;
		
		this.creationTimestamp = new Date(creationTimestamp.getTime());
		
		this.xml = xml;
		
		userRoles = new HashMap<String, List<Role>>();
		classes = new LinkedList<String>();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param configuration A configuration of which this will be a copy.
	 */
	public Campaign(Campaign configuration) {
		this.id = configuration.id;
		this.name = configuration.name;
		this.description = configuration.description;
		
		this.serverUrl = configuration.serverUrl;
		this.iconUrl = configuration.iconUrl;
		this.authoredBy = configuration.authoredBy;
		
		this.runningState = configuration.runningState;
		this.privacyState = configuration.privacyState;
		
		this.creationTimestamp = new Date(configuration.creationTimestamp.getTime());
		
		this.xml = configuration.xml;
		
		this.surveyMap = configuration.surveyMap; // FIXME: Deep copy.
		
		this.userRoles = configuration.userRoles; // FIXME: Deep copy.
		this.classes = new ArrayList<String>(configuration.classes);
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
	 * Returns the server's URL.
	 * 
	 * @return The server's URL. This may be null.
	 */
	public URL getServerUrl() {
		return serverUrl;
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
	 * @return The date and time the configuration was creatd.
	 */
	public Date getCreationTimestamp() {
		return new Date(creationTimestamp.getTime());
	}
	
	/**
	 * Returns the configuration defined as an XML string.
	 * 
	 * @return The configuration defined as an XML string.
	 */
	public String getXml() {
		return xml;
	}
	
	/**
	 * Adds a user and an associated role to the list of users and their roles.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param role The user's configuration role.
	 * 
	 * @throws NullPointerException Thrown if the username or role is null.
	 */
	public void addUser(final String username, final Role role) {
		if(username == null) {
			throw new NullPointerException("The username is null.");
		}
		else if(role == null) {
			throw new NullPointerException("The role is null.");
		}
		
		List<Role> roles = userRoles.get(username);
		if(roles == null) {
			roles = new LinkedList<Role>();
			userRoles.put(username, roles);
		}
		roles.add(role);
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
	 * @throws IllegalArgumentException Thrown if the class ID list is null.
	 */
	public void addClasses(final Collection<String> classIds) {
		if(classIds == null) {
			throw new IllegalArgumentException("The class ID list is null.");
		}
		
		classes.addAll(classIds);
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
	 * @throws NullPointerException Thrown if the survey ID is null.
	 */
	public boolean surveyIdExists(String surveyId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
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
	 * @throws NullPointerException Thrown if the survey ID is null.
	 */
	public String getSurveyTitleFor(String surveyId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
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
	 * @throws NullPointerException Thrown if the survey ID is null.
	 */
	public String getSurveyDescriptionFor(String surveyId) {
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration.
	 */
	public RepeatableSet getRepeatableSet(String surveyId, String repeatableSetId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		else if(repeatableSetId == null) {
			throw new NullPointerException("The repeatable set ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new IllegalArgumentException("The survey ID is unknown.");
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration.
	 */
	public Prompt getPrompt(String surveyId, String promptId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		else if(promptId == null) {
			throw new NullPointerException("The prompt ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new IllegalArgumentException("The survey ID is unknown.");
		}
		
		Map<Integer, SurveyItem> prompts = survey.getSurveyItems();
		for(SurveyItem prompt : prompts.values()) {
			if(prompt instanceof Prompt) {
				Prompt currPrompt = (Prompt) prompt;
				
				if(promptId.equals(currPrompt.getId())) {
					return currPrompt;
				}
			}
			if(prompt instanceof RepeatableSet) {
				Prompt currPrompt = ((RepeatableSet) prompt).getPrompt(promptId);
				if(currPrompt != null) {
					return currPrompt;
				}
			}
		}
        
        return null;
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public boolean isPromptSkippable(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration.
	 */
	public boolean promptExists(String surveyId, String promptId) {
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 * 
	 * @see org.ohmage.domain.campaign.Prompt.Type
	 */
	public Type getPromptType(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public boolean isPromptInRepeatableSet(String surveyId, String promptId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		else if(promptId == null) {
			throw new NullPointerException("The prompt ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new IllegalArgumentException("The survey ID is unknown.");
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
	 * @throws NullPointerException Thrown if the prompt ID is null.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt ID is not known.
	 */
	public boolean promptContainsSingleChoiceValues(String promptId) {
		if(promptId == null) {
			throw new NullPointerException("The prompt ID is null.");
		}
		
		Survey survey = getSurveyForPromptId(promptId);
		if(survey == null) {
			throw new IllegalArgumentException("Unknown prompt ID: " + promptId);
		}
		
		SurveyItem abstractPrompt = survey.getSurveyItem(promptId);
		if(abstractPrompt == null) {
			// This is impossible given that we just got the survey ID from 
			// this prompt ID.
			throw new IllegalArgumentException("The prompt ID has changed.");
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
	 * @throws NullPointerException Thrown if the survey ID is null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey ID is unknown.
	 */
	public int getNumberOfPromptsInSurvey(String surveyId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new IllegalArgumentException("There is no survey with the ID: " + surveyId);
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID is null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey ID or prompt ID 
	 * 									are unknown or if the repeatable set ID
	 * 									doesn't refer to a repeatable set.
	 */
	public int numberOfPromptsInRepeatableSet(String surveyId, String repeatableSetId) {
		if(surveyId == null) {
			throw new NullPointerException("The survey ID is null.");
		}
		else if(repeatableSetId == null) {
			throw new NullPointerException("The repeatable set ID is null.");
		}
		
		Survey survey = surveyMap.get(surveyId);
		if(survey == null) {
			throw new IllegalArgumentException("There is no survey with the ID: " + surveyId);
		}
		
		SurveyItem prompt = survey.getSurveyItems().get(repeatableSetId);
		if(prompt instanceof RepeatableSet) {
			RepeatableSet repeatableSet = (RepeatableSet) prompt;
			
			return repeatableSet.getNumPrompts();
		}
		else if(prompt == null) {
			throw new IllegalArgumentException("There is no prompt with the ID: " + repeatableSetId);
		}
		else {
			throw new IllegalArgumentException("The prompt is not a repeatable set: " + repeatableSetId);
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
	 * @throws NullPointerException Thrown if the prompt ID is null.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt ID is unknown.
	 */
	public String getSurveyIdForPromptId(String promptId) {
		for(Survey survey : surveyMap.values()) {
			if(survey.getSurveyItem(promptId) != null) {
				return survey.getId();
			}
		}
		
		throw new IllegalArgumentException("The prompt doesn't exist.");
	}
	
	/**
	 * Retrieves the Survey object for some prompt.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The configuration-unique identifier for the survey to which the 
	 * 		   prompt belongs.
	 * 
	 * @throws NullPointerException Thrown if the prompt ID is null.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt ID is unknown.
	 */
	public Survey getSurveyForPromptId(String promptId) {
		for(Survey survey : surveyMap.values()) {
			if(survey.getSurveyItem(promptId) != null) {
				return survey;
			}
		}
		
		throw new IllegalArgumentException("The prompt doesn't exist.");
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public String getPromptTextFor(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getText();
		}
	}

	/**
	 * Retrieves the display type for some prompt in some survey.
	 * 
	 * @param surveyId The survey's configuration-unique identifier.
	 * 
	 * @param promptId The prompt's configuration-unique identifier.
	 * 
	 * @return The display type for the prompt in the survey.
	 * 
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public DisplayType getDisplayTypeFor(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
		}
		else {
			return prompt.getDisplayType();
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public String getDisplayLabelFor(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
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
	 * @throws NullPointerException Thrown if the survey ID or prompt ID are 
	 * 								null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey or isn't a
	 * 									ChoicePrompt or if the label is 
	 * 									unknown.
	 */
	public String getValueForChoiceKey(String surveyId, String promptId, String key) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
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
	 * @throws IllegalArgumentException Thrown if the Prompt isn't a 
	 * 									ChoicePrompt or the label isn't know 
	 * 									for the prompt.
	 */
	private String getChoiceValueFrom(Prompt prompt, String label) {
		Map<Integer, LabelValuePair> choices;
		if(prompt instanceof CustomChoicePrompt) {
			choices = ((CustomChoicePrompt) prompt).getAllChoices();
		}
		else if(prompt instanceof ChoicePrompt) {
			choices = ((ChoicePrompt) prompt).getChoices();
		}
		else {
			throw new IllegalArgumentException("The prompt isn't a choice prompt.");
		}
		
		for(LabelValuePair valueLabelPair : choices.values()) {
			if(valueLabelPair.getLabel().equals(label)) {
				return valueLabelPair.getValue().toString();
			}
		}
		
		throw new IllegalArgumentException("The label is not known.");
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
	 * @throws NullPointerException Thrown if the survey ID or repeatable set
	 * 								ID are null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey or isn't a
	 * 									ChoicePrompt or if the label is 
	 * 									unknown.
	 */
	public String getLabelForChoiceKey(String surveyId, String promptId, String key) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
		}
		else {
			return getChoiceLabelFrom(prompt, key);
		}
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
	 * Returns the label from some prompt response key.
	 * 
	 * @param prompt A ChoicePrompt prompt.
	 * 
	 * @param key A choice key valid for the ChoicePrompt.
	 * 
	 * @return The choice's label value.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt isn't a choice 
	 * 									prompt, the 'key' isn't a number, or 
	 * 									the key is not a valid choice for the
	 * 									prompt.
	 */
	private String getChoiceLabelFrom(Prompt prompt, String key) {
		Map<Integer, LabelValuePair> choices;
		if(prompt instanceof CustomChoicePrompt) {
			choices = ((CustomChoicePrompt) prompt).getAllChoices();
		}
		else if(prompt instanceof ChoicePrompt) {
			choices = ((ChoicePrompt) prompt).getChoices();
		}
		else {
			throw new IllegalArgumentException("The prompt isn't a choice prompt.");
		}
		
		Integer keyInt;
		try {
			keyInt = Integer.decode(key);
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("The key is not a number.", e);
		}
		
		LabelValuePair vlp = choices.get(keyInt);
		if(vlp == null) {
			throw new IllegalArgumentException("The key is unknown.");
		}
		else {
			return vlp.getLabel();
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
	 * @throws NullPointerException Thrown if the survey ID or prompt ID are
	 * 								null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey or if that
	 * 									prompt isn't a ChoicePrompt.
	 */
	public Map<Integer, LabelValuePair> getChoiceGlossaryFor(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
		}
		else {
			if(prompt instanceof CustomChoicePrompt) {
				return ((CustomChoicePrompt) prompt).getAllChoices();
			}
			else if(prompt instanceof ChoicePrompt) {
				return ((ChoicePrompt) prompt).getChoices();
			}
			else {
				throw new IllegalArgumentException("The prompt is not a choice prompt.");
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
	 * @throws NullPointerException Thrown if the survey ID or prompt ID are
	 * 								null.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey doesn't exist in
	 * 									this configuration or if the prompt 
	 * 									doesn't exist in that survey.
	 */
	public String getUnitFor(String surveyId, String promptId) {
		Prompt prompt = getPrompt(surveyId, promptId);
		if(prompt == null) {
			throw new IllegalArgumentException("No prompt with the given ID: " + promptId);
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
	 * @return A JSONObject that represents this campaign or null if there was
	 * 		   an error.
	 */
	public JSONObject toJson(final boolean withId, final boolean withClasses,
			final boolean withRoles, final boolean withParticipants,
			final boolean withAnalysts, final boolean withAuthors,
			final boolean withSupervisors, final boolean withXml,
			final boolean withSurveys) {
		try {
			JSONObject result = new JSONObject();
			
			if(withId) {
				result.put(JSON_KEY_ID, id);
			}
			result.put(JSON_KEY_NAME, name);
			result.put(JSON_KEY_DESCRIPTION, (description == null) ? "" : description);
			result.put(JSON_KEY_SERVER_URL, serverUrl);
			result.put(JSON_KEY_ICON_URL, iconUrl);
			result.put(JSON_KEY_AUTHORED_BY, authoredBy);
			result.put(JSON_KEY_RUNNING_STATE, runningState.name().toLowerCase());
			result.put(JSON_KEY_PRIVACY_STATE, privacyState.name().toLowerCase());
			result.put(JSON_KEY_CREATION_TIMESTAMP, TimeUtils.getIso8601DateTimeString(creationTimestamp));
			
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
				result.put(JSON_KEY_XML, xml);
			}
			
			if(withSurveys) {
				JSONArray surveysArray = new JSONArray();
				
				for(Survey survey : surveyMap.values()) {
					surveysArray.put( 
							survey.toJson(
									true,	// ID
									true,	// Title
									true,	// Description
									true,	// Intro Text
									true,	// Submit Text
									true,	// Show Summary
									true,	// Edit Summary
									true,	// Summary Text
									true,	// Anytime
									true	// Prompts
								)
						);
				}
				
				result.put(JSON_KEY_SURVEYS, surveysArray);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
	
	/**
	 * Validates that some XML contains all required components of an ohmage
	 * XML document and that all values, even optional ones that are given, are
	 * valid values.
	 * 
	 * @param xml The XML as a String.
	 * 
	 * @throws IllegalArgumentException Thrown if the XML is not valid.
	 */
	public static void validateXml(final String xml) {
		Document document;
		try {
			document = (new Builder()).build(new StringReader(xml));
		} 
		catch(IOException e) {
			// This should only be thrown if it can't read the 'xml', but
			// given that it is already in memory this should never happen.
			throw new IllegalStateException("XML was unreadable.", e);
		}
		catch(XMLException e) {
			throw new IllegalStateException("No usable XML parser could be found.", e);
		}
		catch(ValidityException e) {
			throw new IllegalArgumentException("The XML is invalid.", e);
		}
		catch(ParsingException e) {
			throw new IllegalArgumentException("The XML is not well formed.", e);
		}
		
		Element root = document.getRootElement();
		
		getId(root);
		getName(root);
		
		try {
			getServerUrl(root);
		}
		catch(IllegalArgumentException e) {
			// The server URL is optional, so we don't care.
		}
		
		try {
			getIconUrl(root);
		}
		catch(IllegalArgumentException e) {
			// The icon URL is optional, so we don't care.
		}
		
		try {
			getAuthoredBy(root);
		}
		catch(IllegalArgumentException e) {
			// The icon URL is optional, so we don't care.
		}
		
		// Process all of the surveys.
		getSurveys(root);
	}
	
	/**
	 * Checks that the campaign URN exists and is a valid URN as defined by
	 * us.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's identifier.
	 * 
	 * @throws IllegalArgumentException Thrown if the URN fails validation.
	 */
	private static String getId(final Node root) {
		Nodes ids = root.query(XML_ID);
		if(ids.size() == 0) {
			throw new IllegalArgumentException("The campaign ID is missing.");
		}
		else if(ids.size() > 1) {
			throw new IllegalArgumentException("Multiple campaign IDs were found.");
		}
		else {
			String urn = ids.get(0).getValue().trim();
			if(StringUtils.isValidUrn(urn)) {
				return urn;
			}
			else {
				throw new IllegalArgumentException("The campaign ID is not valid.");
			}
		}
	}
	
	/**
	 * Checks that the campaign name exists and is a valid name as defined by
	 * us.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's name.
	 * 
	 * @throws IllegalArgumentException Thrown if the name fails validation.
	 */
	private static String getName(final Node root) {
		Nodes names = root.query(XML_NAME);
		if(names.size() == 0) {
			throw new IllegalArgumentException("The campaign name is missing.");
		}
		else if(names.size() > 1) {
			throw new IllegalArgumentException("Multiple campaign names were found.");
		}
		else {
			String name = names.get(0).getValue().trim();
			
			if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
				throw new IllegalArgumentException("The name tag exists but the value is empty.");
			}
			else {
				return name;
			}
		}
	}
	
	/**
	 * Checks that the campaign URL exists and is a valid URL.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's server's URL.
	 * 
	 * @throws IllegalArgumentException Thrown if the server URL is missing,
	 * 									there are multiple of them, or it is 
	 * 									not a valid URL.
	 */
	private static URL getServerUrl(final Node root) {
		Nodes serverUrls = root.query(XML_SERVER_URL);
		if(serverUrls.size() == 0) {
			throw new IllegalArgumentException("The server URL is missing.");
		}
		else if(serverUrls.size() > 1) {
			throw new IllegalArgumentException("Multiple server URLs were found.");
		}
		else {
			String serverUrlString = serverUrls.get(0).getValue().trim();
			
			try {
				return new URL(serverUrlString);
			}
			catch(MalformedURLException e) {
				throw new IllegalArgumentException("The server URL is not a valid URL.");
			}
		}
	}
	
	/**
	 * Checks that the icon URL exists and is a valid URL.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's icon URL.
	 * 
	 * @throws IllegalArgumentException Thrown if the icon URL is missing, 
	 * 									there are multiple of them, or it is 
	 * 									not a valid URL.
	 */
	private static URL getIconUrl(final Node root) {
		Nodes iconUrls = root.query(XML_ICON_URL);
		if(iconUrls.size() == 0) {
			throw new IllegalArgumentException("The icon URL is missing.");
		}
		else if(iconUrls.size() > 1) {
			throw new IllegalArgumentException("Multiple icon URLs were found.");
		}
		else {
			String serverUrlString = iconUrls.get(0).getValue().trim();
			
			try {
				return new URL(serverUrlString);
			}
			catch(MalformedURLException e) {
				throw new IllegalArgumentException("The icon URL is not a valid URL.");
			}
		}
	}
	
	/**
	 * Checks that the authored by value exists.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return The campaign's authored by value.
	 * 
	 * @throws IllegalArgumentException Thrown if the authored by value is  
	 * 									missing, or there are multiple of them.
	 */
	private static String getAuthoredBy(final Node root) {
		Nodes authoredBy = root.query(XML_AUTHORED_BY);
		if(authoredBy.size() == 0) {
			throw new IllegalArgumentException("The authored by value is missing.");
		}
		else if(authoredBy.size() > 1) {
			throw new IllegalArgumentException("Multiple authored by values were found.");
		}
		else {
			return authoredBy.get(0).getValue().trim();
		}
	}
	
	/**
	 * Checks that the 'surveys' tag exists and that it contains at least one
	 * survey.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @return A map of survey IDs to their Survey object.
	 * 
	 * @throws IllegalArgumentException Thrown if the surveys value is missing
	 * 									or there are multiple of them or if
	 * 									there are no surveys.
	 */
	private static Map<String, Survey> getSurveys(final Node root) {
		Nodes surveysNodes = root.query(XML_SURVEYS);
		if(surveysNodes.size() == 0) {
			throw new IllegalArgumentException("The surveys value is missing.");
		}
		else if(surveysNodes.size() > 1) {
			throw new IllegalArgumentException("Multiple surveys values were found.");
		}
		Node surveysNode = surveysNodes.get(0);
		
		Nodes surveyNodes = surveysNode.query(XML_SURVEY);
		List<Survey> surveys = processSurveys(surveyNodes);
		
		int numSurveys = surveys.size();
		if(numSurveys == 0) {
			throw new IllegalArgumentException("No surveys were found.");
		}
		
		Map<String, Survey> result = new HashMap<String, Survey>(numSurveys);
		for(Survey survey : surveys) {
			if(result.put(survey.getId(), survey) != null) {
				throw new IllegalArgumentException("Mutiple surveys have the same unique identifier: " + survey.getId());
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
	 * @throws IllegalArgumentException Thrown if one of the surveys is 
	 * 									malformed.
	 */
	private static List<Survey> processSurveys(final Nodes surveys) {
		int numSurveys = surveys.size();
		List<Survey> result = new ArrayList<Survey>(numSurveys);
		
		for(int i = 0; i < numSurveys; i++) {
			Node survey = surveys.get(i);
			result.add(processSurvey(survey));
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
	 * @throws IllegalArgumentException Thrown if the survey is invalid.
	 */
	private static Survey processSurvey(final Node survey) {
		Nodes ids = survey.query(XML_SURVEY_ID);
		if(ids.size() == 0) {
			throw new IllegalArgumentException("The survey ID is missing.");
		}
		else if(ids.size() > 1) {
			throw new IllegalArgumentException("Multiple survey IDs were found for the same survey.");
		}
		String id = ids.get(0).getValue().trim();
		
		Nodes titles = survey.query(XML_SURVEY_TITLE);
		if(titles.size() == 0) {
			throw new IllegalArgumentException("The survey title is missing: " + id);
		}
		else if(titles.size() > 1) {
			throw new IllegalArgumentException("Multiple survey titles were found for the same survey: " + id);
		}
		String title = titles.get(0).getValue().trim();
		
		String description = null;
		Nodes descriptions = survey.query(XML_SURVEY_DESCRIPTION);
		if(descriptions.size() > 1) {
			throw new IllegalArgumentException("Multiple survey descriptions were found for the same survey: " + id);
		}
		else if(descriptions.size() == 1) {
			description = descriptions.get(0).getValue().trim();
		}
		
		String introText = null;
		Nodes introTexts = survey.query(XML_SURVEY_INTRO_TEXT);
		if(introTexts.size() > 1) {
			throw new IllegalArgumentException("Multiple survey intro texts were found for the same survey: " + id);
		}
		else if(introTexts.size() == 1) {
			introText = introTexts.get(0).getValue().trim();
		}
		
		Nodes submitTexts = survey.query(XML_SURVEY_SUBMIT_TEXT);
		if(submitTexts.size() == 0) {
			throw new IllegalArgumentException("The survey submit text is missing.");
		}
		else if(submitTexts.size() > 1) {
			throw new IllegalArgumentException("Multiple survey submit texts were found for the same survey: " + id);
		}
		String submitText = submitTexts.get(0).getValue().trim();
		
		Nodes showSummarys = survey.query(XML_SURVEY_SHOW_SUMMARY);
		if(showSummarys.size() == 0) {
			throw new IllegalArgumentException("The survey show summary is missing.");
		}
		else if(showSummarys.size() > 1) {
			throw new IllegalArgumentException("Multiple survey show summarys were found for the same survey: " + id);
		}
		Boolean showSummary = StringUtils.decodeBoolean(showSummarys.get(0).getValue().trim());
		if(showSummary == null) {
			throw new IllegalArgumentException("The show summary value is not a valid boolean value: " + id);
		}
		
		Boolean editSummary = null;
		Nodes editSummarys = survey.query(XML_SURVEY_EDIT_SUMMARY);
		if(editSummarys.size() > 1) {
			throw new IllegalArgumentException("Multiple survey edit summarys were found for the same survey: " + id);
		}
		else if(editSummarys.size() == 1) {
			editSummary = StringUtils.decodeBoolean(editSummarys.get(0).getValue().trim());
			
			if(editSummary == null) {
				throw new IllegalArgumentException("The edit summary value is not a valid boolean value: " + id);
			}
		}
		
		String summaryText = null;
		Nodes summaryTexts = survey.query(XML_SURVEY_SUMMARY_TEXT);
		if(summaryTexts.size() > 1) {
			throw new IllegalArgumentException("Multiple survey summary texts were found for the same survey: " + id);
		}
		else if(summaryTexts.size() == 1) {
			summaryText = summaryTexts.get(0).getValue();
		}
		
		Nodes anytimes = survey.query(XML_SURVEY_ANYTIME);
		if(anytimes.size() == 0) {
			throw new IllegalArgumentException("The survey anytime value is missing: " + id);
		}
		else if(anytimes.size() > 1) {
			throw new IllegalArgumentException("Multiple survey anytime values were found for the same survey: " + id);
		}
		Boolean anytime = StringUtils.decodeBoolean(anytimes.get(0).getValue().trim());
		if(anytime == null) {
			throw new IllegalArgumentException("The anytime value is not a valid boolean value: " + id);
		}
		
		Nodes contentLists = survey.query(XML_SURVEY_CONTENT_LIST);
		if(contentLists.size() == 0) {
			throw new IllegalArgumentException("The survey content list is missing: " + id);
		}
		else if(contentLists.size() > 1) {
			throw new IllegalArgumentException("Multiple survey content lists were found: " + id);
		}
		Node contentList = contentLists.get(0);
		List<SurveyItem> promptsList = processContentList(id, contentList.query(XML_CONTENT_LIST_ITEMS));
		
		Map<Integer, SurveyItem> prompts = new HashMap<Integer, SurveyItem>(promptsList.size());
		Set<String> promptIds = new HashSet<String>();
		for(SurveyItem prompt : promptsList) {
			if(promptIds.add(prompt.getId())) {
				prompts.put(prompt.getIndex(), prompt);
			}
			else {
				throw new IllegalArgumentException("Multiple prompts have the same unqiue identifier in a group of survey items: " + prompt.getId());
			}
		}
		
		return new Survey(id, title, description, introText, submitText,
				showSummary, editSummary, summaryText, anytime, prompts);
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
	 * @throws IllegalArgumentException Thrown if one of the content list items 
	 * 									is malformed.
	 */
	private static List<SurveyItem> processContentList(final String surveyId,
			final Nodes contentListItems) {
		
		int numItems = contentListItems.size();
		List<SurveyItem> result = new ArrayList<SurveyItem>(numItems);
		
		for(int i = 0; i < numItems; i++) {
			SurveyItem.Type contentListItem;
			try {
				contentListItem = SurveyItem.Type.getValue(
						((Element) contentListItems.get(i)).getLocalName());
			}
			catch(IllegalArgumentException e) {
				throw new IllegalStateException("There were unknown content list items found: " + surveyId, e);
			}
				
			switch(contentListItem) {
			case MESSAGE:
				result.add(processMessage(surveyId, contentListItems.get(i), i));
				break;
				
			case REPEATABLE_SET:
				result.add(processRepeatableSet(surveyId, contentListItems.get(i), i));
				break;
				
			case PROMPT:
				result.add(processPrompt(surveyId, contentListItems.get(i), i));
				break;
				
			default:
				throw new IllegalStateException("There are new content list items but no matching processor for survey '" + surveyId + "': " + contentListItem);
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
	 */
	private static Message processMessage(final String containerId,
			final Node message, final int index) {
		
		Nodes ids = message.query(XML_MESSAGE_ID);
		if(ids.size() == 0) {
			throw new IllegalArgumentException("The message ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new IllegalArgumentException("Multiple message IDs were found: " + containerId);
		}
		String id = ids.get(0).getValue().trim();
		
		String condition = null;
		Nodes conditions = message.query(XML_MESSAGE_CONDITION);
		if(conditions.size() > 1) {
			throw new IllegalArgumentException("Multiple message conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();
		}
		
		Nodes texts = message.query(XML_MESSAGE_TEXT);
		if(texts.size() == 0) {
			throw new IllegalArgumentException("The message text is missing: " + id);
		}
		else if(texts.size() > 1) {
			throw new IllegalArgumentException("Multiple message texts were found: " + id);
		}
		String text = texts.get(0).getValue().trim();
		
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
	 * @throws IllegalArgumentException Thrown if the repeatable set is 
	 * 									malformed or if any of the prompts in
	 * 									the repeatable set are malformed.
	 */
	private static RepeatableSet processRepeatableSet(final String containerId,
			final Node repeatableSet, final int index) {
		
		Nodes ids = repeatableSet.query(XML_REPEATABLE_SET_ID);
		if(ids.size() == 0) {
			throw new IllegalArgumentException("The repeatable set ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set IDs were found: " + containerId);
		}
		String id = ids.get(0).getValue().trim();
		
		String condition = null;
		Nodes conditions = repeatableSet.query(XML_REPEATABLE_SET_CONDITION);
		if(conditions.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();
		}
		
		Nodes teminationQuestions =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_QUESTION);
		if(teminationQuestions.size() == 0) {
			throw new IllegalArgumentException("The repeatable set termination question is missing: " + id);
		}
		else if(teminationQuestions.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set termination questions were found: " + id);
		}
		String terminationQuestion = teminationQuestions.get(0).getValue().trim();
		
		Nodes terminationTrueLabels =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_TRUE_LABEL);
		if(terminationTrueLabels.size() == 0) {
			throw new IllegalArgumentException("The repeatable set termination true label is missing: " + id);
		}
		else if(terminationTrueLabels.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set termination true labels were found: " + id);
		}
		String terminationTrueLabel = terminationTrueLabels.get(0).getValue().trim();
		
		Nodes terminationFalseLabels =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_FALSE_LABEL);
		if(terminationFalseLabels.size() == 0) {
			throw new IllegalArgumentException("The repeatable set termination false label is missing: " + id);
		}
		else if(terminationFalseLabels.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set termination false labels were found: " + id);
		}
		String terminationFalseLabel = terminationFalseLabels.get(0).getValue().trim();
		
		Nodes terminationSkipEnableds =
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_SKIP_ENABLED);
		if(terminationSkipEnableds.size() == 0) {
			throw new IllegalArgumentException("The repeatable set termination skip enabled value is missing: " + id);
		}
		else if(terminationSkipEnableds.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set termination skip enabled value were found: " + id);
		}
		Boolean terminationSkipEnabled = 
			StringUtils.decodeBoolean(
					terminationSkipEnableds.get(0).getValue().trim()
				);
		if(terminationSkipEnabled == null) {
			throw new IllegalArgumentException("The termination skip enabled value is not a valid boolean value: " + id);
		}
		
		String terminationSkipLabel = null;
		Nodes terminationSkipLabels = 
			repeatableSet.query(XML_REPEATABLE_SET_TERMINATION_SKIP_LABEL);
		if(terminationSkipLabels.size() > 1) {
			throw new IllegalArgumentException("Multiple repeatable set termination skip labels were found: " + id);
		}
		else if(terminationSkipLabels.size() == 1) {
			terminationSkipLabel = terminationSkipLabels.get(0).getValue().trim();
		}
		
		Nodes promptsNodes = repeatableSet.query(XML_REPEATABLE_SET_PROMPTS);
		if(promptsNodes.size() == 0) {
			throw new IllegalArgumentException("The repeatable set doesn't contain a prompts group: " + id);
		}
		if(promptsNodes.size() > 1) {
			throw new IllegalArgumentException("The repeatable set contains multiple prompts groups: " + id);
		}
		
		Nodes promptNodes = promptsNodes.get(0).query(XML_CONTENT_LIST_ITEMS);
		List<SurveyItem> promptGroup = processContentList(id, promptNodes);
		if(promptGroup.size() == 0) {
			throw new IllegalArgumentException("The repeatable set doesn't contain any prompts: " + id);
		}
		
		Map<Integer, SurveyItem> promptMap = 
			new HashMap<Integer, SurveyItem>(promptGroup.size());
		Set<String> promptIds = new HashSet<String>();
		for(SurveyItem currPrompt : promptGroup) {
			if(promptIds.add(currPrompt.getId())) {
				// This is where repeatable sets prevent sub repeatable sets.
				// To allow this restriction, remove this if statement.
				if(currPrompt instanceof RepeatableSet) {
					throw new IllegalArgumentException("Repeatable sets may not contain repeatable sets: " + id);
				}

				promptMap.put(currPrompt.getIndex(), currPrompt);
			}
			else {
				throw new IllegalArgumentException("A repeatable set has multiple prompts with the same unique identifier: " + currPrompt.getId());
			}
		}
		
		return new RepeatableSet(id, condition, terminationQuestion,
				terminationTrueLabel, terminationFalseLabel, 
				terminationSkipEnabled, terminationSkipLabel, 
				promptMap, index);
	}
	
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
	private static Prompt processPrompt(final String containerId, 
			final Node prompt, final int index) {
		
		Nodes ids = prompt.query(XML_PROMPT_ID);
		if(ids.size() == 0) {
			throw new IllegalArgumentException("The prompt ID is missing: " + containerId);
		}
		else if(ids.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt IDs were found: " + containerId);
		}
		String id = ids.get(0).getValue().trim();
		
		String condition = null;
		Nodes conditions = prompt.query(XML_PROMPT_CONDITION);
		if(conditions.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt conditions were found: " + id);
		}
		else if(conditions.size() == 1) {
			condition = conditions.get(0).getValue().trim();
		}
		
		String unit = null;
		Nodes units = prompt.query(XML_PROMPT_UNIT);
		if(units.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt units were found: " + id);
		}
		else if(units.size() == 1) {
			unit = units.get(0).getValue().trim();
		}
		
		Nodes texts = prompt.query(XML_PROMPT_TEXT);
		if(texts.size() == 0) {
			throw new IllegalArgumentException("The prompt text is missing: " + id);
		}
		else if(texts.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt texts were found: " + id);
		}
		String text = texts.get(0).getValue().trim();
		
		String abbreviatedText = null;
		Nodes abbreviatedTexts = prompt.query(XML_PROMPT_ABBREVIATED_TEXT);
		if(abbreviatedTexts.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt abbreviated texts were found: " + id);
		}
		else if(abbreviatedTexts.size() == 1) {
			abbreviatedText = abbreviatedTexts.get(0).getValue().trim();
		}
		
		String explanationText = null;
		Nodes explanationTexts = prompt.query(XML_PROMPT_EXPLANATION_TEXT);
		if(explanationTexts.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt explanation texts were found: " + id);
		}
		else if(explanationTexts.size() == 1) {
			explanationText = explanationTexts.get(0).getValue().trim();
		}
		
		Nodes skippables = prompt.query(XML_PROMPT_SKIPPABLE);
		if(skippables.size() == 0) {
			throw new IllegalArgumentException("The prompt skippable is missing: " + id);
		}
		else if(skippables.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt skippable values were found: " + id);
		}
		Boolean skippable = StringUtils.decodeBoolean(skippables.get(0).getValue());
		if(skippable == null) {
			throw new IllegalArgumentException("The prompt skippable value was not a valid boolean value: " + id);
		}
		
		String skipLabel = null;
		Nodes skipLabels = prompt.query(XML_PROMPT_SKIP_LABEL);
		if(skipLabels.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt skip labels were found: " + id);
		}
		else if(skipLabels.size() == 1) {
			skipLabel = skipLabels.get(0).getValue().trim();
		}
		
		Nodes displayTypes = prompt.query(XML_PROMPT_DISPLAY_TYPE);
		if(displayTypes.size() == 0) {
			throw new IllegalArgumentException("The prompt display type is missing: " + id);
		}
		else if(displayTypes.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt display types were found: " + id);
		}
		DisplayType displayType;
		try {
			displayType = DisplayType.valueOf(displayTypes.get(0).getValue().trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The display type is unknown: " + id, e);
		}
		
		Nodes displayLabels = prompt.query(XML_PROMPT_DISPLAY_LABEL);
		if(displayLabels.size() == 0) {
			throw new IllegalArgumentException("The prompt display label is missing: " + id);
		}
		else if(displayLabels.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt display labels were found: " + id);
		}
		String displayLabel = displayLabels.get(0).getValue().trim();
		
		String defaultValue = null;
		Nodes defaultValues = prompt.query(XML_PROMPT_DEFAULT);
		if(defaultValues.size() > 1) {
			throw new IllegalArgumentException("Multiple default values were found: " + id);
		}
		else if(defaultValues.size() == 1) {
			defaultValue = defaultValues.get(0).getValue().trim();
		}
		
		Nodes promptTypes = prompt.query(XML_PROMPT_TYPE);
		if(promptTypes.size() == 0) {
			throw new IllegalArgumentException("The prompt type is invalid: " + id);
		}
		else if(promptTypes.size() > 1) {
			throw new IllegalArgumentException("Multiple prompt types were found: " + id);
		}
		Prompt.Type type;
		try {
			type = Prompt.Type.valueOf(promptTypes.get(0).getValue().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The prompt type is unknown: " + id);
		}
		
		Map<String, LabelValuePair> properties;
		Nodes propertiesNodes = prompt.query(XML_PROMPT_PROPERTIES);
		if(propertiesNodes.size() > 1) {
			throw new IllegalArgumentException("Multiple properties groups found: " + id);
		}
		else if(propertiesNodes.size() == 1) {
			properties = getKeyValueLabelTrios(id, propertiesNodes.get(0).query(XML_PROMPT_PROPERTY));
		}
		else {
			properties = new HashMap<String, LabelValuePair>(0);
		}

		switch(type) {
		case HOURS_BEFORE_NOW:
			return processHoursBeforeNow(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case MULTI_CHOICE:
			return processMultiChoice(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case MULTI_CHOICE_CUSTOM:
			return processMultiChoiceCustom(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case NUMBER:
			return processNumber(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case PHOTO:
			return processPhoto(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case REMOTE_ACTIVITY:
			return processRemoteActivity(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case SINGLE_CHOICE:
			return processSingleChoice(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case SINGLE_CHOICE_CUSTOM:
			return processSingleChoiceCustom(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case TEXT:
			return processText(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		case TIMESTAMP:
			return processTimestamp(id, condition, unit, text, 
					abbreviatedText, explanationText, skippable, skipLabel, 
					displayType, displayLabel, defaultValue, properties, index);
			
		default:
			return null;
		}
	}
	
	/**
	 * Process the property Nodes from the XML and creates a mapping of all of
	 * the keys to label/value pairs.
	 * 
	 * @param properties The properties for the prompt in the XML.
	 * 
	 * @return A mapping of property keys to their label/value pairs.
	 * 
	 * @throws IllegalArgumentException Thrown if a property is invalid.
	 */
	private static Map<String, LabelValuePair> getKeyValueLabelTrios(
			final String containerId, final Nodes properties) {

		int numProperties = properties.size();
		if(numProperties == 0) {
			return Collections.emptyMap();
		}

		Map<String, LabelValuePair> result = new HashMap<String, LabelValuePair>(numProperties);
		
		for(int i = 0; i < numProperties; i++) {
			Node propertyNode = properties.get(i);
			
			Nodes keys = propertyNode.query(XML_PROPERTY_KEY);
			if(keys.size() == 0) {
				throw new IllegalArgumentException("The property key is missing: " + containerId);
			}
			else if(keys.size() > 1) {
				throw new IllegalArgumentException("Multiple property keys were found: " + containerId);
			}
			String key = keys.get(0).getValue().trim();
			
			Nodes labels = propertyNode.query(XML_PROPERTY_LABEL);
			if(labels.size() == 0) {
				throw new IllegalArgumentException("The property label is missing: " + containerId);
			}
			else if(labels.size() > 1) {
				throw new IllegalArgumentException("Multiple property labels were found: " + containerId);
			}
			String label = labels.get(0).getValue().trim();
			
			Number value = null;
			Nodes values = propertyNode.query(XML_PROPERTY_VALUE);
			if(values.size() > 1) {
				throw new IllegalArgumentException("Multiple property values found: " + containerId);
			}
			else if(values.size() == 1) {
				String valueString = values.get(0).getValue().trim();
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
									throw new IllegalArgumentException(
											"The property value is not a numeric value: " + containerId);
								}
							}
						}
					}
				}
			}
			
			if(result.put(key, new LabelValuePair(label, value)) != null) {
				throw new IllegalArgumentException(
						"Multiple properties with the same key were found: " + containerId);
			}
		}
		
		return result;
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static HoursBeforeNowPrompt processHoursBeforeNow(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		int min;
		try {
			LabelValuePair minVlp = 
				properties.get(HoursBeforeNowPrompt.XML_KEY_MIN);
			
			if(minVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" +
							HoursBeforeNowPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			min = Integer.decode(minVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						HoursBeforeNowPrompt.XML_KEY_MIN +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		int max;
		try {
			LabelValuePair maxVlp = 
				properties.get(HoursBeforeNowPrompt.XML_KEY_MAX);
			
			if(maxVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" +
							HoursBeforeNowPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			max = Integer.decode(maxVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						HoursBeforeNowPrompt.XML_KEY_MAX +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		Long defaultValueLong = null;
		try {
			if(defaultValue != null) {
				defaultValueLong = Long.decode(defaultValue);
			}
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException(
					"The default value is not a valid integer: " +
						id);
		}
		
		return new HoursBeforeNowPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, min, max, defaultValueLong, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static MultiChoicePrompt processMultiChoice(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new IllegalArgumentException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		List<String> defaultValues = null;
		if((defaultValue != null) && (! "".equals(defaultValue))) {
			String[] values = defaultValue.split(InputKeys.LIST_ITEM_SEPARATOR);
			
			defaultValues = new ArrayList<String>(values.length);
			for(int i = 0; i < values.length; i++) {
				String currValue = values[i];
				
				if(! "".equals(currValue)) {
					defaultValues.add(currValue);
				}
			}
		}
		
		return new MultiChoicePrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, choices, defaultValues, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static MultiChoiceCustomPrompt processMultiChoiceCustom(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new IllegalArgumentException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		List<String> defaultValues = null;
		if((defaultValue != null) && (! "".equals(defaultValue))) {
			String[] values = defaultValue.split(InputKeys.LIST_ITEM_SEPARATOR);
			
			defaultValues = new ArrayList<String>(values.length);
			for(int i = 0; i < values.length; i++) {
				String currValue = values[i];
				
				if(! "".equals(currValue)) {
					defaultValues.add(currValue);
				}
			}
		}
		
		return new MultiChoiceCustomPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, choices, 
				new HashMap<Integer, LabelValuePair>(), defaultValues, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static NumberPrompt processNumber(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		int min;
		try {
			LabelValuePair minVlp = 
				properties.get(NumberPrompt.XML_KEY_MIN);
			
			if(minVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			min = Integer.decode(minVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
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
				throw new IllegalArgumentException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			max = Integer.decode(maxVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						NumberPrompt.XML_KEY_MAX +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		Long defaultValueLong = null;
		try {
			if(defaultValue != null) {
				defaultValueLong = Long.decode(defaultValue);
			}
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException(
					"The default value is not a valid integer: " +
						id);
		}
		
		return new NumberPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, min, max, defaultValueLong, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static PhotoPrompt processPhoto(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		int verticalResolution;
		try {
			LabelValuePair verticalResolutionVlp = 
				properties.get(PhotoPrompt.XML_KEY_VERTICAL_RESOLUTION);
			
			if(verticalResolutionVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" +
							PhotoPrompt.XML_KEY_VERTICAL_RESOLUTION +
							"' property: " +
							id);
			}
			verticalResolution = Integer.decode(verticalResolutionVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						PhotoPrompt.XML_KEY_VERTICAL_RESOLUTION +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		if(defaultValue != null) {
			throw new IllegalArgumentException(
					"Default values are not allowed for photo prompts: " +
						id);
		}
		
		return new PhotoPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, verticalResolution, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static RemoteActivityPrompt processRemoteActivity(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {

		LabelValuePair packageVlp = properties.get(RemoteActivityPrompt.XML_KEY_PACKAGE);
		if(packageVlp == null) {
			throw new IllegalArgumentException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_PACKAGE + 
						"' property: " +
						id);
		}
		String packagee = packageVlp.getLabel();
		
		LabelValuePair activityVlp = properties.get(RemoteActivityPrompt.XML_KEY_ACTIVITY);
		if(activityVlp == null) {
			throw new IllegalArgumentException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTIVITY + 
						"' property: " +
						id);
		}
		String activity = activityVlp.getLabel();
		
		LabelValuePair actionVlp = properties.get(RemoteActivityPrompt.XML_KEY_ACTION);
		if(actionVlp == null) {
			throw new IllegalArgumentException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTION + 
						"' property: " +
						id);
		}
		String action = actionVlp.getLabel();
		
		LabelValuePair autolaunchVlp = properties.get(RemoteActivityPrompt.XML_KEY_AUTOLAUNCH);
		if(autolaunchVlp == null) {
			throw new IllegalArgumentException(
					"Missing the '" + 
						RemoteActivityPrompt.XML_KEY_ACTION + 
						"' property: " +
						id);
		}
		Boolean autolaunch = StringUtils.decodeBoolean(autolaunchVlp.getLabel());
		if(autolaunch == null) {
			throw new IllegalArgumentException(
					"The property '" + 
						RemoteActivityPrompt.XML_KEY_AUTOLAUNCH + 
						"' is not a valid boolean: " +
						id);
		}
		
		int retries;
		try {
			LabelValuePair retriesVlp = properties.get(RemoteActivityPrompt.XML_KEY_RETRIES);
			if(retriesVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" + 
							RemoteActivityPrompt.XML_KEY_RETRIES + 
							"' property: " +
							id);	
			}
			retries = Integer.decode(retriesVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						RemoteActivityPrompt.XML_KEY_RETRIES +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		int minRuns;
		try {
			LabelValuePair minRunsVlp = properties.get(RemoteActivityPrompt.XML_KEY_MIN_RUNS);
			if(minRunsVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" + 
							RemoteActivityPrompt.XML_KEY_MIN_RUNS + 
							"' property: " +
							id);	
			}
			minRuns = Integer.decode(minRunsVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
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
			throw new IllegalArgumentException(
					"Default values aren't allowed for remote activity prompts: " +
					id);
		}
		
		return new RemoteActivityPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, packagee, activity, action, 
				autolaunch, retries, minRuns, input, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static SingleChoicePrompt processSingleChoice(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new IllegalArgumentException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		return new SingleChoicePrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, choices, defaultValue, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static SingleChoiceCustomPrompt processSingleChoiceCustom(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		Map<Integer, LabelValuePair> choices = 
			new HashMap<Integer, LabelValuePair>(properties.size());
		
		for(String key : properties.keySet()) {
			Integer keyInt;
			try {
				keyInt = Integer.decode(key);
				
				if(keyInt < 0) {
					throw new IllegalArgumentException(
							"The key value cannot be negative: " +
								id);
				}
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException(
						"The key is not a valid integer: " +
							id, 
						e);
			}
			
			choices.put(keyInt, properties.get(key));
		}
		
		return new SingleChoiceCustomPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, choices, 
				new HashMap<Integer, LabelValuePair>(), defaultValue, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static TextPrompt processText(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		int min;
		try {
			LabelValuePair minVlp = 
				properties.get(NumberPrompt.XML_KEY_MIN);
			
			if(minVlp == null) {
				throw new IllegalArgumentException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MIN +
							"' property: " +
							id);
			}
			min = Integer.decode(minVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
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
				throw new IllegalArgumentException(
						"Missing the '" +
							NumberPrompt.XML_KEY_MAX +
							"' property: " +
							id);
			}
			max = Integer.decode(maxVlp.getLabel());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"The '" +
						NumberPrompt.XML_KEY_MAX +
						"' property is not an integer: " +
						id, 
					e);
		}
		
		return new TextPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, min, max, defaultValue, index);
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
	 * @param abbreviatedText The prompt's abbreviated text value.
	 * 
	 * @param explanationText The prompt's explanation text value.
	 * 
	 * @param skippable Whether or not this prompt is skippable.
	 * 
	 * @param skipLabel The label to show to skip this prompt.
	 * 
	 * @param displayType The display type of this prompt.
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
	 * @throws IllegalArgumentException Thrown if the required properties are
	 * 									missing or if any of the parameters are
	 * 									invalid.
	 */
	private static TimestampPrompt processTimestamp(final String id,
			final String condition, final String unit, final String text,
			final String abbreviatedText, final String explanationText, 
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String defaultValue,
			final Map<String, LabelValuePair> properties, final int index) {
		
		if(defaultValue != null) {
			throw new IllegalArgumentException(
					"Default values aren't allowed for remote activity prompts: " +
						id);
		}
		
		return new TimestampPrompt(id, condition, unit, text, 
				abbreviatedText, explanationText, skippable, skipLabel, 
				displayType, displayLabel, index);
	}
}