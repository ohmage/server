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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;

/**
 * The default user implementation.
 * 
 * @author selsky
 */
public class User {
	private int _id;
	private String  _userName;
	private Map<String, CampaignUserRoles> _campaignUserRoleMap; // a user can have many roles in one campaign
	private Map<String, ClassUserRoles> _classUserRoleMap;
	private boolean _loggedIn;
	private String _password;
	
	public User() {
		_id = -1;
		_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
		_classUserRoleMap = new HashMap<String, ClassUserRoles>();
	}
	
	/**
	 * Copy constructor.
	 */
	public User(User user) {
		if(null == user) {
			throw new IllegalArgumentException("a null user is not allowed");
		}
		_id = user.getId();
		_userName = user.getUserName();
		_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
		// Authentication no longer sets the user roles on the user, so the users
		// are added to the bin with no roles. It is the responsibility of application
		// flows that require knowledge of the user's role to obtain them at runtime.
		_campaignUserRoleMap.putAll(user.getCampaignUserRoleMap()); // shallow copy ok because once a user is created it is read-only in practice
		_classUserRoleMap = new HashMap<String, ClassUserRoles>();
		_classUserRoleMap.putAll(user._classUserRoleMap);
		_loggedIn = user.isLoggedIn();
	}
	
    public int getId() {
    	return _id;
    }
    
    public void setId(int id) {
    	_id = id;
    }
    
	public Map<String, CampaignUserRoles> getCampaignUserRoleMap() {
		return _campaignUserRoleMap;
	}
	
	public void addCampaignRole(Campaign campaign, UserRole userRole) {
		if(null == _campaignUserRoleMap) {
			_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
		}
		
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaign.getUrn());
		
		if(null == campaignUserRoles) {
			campaignUserRoles = new CampaignUserRoles();
			campaignUserRoles.setCampaign(campaign);
			List<UserRole> userRoles = new ArrayList<UserRole>();
			campaignUserRoles.setUserRoles(userRoles);
			_campaignUserRoleMap.put(campaign.getUrn(), campaignUserRoles);
		}
		
		campaignUserRoles.addUserRole(userRole);
	}
	
	/**
	 * Adds the role to the class for this user.
	 * 
	 * @param classInfo A ClassDescription for which this role applies to this
	 * 					user.
	 * 
	 * @param role The role to associate to this user for this class.
	 */
	public void addClassRole(ClassDescription classInfo, String role) {
		ClassUserRoles classUserRoles = _classUserRoleMap.get(classInfo.getUrn());
		
		if(classUserRoles == null) {
			classUserRoles = new ClassUserRoles(classInfo);
			_classUserRoleMap.put(classInfo.getUrn(), classUserRoles);
		}
		
		classUserRoles.addRole(role);
	}
	
	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}
	
	public boolean isLoggedIn() {
		return _loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		_loggedIn = loggedIn;
	}
	
	public void setPassword(String password) {
		_password = password;
	}

	public String getPassword() {
		return _password;
	}
	
	public boolean isSupervisorInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			return false;
		}
		
		return campaignUserRoles.getUserRoleStrings().contains(CampaignRoleCache.ROLE_SUPERVISOR);
	}
	
	public boolean isAuthorInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			return false;
		}

		return campaignUserRoles.getUserRoleStrings().contains(CampaignRoleCache.ROLE_AUTHOR);
	}
	
	public boolean isAnalystInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			return false;
		}

		return campaignUserRoles.getUserRoleStrings().contains(CampaignRoleCache.ROLE_ANALYST);
	}
	
	public boolean isParticipantInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			return false;
		}

		return campaignUserRoles.getUserRoleStrings().contains(CampaignRoleCache.ROLE_PARTICIPANT);
	}
	
	public boolean hasRoleInCampaign(String campaignUrn, String role) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(campaignUserRoles == null) {
			return false;
		}
		
		return campaignUserRoles.getUserRoleStrings().contains(role);
	}
	
	public boolean isPrivilegedInClass(String classUrn) {
		ClassUserRoles classUserRoles = _classUserRoleMap.get(classUrn);
		if(classUserRoles == null) {
			return false;
		}
		
		return classUserRoles.getRoles().contains(ClassRoleCache.ROLE_PRIVILEGED);
	}
	
	public boolean isRestrictedInClass(String classUrn) {
		ClassUserRoles classUserRoles = _classUserRoleMap.get(classUrn);
		if(classUserRoles == null) {
			return false;
		}
		
		return classUserRoles.getRoles().contains(ClassRoleCache.ROLE_RESTRICTED);
	}
	
	public boolean hasRoleInClass(String classUrn, String role) {
		ClassUserRoles classUserRoles = _classUserRoleMap.get(classUrn);
		if(classUserRoles == null) {
			return false;
		}
		
		return classUserRoles.getRoles().contains(role);
	}
	
	public boolean isOnlyAnalystOrAuthor(String campaignUrn) {
		return (isAnalystInCampaign(campaignUrn) 
				&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1))
				
			||  (isAuthorInCampaign(campaignUrn) 
					&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1))
					
			|| (isAnalystInCampaign(campaignUrn) 
			    	&& isAuthorInCampaign(campaignUrn) 
			    	&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 2));
	}
	
	// NOTE: if you regenerate this toString() automatically in your IDE, please remember to omit the user's password!
	@Override
	public String toString() {
		return "User [_id=" + _id + ", _userName=" + _userName
				+ ", _campaignUserRoleMap=" + _campaignUserRoleMap
				+ ", _loggedIn=" + _loggedIn + ", _password=omitted]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((_campaignUserRoleMap == null) ? 0 : _campaignUserRoleMap
						.hashCode());
		result = prime
				* result
				+ ((_classUserRoleMap == null) ? 0 : _classUserRoleMap
						.hashCode());
		result = prime * result + _id;
		result = prime * result + (_loggedIn ? 1231 : 1237);
		result = prime * result
				+ ((_password == null) ? 0 : _password.hashCode());
		result = prime * result
				+ ((_userName == null) ? 0 : _userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (_campaignUserRoleMap == null) {
			if (other._campaignUserRoleMap != null)
				return false;
		} else if (!_campaignUserRoleMap.equals(other._campaignUserRoleMap))
			return false;
		if (_classUserRoleMap == null) {
			if (other._classUserRoleMap != null)
				return false;
		} else if (!_classUserRoleMap.equals(other._classUserRoleMap))
			return false;
		if (_id != other._id)
			return false;
		if (_loggedIn != other._loggedIn)
			return false;
		if (_userName == null) {
			if (other._userName != null)
				return false;
		} else if (!_userName.equals(other._userName))
			return false;
		return true;
	}
}
