package edu.ucla.cens.awserver.request;

import java.security.InvalidParameterException;
import java.util.Map;

/**
 * Encapsulating class for all the information pertaining to creating a new
 * campaign. 
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequest extends ResultListAwRequest {
	public static final String KEY_RUNNING_STATE = "runningState";
	public static final String KEY_PRIVACY_STATE = "privacyState";
	public static final String KEY_LIST_OF_CLASSES_AS_STRING = "commaSeparatedListOfClasses";
	
	public static final String KEY_XML = "xml";
	public static final String KEY_DESCRIPTION = "description";
	
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
	public CampaignCreationAwRequest(String runningState, String privacyState, String campaignAsXml, String commaSeparatedListOfClasses, String description) throws InvalidParameterException {
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

		Map<String, Object> toValidate = getToValidate();
		
		toValidate.put(KEY_RUNNING_STATE, runningState);
		toValidate.put(KEY_PRIVACY_STATE, privacyState);
		toValidate.put(KEY_LIST_OF_CLASSES_AS_STRING, commaSeparatedListOfClasses);
		
		toValidate.put(KEY_XML, campaignAsXml);
		toValidate.put(KEY_DESCRIPTION, description);
		
		setToValidate(toValidate);
	}
	
	/**
	 * The initial running state of this campaign.
	 * 
	 * @return The initial running state of this campaign.
	 */
	public String getRunningState() {
		return (String) getToValidate().get(KEY_RUNNING_STATE);
	}
	
	/**
	 * The initial privacy state of this campaign.
	 * 
	 * @return The initial privacy state of this campaign.
	 */
	public String getPrivacyState() {
		return (String) getToValidate().get(KEY_PRIVACY_STATE);
	}
	
	/**
	 * The campaign as an XML file.
	 * 
	 * @return The campaign as an XML file.
	 */
	public String getCampaign() {
		return (String) getToValidate().get(KEY_XML);
	}
	
	/**
	 * The list of classes that this campaign should be associated with 
	 * separated with commas.
	 * 
	 * @return A comma-separated list of classes that this should be initially
	 * 		   associated with.
	 */
	public String getCommaSeparatedListOfClasses() {
		return (String) getToValidate().get(KEY_LIST_OF_CLASSES_AS_STRING);
	}
	
	/**
	 * Returns the description for this campaign.
	 * 
	 * @return The description for this campaign.
	 */
	public String getDescription() {
		return (String) getToValidate().get(KEY_DESCRIPTION);
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
				+ ((getToValidate().containsKey(KEY_DESCRIPTION)) ? (", _description=" + getDescription()) : "" )
				+ "], super.toString()=" + super.toString();
	}
}
