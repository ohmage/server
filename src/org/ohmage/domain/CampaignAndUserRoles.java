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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.cache.CampaignRoleCache;

/**
 * Storage class for mapping a Campaign to a List of user roles.
 * 
 * TODO: make this a Map and store it directly in the User object?
 * Maybe we should just call this CampaignAndRoles?
 * 
 * Better yet, maybe we should just get rid of this class and replace it with
 * Map<Campaign, List<CampaignRoleCache.Role>>?
 * 
 * @author Joshua Selsky
 */
public class CampaignAndUserRoles {
	
	private Campaign campaign;
	private List<CampaignRoleCache.Role> userRoles;

	/**
	 * Default constructor.
	 * 
	 * Should we use the constructor that takes a Campaign instead? We can then
	 * make Campaign final, which seems appropriate.
	 */
	public CampaignAndUserRoles() {
		campaign = null;
		userRoles = new LinkedList<CampaignRoleCache.Role>();
	}
	
	/**
	 * Creates a new CampaignAndUserRoles object with a default Campaign.
	 * 
	 * @param campaign The Campaign for which the user's roles will be later
	 * 				   added.
	 */
	public CampaignAndUserRoles(Campaign campaign) {
		this.campaign = campaign;
		this.userRoles = new LinkedList<CampaignRoleCache.Role>();
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
		userRoles = new ArrayList<CampaignRoleCache.Role>(original.userRoles);
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
	public void addUserRole(CampaignRoleCache.Role userRole) {
		if(null == this.userRoles) {
			this.userRoles = new ArrayList<CampaignRoleCache.Role>();
		}
		this.userRoles.add(userRole);
	}
	
	/**
	 * Adds a Collection of roles to the roles for this Campaign.
	 * 
	 * @param roles A Collection of roles to be added.
	 */
	public void addRoles(Collection<CampaignRoleCache.Role> roles) {
		if(roles != null) {
			userRoles.addAll(roles);
		}
	}

	/**
	 * Retrieves an immutable list of the user's roles in this campaign.
	 * 
	 * @return A List of the user's roles in this campaign.
	 */
	public List<CampaignRoleCache.Role> getUserRoleStrings() {
		return Collections.unmodifiableList(this.userRoles);
	}

	/**
	 * Creates a String from the campaign's toString method and then dumps the
	 * List of campaign roles.
	 */
	@Override
	public String toString() {
		return "CampaignAndUserRoles [campaign=" + campaign
				+ ", userRoles=" + userRoles + "]";
	}
}
