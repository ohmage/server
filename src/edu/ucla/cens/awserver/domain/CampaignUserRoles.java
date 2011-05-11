package edu.ucla.cens.awserver.domain;

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

