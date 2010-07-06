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
	
	public Map<Integer, List<Integer>> getCampaignRoles();
	public void addCampaignRole(Integer campaignId, Integer roleId);
	
	public boolean isLoggedIn();
	public void setLoggedIn(boolean b);
	
	public void setCurrentCampaignId(String id);
	public String getCurrentCampaignId();
	
	public boolean getIsResearcherOrAdmin();
}
