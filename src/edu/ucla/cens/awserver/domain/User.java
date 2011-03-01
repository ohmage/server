package edu.ucla.cens.awserver.domain;

import java.util.List;
import java.util.Map;


/**
 * Internal representation of an AndWellness user. 
 * 
 * @author selsky
 */
public interface User {

	public int getId();
	public void setId(int id);
	
	public String getUserName();
	public void setUserName(String string);

	public String getPassword();
	public void setPassword(String string);
	
	public Map<String, List<Integer>> getCampaignRoles();
	public void addCampaignRole(String campaignName, Integer roleId);
	
	public boolean isLoggedIn();
	public void setLoggedIn(boolean b);
	
//	// TODO - these are actually not properties of the user - they are processing state on the *request*
//	public void setCurrentCampaignId(String id);
//	public String getCurrentCampaignId();
//	
//	public void setCurrentCampaignName(String name);
//	public String getCurrentCampaignName();
//	// -------
	
	public boolean isResearcherOrAdmin(String campaignName);
}
