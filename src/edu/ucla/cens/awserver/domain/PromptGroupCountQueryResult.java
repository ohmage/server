package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class PromptGroupCountQueryResult {
	private String _user;
	private int _count;
	private int _campaignPromptGroupId;
	private String _date;

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
}
