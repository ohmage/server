package edu.ucla.cens.awserver.domain;

/**
 * Container for mapping campaign ids to the group ids of their various prompt groups.
 * 
 * @author selsky
 */
public class CampaignPromptGroup {
	private int _campaignId;
	private int _groupId;
	
	public CampaignPromptGroup(int campaignId, int groupId) {
		_campaignId = campaignId;
		_groupId = groupId;
	}

	public int getCampaignId() {
		return _campaignId;
	}

	public int getGroupId() {
		return _groupId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _campaignId;
		result = prime * result + _groupId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignPromptGroup other = (CampaignPromptGroup) obj;
		if (_campaignId != other._campaignId)
			return false;
		if (_groupId != other._groupId)
			return false;
		return true;
	}
}
