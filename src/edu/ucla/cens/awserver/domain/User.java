package edu.ucla.cens.awserver.domain;

import java.util.List;
import java.util.Map;


/**
 * User properties for a valid, logged-in user.
 * 
 * @author selsky
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
	 * @return a Map of campaign URNs and the allowed roles for this user.
	 */
	public Map<String, List<Integer>> getCampaignRoles();
	
	/**
	 * Adds a role in a campaign URN for this user. Users can have multiple roles within a campaign.
	 * @param campaignUrn
	 * @param roleId
	 */
	public void addCampaignRole(String campaignUrn, Integer roleId);
	
	/**
	 * @return whether this user is logged in to the system. Flag for handling failed logins.
	 */
	public boolean isLoggedIn();
	
	/**
	 * Sets the login status for this user on successful or unsuccessful authentication.
	 * @param b
	 */
	public void setLoggedIn(boolean b);
	
//	/**
//	 * 
//	 * @param campaignName
//	 * @return
//	 */
//	public boolean isResearcherOrAdmin(String campaignName);
}
