package edu.ucla.cens.awserver.domain;

/**
 * Container for query results for the prompt group count query feature. 
 * 
 * @author selsky
 */
public class SingleUserPromptGroupCountQueryResult {
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
}
