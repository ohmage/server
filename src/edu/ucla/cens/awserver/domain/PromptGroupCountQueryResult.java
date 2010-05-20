package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class PromptGroupCountQueryResult implements Comparable<PromptGroupCountQueryResult> {
	private String _user = null;
	private int _count = -1;
	private int _campaignPromptGroupId = -1;
	private String _date = null;
	private boolean _empty = false;

	public int getCount() {
		return _count;
	}
	
	public void setCount(int count) {
		_count = count;
	}
	
	public int getCampaignPromptGroupId() {
		return _campaignPromptGroupId;
	}
	
	public void setCampaignPromptGroupId(int campaignPromptGroupId) {
		_campaignPromptGroupId = campaignPromptGroupId;
	}
	
	public String getDate() {
		return _date;
	}
	
	public void setDate(String date) {
		_date = date;
	}
	
	public String getUser() {
		return _user;
	}

	public void setUser(String user) {
		_user = user;
	}

	public boolean isEmpty() {
		return _empty;
	}

	public void setEmpty(boolean empty) {
		_empty = empty;
	}
	
	public int compareTo(PromptGroupCountQueryResult o) {
		int userCompare = _user.compareTo(o.getUser());
		
		if(0 == userCompare) {
		
			return ((Integer) _campaignPromptGroupId).compareTo(o.getCampaignPromptGroupId());
			
		} 
		
		return userCompare;
	}	
}
