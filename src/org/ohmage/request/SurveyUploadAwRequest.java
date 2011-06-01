/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request;

import java.util.Map;

import org.ohmage.domain.Configuration;



/**
 * Represents state for survey uploads.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequest extends UploadAwRequest {
	private Map<String, String> _postParameters;
	private String _campaignRunningState;
	private String _campaignCreationTimestamp;
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
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}
}

