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
package org.ohmage.domain;

/**
 * Storage of campaign metadata (basically everything except the XML).
 * 
 * @author Joshua Selsky
 */
public class Campaign {
	private String _urn;
	private String _name;
	private String _description;
	private String _runningState;
	private String _privacyState;
	private String _campaignCreationTimestamp;
	
	public String getUrn() {
		return _urn;
	}
	
	public void setUrn(String urn) {
		_urn = urn;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getDescription() {
		return _description;
	}
	
	public void setDescription(String description) {
		_description = description;
	}
	
	public String getRunningState() {
		return _runningState;
	}
	
	public void setRunningState(String runningState) {
		_runningState = runningState;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public void setPrivacyState(String privacyState) {
		_privacyState = privacyState;
	}
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}
}
