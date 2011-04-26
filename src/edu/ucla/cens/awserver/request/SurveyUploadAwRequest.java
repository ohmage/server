package edu.ucla.cens.awserver.request;

import java.util.Map;

import edu.ucla.cens.awserver.domain.Configuration;


/**
 * Represents state for survey uploads.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequest extends UploadAwRequest {
	private Map<String, String> _postParameters;
	private String _campaignRunningState;
	private Configuration _configuration; 

	/**
	 * Default no-arg constructor.	
	 */
	public SurveyUploadAwRequest() {
		super();
	}
	
	public Configuration getConfiguration() {
		return _configuration;
	}

	public void setConfiguration(Configuration configuration) {
		_configuration = configuration;
	}

	public Map<String, String> getPostParameters() {
		return _postParameters;
	}

	public void setPostParameters(Map<String, String> postParameters) {
		_postParameters = postParameters;
	}
	
	public String getCampaignRunningState() {
		return _campaignRunningState;
	}
	
	public void setCampaignRunningState(String state) {
		_campaignRunningState = state;
	}
}

