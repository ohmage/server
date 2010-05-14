package edu.ucla.cens.awserver.domain;

/**
 * Wrapper for query results for the number of prompts per group per campaign.
 * 
 * @author selsky
 */
public class CampaignPromptGroupItemCount {
	private CampaignPromptGroup _campaignPromptGroup;
	private int _count;
	
	public CampaignPromptGroupItemCount(int campaignId, int groupId, int count) {
		_campaignPromptGroup = new CampaignPromptGroup(campaignId, groupId);
		_count = count;
	}

	public int getCount() {
		return _count;
	}
	
	public CampaignPromptGroup getCampaignPromtGroup() {
		return _campaignPromptGroup;
	}
}
