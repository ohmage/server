package edu.ucla.cens.awserver.request;

/**
 * AndWellness request object responsible for deleting a campaign.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionAwRequest extends ResultListAwRequest {
	
	/**
	 * Everything that this requests tracks, exists in the superclass.
	 */
	public CampaignDeletionAwRequest() {
		super();
	}
	
	/**
	 * This class has no variables, so it states that and calls toString on
	 * its superclass.
	 */
	@Override
	public String toString() {
		return "CampaignDeletionAwRequest [], super.toString()=" + super.toString();
	}
}
