package edu.ucla.cens.awserver.request;

import java.util.Map;


/**
 * Represents state for survey uploads.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequest extends UploadAwRequest {
	private String _campaignVersion;
	private Map<String, String> _postParameters;

	/**
	 * Default no-arg constructor.	
	 */
	public SurveyUploadAwRequest() {
		super();
	}
	
	public String getCampaignVersion() {
		return _campaignVersion;
	}

	public void setCampaignVersion(String campaignVersion) {
		_campaignVersion = campaignVersion;
	}
	
	public Map<String, String> getPostParameters() {
		return _postParameters;
	}

	public void setPostParameters(Map<String, String> postParameters) {
		_postParameters = postParameters;
	}

	@Override
	public String toString() {
		return "SurveyUploadAwRequest [_campaignVersion=" + _campaignVersion
				+ ", _postParameters=" + _postParameters + ", toString()="
				+ super.toString() + "]";
	}	
}

