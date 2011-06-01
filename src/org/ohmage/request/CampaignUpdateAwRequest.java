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
package org.ohmage.request;


public class CampaignUpdateAwRequest extends ResultListAwRequest {
	/**
	 * There are no required parameters specific to this request. 
	 */
	public CampaignUpdateAwRequest() {
		super();
	}

	/**
	 * Returns the new description for this campaign.
	 * 
	 * @return The new description for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if no description has been set.
	 */
	public String getDescription() {
		return (String) getToValidate().get(InputKeys.DESCRIPTION);
	}

	/**
	 * Sets the new description for this campaign.
	 * 
	 * @param description The new description for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'description' is null.
	 */
	public void setDescription(String description) {
		addToValidate(InputKeys.DESCRIPTION, description, true);
	}

	/**
	 * Returns the new running state for this campaign.
	 * 
	 * @return The new running state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if no running state has been
	 * 									set.
	 */
	public String getRunningState() {
		return (String) getToValidate().get(InputKeys.RUNNING_STATE);
	}

	/**
	 * Sets the new running state for this campaign.
	 * 
	 * @param runningState The new running state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'runningState' is null.
	 */
	public void setRunningState(String runningState) {
		addToValidate(InputKeys.RUNNING_STATE, runningState, true);
	}

	/**
	 * Returns the new privacy state for this campaign.
	 * 
	 * @return The new privacy state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if no privacy state has been
	 * 									set.
	 */
	public String getPrivacyState() {
		return (String) getToValidate().get(InputKeys.PRIVACY_STATE);
	}

	/**
	 * Sets the new privacy state for this campaign.
	 * 
	 * @param privacyState The new privacy state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'privacyState' is null.
	 */
	public void setPrivacyState(String privacyState) {
		addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
	}

	/**
	 * Returns the new comma-separated list of classes that this campaign is
	 * associated with.
	 *  
	 * @return The new comma-separated list of classes that this campaign is
	 * 		   associated with.
	 * 
	 * @throws IllegalArgumentException Thrown if the list of classes has not
	 * 									been set.
	 */
	public String getCommaSeparatedClasses() {
		return (String) getToValidate().get(InputKeys.CLASS_URN_LIST);
	}

	/**
	 * Sets the new list of classes for this campaign. This should be a comma-
	 * separated list of campaign URNs. An absence of a class that this
	 * campaign was previous associated with will result in this campaign no
	 * longer being associated with that class. Likewise, a class in this list
	 * that this campaign was not associated with before will result in the
	 * class being added to the list of classes that this campaign is
	 * associated with.
	 * 
	 * @param commaSeparatedClasses A comma-separated list of classes that
	 * 								this campaign should be associated with.
	 * 
	 * @throws IllegalArgumentException Thrown if 'commaSeparatedClasses' is
	 * 									null.
	 * 
	 */
	public void setCommaSeparatedClasses(String commaSeparatedClasses) {
		addToValidate(InputKeys.CLASS_URN_LIST, commaSeparatedClasses, true);
	}

	/**
	 * Returns the new XML for this campaign.
	 * 
	 * @return The new XML for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if the XML has not been set.
	 */
	public String getXml() {
		return (String) getToValidate().get(InputKeys.XML);
	}

	/**
	 * Sets the new XML for this campaign.
	 * 
	 * @param xml The new XML for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'xml' is null.
	 */
	public void setXml(String xml) {
		addToValidate(InputKeys.XML, xml, true);
	}
	
	/**
	 * Sets the new XML for this campaign.
	 * 
	 * @param xml The new XML for this campaign as a byte array which is how
	 * 			  the HTTPServlet object will store it.
	 * 
	 * @throws IllegalArgumentException Thrown if 'xml' is null.
	 */
	public void setXmlAsByteArray(byte[] xml) {
		addToValidate(InputKeys.XML, new String(xml), true);
	}
	
	/**
	 * Sets the list of users and their roles to add to the campaign.
	 * 
	 * @param list The list of users and roles to add to a campaign.
	 */
	public void setUserRoleListAdd(String list) {
		addToValidate(InputKeys.USER_ROLE_LIST_ADD, list, true);
	}
	
	/**
	 * Sets the list of users to their roles to remove from the campaign.
	 * 
	 * @param list The list of users and roles to remove from the campaign.
	 */
	public void setUserRoleListRemove(String list) {
		addToValidate(InputKeys.USER_ROLE_LIST_REMOVE, list, true);
	}
}
