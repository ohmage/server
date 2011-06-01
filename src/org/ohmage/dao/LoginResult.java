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
package org.ohmage.dao;

/**
 * Container used for authentication query results.
 * 
 * @author selsky
 */
public class LoginResult {
//	private int _campaignId;
//	private String _campaignUrn;
//	private int _userRoleId;
	private int _userId;
	private boolean _enabled;
	private boolean _new;
	
//	public int getCampaignId() {
//		return _campaignId;
//	}
//	public void setCampaignId(int campaignId) {
//		_campaignId = campaignId;
//	}
	public int getUserId() {
		return _userId;
	}
	
	public void setUserId(int userId) {
		_userId = userId;
	}
	
	public boolean isEnabled() {
		return _enabled;
	}
	
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	public boolean isNew() {
		return _new;
	}
	
	public void setNew(boolean bnew) {
		_new = bnew;
	}
	
/*	public int getUserRoleId() {
		return _userRoleId;
	}
	
	public void setUserRoleId(int userRoleId) {
		_userRoleId = userRoleId;
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}*/
}
