package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.domain.UserStatsQueryResult;

/**
 * State for user stat API queries.
 * 
 * @author selsky
 */
public class UserStatsQueryAwRequest extends ResultListAwRequest {
	// input
	private String _userNameRequestParam;
	private String _client;
	private String _campaignName;
	
	// output
	private UserStatsQueryResult _userStatsQueryResult;
	
	public String getUserNameRequestParam() {
		return _userNameRequestParam;
	}

	public void setUserNameRequestParam(String userNameRequestParam) {
		_userNameRequestParam = userNameRequestParam;
	}

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getCampaignName() {
		return _campaignName;
	}

	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
	}

	public UserStatsQueryResult getUserStatsQueryResult() {
		return _userStatsQueryResult;
	}

	public void setUserStatsQueryResult(UserStatsQueryResult userStatsQueryResult) {
		_userStatsQueryResult = userStatsQueryResult;
	}
}
