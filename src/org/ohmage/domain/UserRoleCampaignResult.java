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
 * @author joshua selsky
 */
public class UserRoleCampaignResult {
	private String _campaignUrn;
	private String _campaignName;
	private String _campaignDescription;
	private String _campaignPrivacyState;
	private String _campaignRunningState;
	private String _campaignCreationTimestamp;
	private UserRole _userRole;
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}

	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}
	
	public String getCampaignName() {
		return _campaignName;
	}

	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
	}

	public String getCampaignDescription() {
		return _campaignDescription;
	}

	public void setCampaignDescription(String campaignDescription) {
		_campaignDescription = campaignDescription;
	}

	public String getCampaignPrivacyState() {
		return _campaignPrivacyState;
	}

	public void setCampaignPrivacyState(String campaignPrivacyState) {
		_campaignPrivacyState = campaignPrivacyState;
	}

	public String getCampaignRunningState() {
		return _campaignRunningState;
	}

	public void setCampaignRunningState(String campaignRunningState) {
		_campaignRunningState = campaignRunningState;
	}
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}
	
	public UserRole getUserRole() {
		return _userRole;
	}
	
	public void setUserRole(UserRole userRole) {
		_userRole = userRole;
	}

	@Override
	public String toString() {
		return "UserRoleCampaignResult [_campaignUrn=" + _campaignUrn
				+ ", _campaignName=" + _campaignName
				+ ", _campaignDescription=" + _campaignDescription
				+ ", _campaignPrivacyState=" + _campaignPrivacyState
				+ ", _campaignRunningState=" + _campaignRunningState
				+ ", _campaignCreationTimestamp=" + _campaignCreationTimestamp
				+ ", _userRole=" + _userRole + "]";
	}
}
