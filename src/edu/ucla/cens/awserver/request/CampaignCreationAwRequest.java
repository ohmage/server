package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Encapsulating class for all the information pertaining to creating a new
 * campaign. 
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequest extends ResultListAwRequest {	
	/**
	 * Constructor that converts the requested parameters into an AndWellness
	 * request.
	 * 
	 * @param runningState The initial running state of this campaign.
	 * 
	 * @param privacyState The initial privacy state of this campaign.
	 * 
	 * @param remoteXmlLocation The location of the remote XML file on the
	 * 							creator's machine.
	 * 
	 * @param commaSeparatedListOfClasses A list of classes for which this
	 * 									  campaign will initially belong.
	 */
	public CampaignCreationAwRequest(String runningState, String privacyState, String campaignAsXml, String commaSeparatedListOfClasses, String description) throws IllegalArgumentException {
		super();
		
		if(runningState == null) {
			throw new IllegalArgumentException("The initial running state cannot be null.");
		}
		else if(privacyState == null) {
			throw new IllegalArgumentException("The initial privacy state cannot be null.");
		}
		else if(campaignAsXml == null) {
			throw new IllegalArgumentException("The campaign XML cannot be null.");
		}
		else if(commaSeparatedListOfClasses == null) {
			throw new IllegalArgumentException("The initial list of classes for a campaign cannot be null.");
		}

		addToValidate(InputKeys.RUNNING_STATE, runningState, true);
		addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
		addToValidate(InputKeys.CLASS_URN_LIST, commaSeparatedListOfClasses, true);
		
		addToValidate(InputKeys.XML, campaignAsXml, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(description)) {
			addToValidate(InputKeys.DESCRIPTION, description, true);
		}
	}
	
	/**
	 * The initial running state of this campaign.
	 * 
	 * @return The initial running state of this campaign.
	 */
	public String getRunningState() {
		return (String) getToValidate().get(InputKeys.RUNNING_STATE);
	}
	
	/**
	 * The initial privacy state of this campaign.
	 * 
	 * @return The initial privacy state of this campaign.
	 */
	public String getPrivacyState() {
		return (String) getToValidate().get(InputKeys.PRIVACY_STATE);
	}
	
	/**
	 * The campaign as an XML file.
	 * 
	 * @return The campaign as an XML file.
	 */
	public String getCampaign() {
		return (String) getToValidate().get(InputKeys.XML);
	}
	
	/**
	 * The list of classes that this campaign should be associated with 
	 * separated with commas.
	 * 
	 * @return A comma-separated list of classes that this should be initially
	 * 		   associated with.
	 */
	public String getCommaSeparatedListOfClasses() {
		return (String) getToValidate().get(InputKeys.CLASS_URN_LIST);
	}
	
	/**
	 * Returns the description for this campaign.
	 * 
	 * @return The description for this campaign.
	 */
	public String getDescription() {
		return (String) getToValidate().get(InputKeys.DESCRIPTION);
	}
	
	/**
	 * Dumps the local variables for this object.
	 */
	@Override
	public String toString() {
		return "CampaignCreationAwRequest [_runningState=" + getRunningState()
				+ ", _privacyState=" + getPrivacyState()
				+ ", _xml=" + getCampaign()
				+ ", _commaSeparatedListOfClasses=" + getCommaSeparatedListOfClasses()
				+ ((getToValidate().containsKey(InputKeys.DESCRIPTION)) ? (", _description=" + getDescription()) : "" )
				+ "], super.toString()=" + super.toString();
	}
}
