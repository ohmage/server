package org.ohmage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * This class contains all information about a campaign. It will always be 
 * populated with the specific information about a campaign. It is almost
 * 
 * @author John Jenkins
 */
public final class CampaignInformation {
	/**
	 * These are all possible running states for a campaign.
	 * 
	 * @author John Jenkins
	 */
	public static enum RunningState { RUNNING, STOPPED };
	
	// TODO: Get rid of invisible.
	/**
	 * These are all possible privacy states for a campaign.
	 * 
	 * @author John Jenkins
	 */
	public static enum PrivacyState { SHARED, PRIVATE, INVISIBLE };
	
	private static final String JSON_KEY_ID = "campaign_id";
	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_DESCRIPTION = "description";
	private static final String JSON_KEY_ICON_URL = "icon_url";
	private static final String JSON_KEY_AUTHORED_BY = "authored_by";
	private static final String JSON_KEY_RUNNING_STATE = "running_state";
	private static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	private static final String JSON_KEY_CREATION_TIMESTAMP = "creation_timestamp";
	private static final String JSON_KEY_XML = "xml";
	
	private static final String JSON_KEY_CLASSES = "classes";
	
	private static final String JSON_KEY_ROLES = "user_role_campaign";
	private static final String JSON_KEY_SUPERVISOR = "supervisor";
	private static final String JSON_KEY_AUTHOR = "author";
	private static final String JSON_KEY_ANALYST = "analyst";
	private static final String JSON_KEY_PARTICIPANT = "participant";
	
	private final String id;
	private final String name;
	private final String description;
	private final String iconUrl;
	private final String authoredBy;
	private final RunningState runningState;
	private final PrivacyState privacyState;
	private final Date creationTimestamp;
	
	private final Set<String> supervisors;
	private final Set<String> authors;
	private final Set<String> analysts;
	private final Set<String> participants;
	
	private final Set<String> classes;
	
	private String xml;
	
	/**
	 * Creates a new object containing the minimum requirements for a campaign
	 * plus any addition information given. See each individual parameter to
	 * determine if it is required or not.
	 * 
	 * @param id The campaign's unique identifier. Required.
	 * 
	 * @param name The campaign's name. Required.
	 * 
	 * @param description The campaign's description. Not required.
	 * 
	 * @param runningState The campaign's running state. Required.
	 * 
	 * @param privacyState The campaign's privacy state. Required.
	 * 
	 * @param creationTimestamp The campaign's creation timestamp. Required.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are null or whitespace only.
	 */
	public CampaignInformation(String id, String name, String description,
			String iconUrl, String authoredBy,
			RunningState runningState, PrivacyState privacyState, 
			Date creationTimestamp) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("The campaign's ID cannot be null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The campaign's name cannot be null.");
		}
		else if(runningState == null) {
			throw new IllegalArgumentException("The campaign's running state cannot be null.");
		}
		else if(privacyState == null) {
			throw new IllegalArgumentException("The campaign's privacy state cannot be null.");
		}
		else if(creationTimestamp == null) {
			throw new IllegalArgumentException("The campaign's creation timestamp cannot be null.");
		}
		
		this.id = id;
		this.name = name;
		this.description = description;
		this.iconUrl = iconUrl;
		this.authoredBy = authoredBy;
		this.runningState = runningState;
		this.privacyState = privacyState;
		this.creationTimestamp = new Date(creationTimestamp.getTime());
		
		supervisors = new HashSet<String>();
		authors = new HashSet<String>();
		analysts = new HashSet<String>();
		participants = new HashSet<String>();
		
		classes = new HashSet<String>();
		
		xml = null;
	}
	
	/**
	 * Returns the campaign's XML.
	 * 
	 * @return The campaign's XML or null if it was never set.
	 */
	public final String getXml() {
		return xml;
	}
	
	/**
	 * Sets the campaign's XML.
	 * 
	 * @param xml The campaign's XML. This does not perform any validation.
	 */
	public final void setXml(String xml) {
		this.xml = xml;
	}
	
	/**
	 * Adds a user as a supervisor to this campaign.
	 * 
	 * @param username The user's username. If it is null, this call is 
	 * 				   ignored.
	 */
	public final void addSupervisor(String username) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return;
		}
		
		supervisors.add(username);
	}
	
	/**
	 * Adds a collection of users as supervisors to this campaign.
	 * 
	 * @param usernames The collection of usernames. If it is null this call is
	 * 					ignored.
	 */
	public final void addSupervisors(Collection<String> usernames) {
		if(usernames == null) {
			return;
		}
		
		supervisors.addAll(usernames);
	}
	
	/**
	 * Returns the current list of supervisors for this campaign.
	 * 
	 * @return An unmodifiable list of the usernames of the supervisors for 
	 * 		   this campaign.
	 */
	public final Set<String> getSupervisors() {
		return Collections.unmodifiableSet(supervisors);
	}
	
	/**
	 * Adds a user as an author to this campaign.
	 * 
	 * @param username The user's username. If it is null, this call is 
	 * 				   ignored.
	 */
	public final void addAuthor(String username) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return;
		}
		
		authors.add(username);
	}
	
	/**
	 * Adds a collection of users as authors to this campaign.
	 * 
	 * @param usernames The collection of usernames. If it is null this call is
	 * 					ignored.
	 */
	public final void addAuthors(Collection<String> usernames) {
		if(usernames == null) {
			return;
		}
		
		authors.addAll(usernames);
	}
	
	/**
	 * Returns the current list of authors for this campaign.
	 * 
	 * @return An unmodifiable list of the usernames of the authors for 
	 * 		   this campaign.
	 */
	public final Set<String> getAuthors() {
		return Collections.unmodifiableSet(authors);
	}
	
	/**
	 * Adds a user as an analyst to this campaign.
	 * 
	 * @param username The user's username. If it is null, this call is 
	 * 				   ignored.
	 */
	public final void addAnalyst(String username) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return;
		}
		
		analysts.add(username);
	}
	
	/**
	 * Adds a collection of users as analysts to this campaign.
	 * 
	 * @param usernames The collection of usernames. If it is null this call is
	 * 					ignored.
	 */
	public final void addAnalysts(Collection<String> usernames) {
		if(usernames == null) {
			return;
		}
		
		analysts.addAll(usernames);
	}
	
	/**
	 * Returns the current list of analysts for this campaign.
	 * 
	 * @return An unmodifiable list of the usernames of the analysts for 
	 * 		   this campaign.
	 */
	public final Set<String> getAnalysts() {
		return Collections.unmodifiableSet(analysts);
	}
	
	/**
	 * Adds a user as a participant to this campaign.
	 * 
	 * @param username The user's username. If it is null, this call is 
	 * 				   ignored.
	 */
	public final void addParticipant(String username) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return;
		}
		
		participants.add(username);
	}
	
	/**
	 * Adds a collection of users as participants to this campaign.
	 * 
	 * @param usernames The collection of usernames. If it is null this call is
	 * 					ignored.
	 */
	public final void addParticipants(Collection<String> usernames) {
		if(usernames == null) {
			return;
		}
		
		participants.addAll(usernames);
	}
	
	/**
	 * Returns the current list of participants for this campaign.
	 * 
	 * @return An unmodifiable list of the usernames of the participants for 
	 * 		   this campaign.
	 */
	public final Set<String> getParticipants() {
		return Collections.unmodifiableSet(participants);
	}
	
	public final void addClass(String classId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			return;
		}
		
		classes.add(classId);
	}
	
	public final void addClasses(Collection<String> classIds) {
		if(classIds == null) {
			return;
		}
		
		classes.addAll(classIds);
	}
	
	/**
	 * Returns the current list of classes associated with this campaign.
	 * 
	 * @return An unmodifiable list of class IDs associated with this campaign.
	 */
	public final Set<String> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	/**
	 * Returns the campaign's unique identifier.
	 * 
	 * @return The campaign's unique identifier.
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Returns the campaign's name.
	 * 
	 * @return The campaign's name.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the campaign's description.
	 * 
	 * @return The campaign's description. It will return an empty string if it
	 * 		   is null.
	 */
	public final String getDescription() {
		return (description == null) ? "" : description;
	}

	/**
	 * Returns the campaign's running state.
	 * 
	 * @return The campaign's running state.
	 */
	public final RunningState getRunningState() {
		return runningState;
	}

	/**
	 * Returns the campaign's privacy state.
	 * 
	 * @return The campaign's privacy state.
	 */
	public final PrivacyState getPrivacyState() {
		return privacyState;
	}

	/**
	 * Returns the date and time the campaign was created.
	 * 
	 * @return The date and time the campaign was created.
	 */
	public final Date getCreationTimestamp() {
		return new Date(creationTimestamp.getTime());
	}
	
	/**
	 * Converts this object into a JSONObject. The parameters allow the output
	 * to be formatted as desired.
	 * 
	 * @param withId Whether or not the campaign's ID should be included.
	 * 
	 * @param withClasses Whether or not the campaign's list of classes should
	 * 					  be included.
	 * 
	 * @param withRoles Whether or not the JSONObject listing all of the users
	 * 					and their roles in the campaign should be included.
	 * 
	 * @param withParticipants If 'withRoles' is true, whether or not to 
	 * 						   include the list of participants in the campaign
	 * 						   as part of the list of users and their roles.
	 * 
	 * @param withAnalysts If 'withRoles' is true, whether or not to include 
	 * 					   the list of analysts in the campaign as part of the
	 * 					   list of users and their roles.
	 * 
	 * @param withAuthors If 'withRoles' is true, whether or not to include the
	 * 					  list of authors in the campaign as part of the list 
	 * 					  of users and their roles.
	 * 
	 * @param withSupervisors If 'withRoles' is true, whether or not to include
	 * 						  the list of supervisors in the campaign as part 
	 * 						  of the list of users and their roles.
	 * 
	 * @param withXml Whether or not the campaign's XML should be included.
	 * 
	 * @return Returns a JSONObject representing this object based on the 
	 * 		   parameters or null if there is an error.
	 */
	public JSONObject toJson(boolean withId, boolean withClasses, boolean withRoles, 
			boolean withParticipants, boolean withAnalysts, boolean withAuthors, boolean withSupervisors,
			boolean withXml) {
		try {
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
			result.put(JSON_KEY_CREATION_TIMESTAMP, TimeUtils.getIso8601DateTimeString(creationTimestamp));
			
			if(withClasses) {
				result.put(JSON_KEY_CLASSES, classes);
			}
			
			if(withRoles) {
				JSONObject roles = new JSONObject();
				
				if(withParticipants) {
					roles.put(JSON_KEY_PARTICIPANT, participants);
				}
				if(withAnalysts) {
					roles.put(JSON_KEY_ANALYST, analysts);
				}
				if(withAuthors) {
					roles.put(JSON_KEY_AUTHOR, authors);
				}
				if(withSupervisors) {
					roles.put(JSON_KEY_SUPERVISOR, supervisors);
				}
				
				result.put(JSON_KEY_ROLES, roles);
			}
			
			if(withXml) {
				result.put(JSON_KEY_XML, xml);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}

	/**
	 * Generates the hash code for this object. This must follow the same 
	 * format as {@link #equals(Object)}, so it creates a hash out of the 
	 * campaign's ID only. This is the only thing that distinguishes one 
	 * campaign from another.
	 * 
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Checks if some object is equal to this campaign. The only thing that
	 * distinguishes two campaigns are their ID. There should never be two 
	 * CampaignInformation objects with the same ID in the same thread; 
	 * therefore, we don't need to check any other information in the class.
	 * 
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignInformation other = (CampaignInformation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}