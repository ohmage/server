package edu.ucla.cens.awserver.domain;

import java.util.List;

/**
 * Storage class for mapping a Campaign to a List of UserRoles for a particular User.
 * 
 * @author Joshua Selsky
 */
public class CampaignUserRoles {
	
	private Campaign _campaign;
	private List<UserRole> _userRoles;
	
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
	}
	
	public void addUserRole(UserRole userRole) {
		_userRoles.add(userRole);
	}
}

