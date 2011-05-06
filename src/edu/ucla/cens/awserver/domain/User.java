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
	public int getId();
	
	/**
	 * @param id the database primary key for this user.
	 */
	public void setId(int id);
	
	/**
	 * @return the user name from the db (user.login_id).
	 */
	public String getUserName();
	
	/**
	 * @param string the user name from the db for this user.
	 */
	public void setUserName(String string);

	/**
	 * @return the user's password.
	 */
	public String getPassword();
	
	/**
	 * @param string the password to be used for authentication.
	 */
	public void setPassword(String string);
	
	/**
	 * @return a Map of campaign URNs to the campaign metadata and the allowed roles for this user.
	 */
	public Map<String, CampaignUserRoles> getCampaignUserRoleMap();
	
	/**
	 * Adds a role in a campaign URN for this user. Users can have multiple roles within a campaign.
	 * @param campaign
	 * @param userRole
	 */
	public void addCampaignRole(Campaign campaign, UserRole userRole);
	
	/**
	 * @return whether this user is logged in to the system. Flag for handling failed logins.
	 */
	public boolean isLoggedIn();
	
	/**
	 * Sets the login status for this user on successful or unsuccessful authentication.
	 * @param b
	 */
	public void setLoggedIn(boolean b);
}
