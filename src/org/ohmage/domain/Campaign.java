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
 * Storage of campaign metadata (basically everything except the XML).
 * 
 * @author Joshua Selsky
 */
public class Campaign {
	private String urn;
	private String name;
	private String description;
	private String runningState;
	private String privacyState;
	private String campaignCreationTimestamp;
	
	public Campaign() {
		
	}
	
	public Campaign(Campaign campaign) {
		this.urn = campaign.urn;
		this.name = campaign.name;
		this.description = campaign.description;
		this.runningState = campaign.runningState;
		this.privacyState = campaign.privacyState;
		this.campaignCreationTimestamp = campaign.campaignCreationTimestamp;
	}
	
	public String getUrn() {
		return this.urn;
	}
	
	public void setUrn(String urn) {
		this.urn = urn;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getRunningState() {
		return this.runningState;
	}
	
	public void setRunningState(String runningState) {
		this.runningState = runningState;
	}
	
	public String getPrivacyState() {
		return this.privacyState;
	}
	
	public void setPrivacyState(String privacyState) {
		this.privacyState = privacyState;
	}
	
	public String getCampaignCreationTimestamp() {
		return this.campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		this.campaignCreationTimestamp = campaignCreationTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((campaignCreationTimestamp == null) ? 0
						: campaignCreationTimestamp.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((privacyState == null) ? 0 : privacyState.hashCode());
		result = prime * result
				+ ((runningState == null) ? 0 : runningState.hashCode());
		result = prime * result + ((urn == null) ? 0 : urn.hashCode());
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
		Campaign other = (Campaign) obj;
		if (campaignCreationTimestamp == null) {
			if (other.campaignCreationTimestamp != null)
				return false;
		} else if (!campaignCreationTimestamp
				.equals(other.campaignCreationTimestamp))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (privacyState == null) {
			if (other.privacyState != null)
				return false;
		} else if (!privacyState.equals(other.privacyState))
			return false;
		if (runningState == null) {
			if (other.runningState != null)
				return false;
		} else if (!runningState.equals(other.runningState))
			return false;
		if (urn == null) {
			if (other.urn != null)
				return false;
		} else if (!urn.equals(other.urn))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Campaign [urn=" + urn + ", name=" + name + ", description="
				+ description + ", runningState=" + runningState
				+ ", privacyState=" + privacyState
				+ ", campaignCreationTimestamp=" + campaignCreationTimestamp
				+ "]";
	}
}
