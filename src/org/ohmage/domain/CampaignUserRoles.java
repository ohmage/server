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
 * Storage class for mapping a Campaign to a List of UserRoles for a particular User.
 * 
 * @author Joshua Selsky
 */
public class CampaignUserRoles {
	
	private Campaign _campaign;
	private List<UserRole> _userRoles;
	private List<String> _userRoleStrings;
	
	public void setCampaign(Campaign campaign) {
		_campaign = campaign;
	}
	
	public Campaign getCampaign() {
		return _campaign;
	}
	
	public List<UserRole> getUserRoles() {
		return _userRoles;
	}
	
	public void setUserRoles(List<UserRole> userRoles) {
		_userRoles = userRoles;
		_userRoleStrings = new ArrayList<String>();
		for(UserRole ur : userRoles) {
			_userRoleStrings.add(ur.getRole());
		}
	}
	
	public void addUserRole(UserRole userRole) {
		_userRoles.add(userRole);
		if(null == _userRoleStrings) {
			_userRoleStrings = new ArrayList<String>();
		}
		_userRoleStrings.add(userRole.getRole());
	}
	
	public List<String> getUserRoleStrings() {
		return Collections.unmodifiableList(_userRoleStrings);
	}
}

