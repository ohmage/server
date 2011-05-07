package edu.ucla.cens.awserver.domain;

import java.util.Map;

/**
 * User properties.
 *  
 * @author Joshua Selsky
 */
public interface User {
	
	/**
	 * @return the database primary key for this user. 
	 */
	int getId();
	
	/**
	 * @param id the database primary key for this user.
	 */
	void setId(int id);
	
	/**
	 * @return the user name from the db (user.login_id).
	 */
	String getUserName();
	
	/**
	 * @param string the user name from the db for this user.
	 */
	void setUserName(String string);

	/**
	 * @return the user's password.
	 */
	String getPassword();
	
	/**
	 * @param string the password to be used for authentication.
	 */
	void setPassword(String string);
	
	/**
	 * @return a Map of campaign URNs to the campaign metadata and the allowed roles for this user.
	 */
	Map<String, CampaignUserRoles> getCampaignUserRoleMap();
	
	/**
	 * Adds a role in a campaign URN for this user. Users can have multiple roles within a campaign.
	 * @param campaign
	 * @param userRole
	 */
	void addCampaignRole(Campaign campaign, UserRole userRole);
	
	/**
	 * @return whether this user is logged in to the system. Flag for handling failed logins.
	 */
	boolean isLoggedIn();
	
	/**
	 * Sets the login status for this user on successful or unsuccessful authentication.
	 * @param b
	 */
	void setLoggedIn(boolean b);
	
	boolean isSupervisorInCampaign(String campaignUrn);
	boolean isAuthorInCampaign(String campaignUrn);
	boolean isAnalystInCampaign(String campaignUrn);
	boolean isParticipantInCampaign(String campaignUrn);
}
