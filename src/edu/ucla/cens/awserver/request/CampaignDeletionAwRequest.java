package edu.ucla.cens.awserver.request;

/**
 * AndWellness request object responsible for deleting a campaign.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionAwRequest extends ResultListAwRequest {
	public static final String KEY_CAMPAIGN_URN = "campaignUrn";
	
	/**
	 * Builds this AndWellness request for deleting a campaign. A campaign URN
	 * is required.
	 * 
	 * @param campaignUrn The URN of the campaign which is attempting to be
	 * 					  deleted.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are
	 * 									invalid.
	 */
	public CampaignDeletionAwRequest(String campaignUrn) {
		if(campaignUrn == null) {
			throw new IllegalArgumentException("Attempting to build a CampaignDeletionAwRequest object with a null campaign URN.");
		}
		
		getToValidate().put(KEY_CAMPAIGN_URN, campaignUrn);
	}
	
	/**
	 * Returns the campaign URN that is attempting to be deleted.
	 * 
	 * @return The campaign URN that is attempting to be deleted.
	 */
	public String getRequestUrn() {
		return (String) getToValidate().get(KEY_CAMPAIGN_URN);
	}
	
	/**
	 * Dumps the local variables for this object.
	 */
	@Override
	public String toString() {
		return "CampaignDeletionAwRequest [_urn=" + getRequestUrn()
			   + "], super.toString()=" + super.toString();
	}
}
