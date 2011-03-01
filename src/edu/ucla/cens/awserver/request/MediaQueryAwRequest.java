package edu.ucla.cens.awserver.request;

/**
 * State for image queries.
 * 
 * @author selsky
 */
public class MediaQueryAwRequest extends ResultListAwRequest {
	private String _userNameRequestParam;
	private String _client;
	private String _campaignName;
	private String _campaignVersion;
	private String _mediaId;
	private String _mediaUrl;
	
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

	public String getCampaignVersion() {
		return _campaignVersion;
	}

	public void setCampaignVersion(String campaignVersion) {
		_campaignVersion = campaignVersion;
	}

	public String getMediaId() {
		return _mediaId;
	}

	public void setMediaId(String mediaId) {
		_mediaId = mediaId;
	}

	public String getMediaUrl() {
		return _mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		_mediaUrl = mediaUrl;
	}
}
