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
 * @author Joshua Selsky
 */
public class UserRoleCampaignInfo {
	private String campaignUrn;
	private String campaignName;
	private String campaignDescription;
	private String campaignPrivacyState;
	private String campaignRunningState;
	private String campaignCreationTimestamp;
	private String userRole;
	
	public UserRoleCampaignInfo(String campaignUrn, String campaignName, String campaignDescription, String campaignPrivacyState,
			String campaignRunningState, String campaignCreationTimestamp, String userRole) {
		this.campaignUrn = campaignUrn;
		this.campaignName = campaignName;
		this.campaignDescription = campaignDescription;
		this.campaignPrivacyState = campaignPrivacyState;
		this.campaignRunningState = campaignRunningState;
		this.campaignCreationTimestamp = campaignCreationTimestamp;
		this.userRole = userRole;
	}
	
	public String getCampaignUrn() {
		return this.campaignUrn;
	}
	
	public String getCampaignName() {
		return this.campaignName;
	}

	public String getCampaignDescription() {
		return this.campaignDescription;
	}

	public String getCampaignPrivacyState() {
		return this.campaignPrivacyState;
	}

	public String getCampaignRunningState() {
		return this.campaignRunningState;
	}

	public String getCampaignCreationTimestamp() {
		return this.campaignCreationTimestamp;
	}
	
	public String getUserRole() {
		return this.userRole;
	}
}
