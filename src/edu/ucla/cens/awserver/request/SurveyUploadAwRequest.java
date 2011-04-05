package edu.ucla.cens.awserver.request;

import java.util.Map;


/**
 * Represents state for survey uploads.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequest extends UploadAwRequest {
	private Map<String, String> _postParameters;

	/**
	 * Default no-arg constructor.	
	 */
	public SurveyUploadAwRequest() {
		super();
	}
		
	public Map<String, String> getPostParameters() {
		return _postParameters;
	}

	public void setPostParameters(Map<String, String> postParameters) {
		_postParameters = postParameters;
	}
}

