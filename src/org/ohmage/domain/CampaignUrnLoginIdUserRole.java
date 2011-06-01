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
 * Simple wrapper for mapping a user role string to a campaign URN for a particular user.
 * 
 * @author selsky
 */
public class CampaignUrnLoginIdUserRole {
	private String _loginId;
	private String _role;
	private String _campaignUrn;
	
	public String getRole() {
		return _role;
	}
	
	public void setRole(String role) {
		_role = role;
	}
	
	public String getLoginId() {
		return _loginId;
	}
	
	public void setLoginId(String loginId) {
		_loginId = loginId;
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}

	@Override
	public String toString() {
		return "CampaignUrnLoginIdUserRole [_campaignUrn=" + _campaignUrn
				+ ", _loginId=" + _loginId + ", _role=" + _role + "]";
	}
}
