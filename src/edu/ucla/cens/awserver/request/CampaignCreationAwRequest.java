package edu.ucla.cens.awserver.request;

import java.security.InvalidParameterException;

/**
 * Encapsulating class for all the information pertaining to creating a new
 * campaign. 
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequest extends ResultListAwRequest {
	private String _runningState;
	private String _privacyState;
	private String _commaSeparatedListOfClasses;

	private String _xml;
	
	// This won't be set until after it is inserted into the database.
	private String _id;
	
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
	public CampaignCreationAwRequest(String runningState, String privacyState, String campaignAsXml, String commaSeparatedListOfClasses) throws InvalidParameterException {
		if(runningState == null) {
			throw new InvalidParameterException("The initial running state cannot be null.");
		}
		else if(privacyState == null) {
			throw new InvalidParameterException("The initial privacy state cannot be null.");
		}
		else if(campaignAsXml == null) {
			throw new InvalidParameterException("The campaign XML cannot be null.");
		}
		else if(commaSeparatedListOfClasses == null) {
			throw new InvalidParameterException("The initial list of classes for a campaign cannot be null.");
		}
		_runningState = runningState;
		_privacyState = privacyState;
		_commaSeparatedListOfClasses = commaSeparatedListOfClasses;
		
		_xml = campaignAsXml;
		
		_id = null;
	}
	
	/**
	 * The initial running state of this campaign.
	 * 
	 * @return The initial running state of this campaign.
	 */
	public String getRunningState() {
		return _runningState;
	}
	
	/**
	 * The initial privacy state of this campaign.
	 * 
	 * @return The initial privacy state of this campaign.
	 */
	public String getPrivacyState() {
		return _privacyState;
	}
	
	/**
	 * The campaign as an XML file.
	 * 
	 * @return The campaign as an XML file.
	 */
	public String getCampaign() {
		return _xml;
	}
	
	/**
	 * The list of classes that this campaign should be associated with 
	 * separated with commas.
	 * 
	 * @return A comma-separated list of classes that this should be initially
	 * 		   associated with.
	 */
	public String getCommaSeparatedListOfClasses() {
		return _commaSeparatedListOfClasses;
	}
	
	/**
	 * Sets the identifier for this campaign. Shouldn't be done until it is
	 * being inserted into the database or shortly thereafter.
	 * 
	 * @param id The identifier for this campaign. Not valid until the XML is
	 * 			 is validated and shortly before or after it is inserted into
	 * 			 the database.
	 */
	public void setCampaignId(String id) {
		_id = id;
	}
	
	/**
	 * Gets the identifier for this campaign. This won't be set until after it
	 * has been validated and is ready to be or has already been inserted into
	 * the database.
	 * 
	 * @return The identifier for this campaign or null if it has not yet been
	 * 		   set.
	 */
	public String getCampaignId() {
		return _id;
	}
	
	/**
	 * Dumps the local variables for this object.
	 */
	@Override
	public String toString() {
		return "CampaignCreationAwRequest [_runningState=" + _runningState
				+ ", _privacyState=" + _privacyState
				+ ", _xml=" + _xml
				+ ", _commaSeparatedListOfClasses=" + _commaSeparatedListOfClasses
				+ "], super.toString()=" + super.toString();
	}
}
