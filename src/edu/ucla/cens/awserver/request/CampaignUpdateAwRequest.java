package edu.ucla.cens.awserver.request;

public class CampaignUpdateAwRequest extends ResultListAwRequest {
	/**
	 * There are no required parameters specific to this request. 
	 */
	public CampaignUpdateAwRequest() {
		// Does nothing.
	}

	/**
	 * Returns the new description for this campaign.
	 * 
	 * @return The new description for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if no description has been set.
	 */
	public String getDescription() {
		return (String) getToProcessValue(InputKeys.DESCRIPTION);
	}

	/**
	 * Sets the new description for this campaign.
	 * 
	 * @param description The new description for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'description' is null.
	 */
	public void setDescription(String description) {
		addToProcess(InputKeys.DESCRIPTION, description, true);
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
		return (String) getToProcessValue(InputKeys.RUNNING_STATE);
	}

	/**
	 * Sets the new running state for this campaign.
	 * 
	 * @param runningState The new running state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'runningState' is null.
	 */
	public void setRunningState(String runningState) {
		addToProcess(InputKeys.RUNNING_STATE, runningState, true);
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
		return (String) getToProcessValue(InputKeys.PRIVACY_STATE);
	}

	/**
	 * Sets the new privacy state for this campaign.
	 * 
	 * @param privacyState The new privacy state for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'privacyState' is null.
	 */
	public void setPrivacyState(String privacyState) {
		addToProcess(InputKeys.PRIVACY_STATE, privacyState, true);
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
		return (String) getToProcessValue(InputKeys.CLASS_URN_LIST);
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
		addToProcess(InputKeys.CLASS_URN_LIST, commaSeparatedClasses, true);
	}

	/**
	 * Returns the new XML for this campaign.
	 * 
	 * @return The new XML for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if the XML has not been set.
	 */
	public String getXml() {
		return (String) getToProcessValue(InputKeys.XML);
	}

	/**
	 * Sets the new XML for this campaign.
	 * 
	 * @param xml The new XML for this campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if 'xml' is null.
	 */
	public void setXml(String xml) {
		addToProcess(InputKeys.XML, xml, true);
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
		addToProcess(InputKeys.XML, new String(xml), true);
	}
}