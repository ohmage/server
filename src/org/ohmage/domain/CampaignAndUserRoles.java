package org.ohmage.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Storage class for mapping a Campaign to a List of user roles.
 * 
 * Maybe we should just call this CampaignAndRoles?
 * 
 * Better yet, maybe we should just get rid of this class and replace it with
 * Map<Campaign, List<String>>?
 * 
 * @author Joshua Selsky
 */
public class CampaignAndUserRoles {
	private Campaign campaign;
	private List<String> userRoleStrings;

	/**
	 * Default constructor.
	 * 
	 * Should we use the constructor that takes a Campaign instead? We can then
	 * make Campaign final, which seems appropriate.
	 */
	public CampaignAndUserRoles() {
		campaign = null;
		userRoleStrings = new LinkedList<String>();
	}
	
	/**
	 * Creates a new CampaignAndUserRoles object with a default Campaign.
	 * 
	 * @param campaign The Campaign for which the user's roles will be later
	 * 				   added.
	 */
	public CampaignAndUserRoles(Campaign campaign) {
		this.campaign = campaign;
		this.userRoleStrings = new LinkedList<String>();
	}

	/**
	 * Copy constructor. This creates as deep of a copy as possible of the 
	 * original Campaign and user-role list.
	 * 
	 * @param original The CampaignAndUserRoles of which this new object will
	 * 				   be based.
	 */
	public CampaignAndUserRoles(CampaignAndUserRoles original) {
		campaign = new Campaign(original.campaign);
		userRoleStrings = new ArrayList<String>(original.userRoleStrings);
	}

	/**
	 * Sets the campaign object.
	 * 
	 * @param campaign A Campaign object for this campaign, user-role 
	 * 				   relationship.
	 */
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	/**
	 * Retrieves the Campaign for this relationship.
	 * 
	 * @return The Campaign for his relationship.
	 */
	public Campaign getCampaign() {
		return this.campaign;
	}

	/**
	 * Adds a user-role to the list of user roles for some user for this
	 * campaign.
	 * 
	 * @param userRole The campaign role to add.
	 */
	public void addUserRoleString(String userRole) {
		if(null == this.userRoleStrings) {
			this.userRoleStrings = new ArrayList<String>();
		}
		this.userRoleStrings.add(userRole);
	}
	
	/**
	 * Adds a Collection of roles to the roles for this Campaign.
	 * 
	 * @param roles A Collection of roles to be added.
	 */
	public void addRoles(Collection<String> roles) {
		if(roles != null) {
			userRoleStrings.addAll(roles);
		}
	}

	/**
	 * Retrieves an immutable list of the user's roles in this campaign.
	 * 
	 * @return A List of the user's roles in this campaign.
	 */
	public List<String> getUserRoleStrings() {
		return Collections.unmodifiableList(this.userRoleStrings);
	}

	/**
	 * Creates a String from the campaign's toString method and then dumps the
	 * List of campaign roles.
	 */
	@Override
	public String toString() {
		return "CampaignAndUserRoles [campaign=" + campaign
				+ ", userRoleStrings=" + userRoleStrings + "]";
	}
}