package edu.ucla.cens.awserver.domain;


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

// future	
//	public List<Integer> getCampaignIds();
//	public void setCampaignIds(List<Integer> ids);
	
	public int getCampaignId();
	public void setCampaignId(int id);
	
//	public boolean isLoggedIn();
//	public void setLoggedIn(boolean b);
}
