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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage class for mapping a Campaign to a List of user roles.
 * 
 * TODO: make this a Map and store it directly in the User object?
 * 
 * @author Joshua Selsky
 */
public class CampaignAndUserRoles {
	
	private Campaign campaign;
	private List<String> userRoleStrings;
	
	public CampaignAndUserRoles() {
		
	}
	
	public CampaignAndUserRoles(CampaignAndUserRoles original) {
		this.campaign = new Campaign(original.getCampaign());
		
		List<String> originalRoleStrings = original.getUserRoleStrings();
		this.userRoleStrings = new ArrayList<String>();
		for(String originalRoleString : originalRoleStrings) {
			userRoleStrings.add(originalRoleString);
		}
	}
	
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
	
	public Campaign getCampaign() {
		return this.campaign;
	}
	
	public void addUserRoleString(String userRole) {
		if(null == this.userRoleStrings) {
			this.userRoleStrings = new ArrayList<String>();
		}
		this.userRoleStrings.add(userRole);
	}
	
	public List<String> getUserRoleStrings() {
		return Collections.unmodifiableList(this.userRoleStrings);
	}

	@Override
	public String toString() {
		return "CampaignAndUserRoles [campaign=" + campaign
				+ ", userRoleStrings=" + userRoleStrings + "]";
	}
}

