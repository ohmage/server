package edu.ucla.cens.awserver.request;

/**
 * A request for getting information about a user.
 * 
 * @author John Jenkins
 */
public class UserReadRequest extends AbstractAwRequest {
	public static final String RESULT = "user_read_request_user_information";
	
	/**
	 * Builds a request for reading information about a user.
	 * 
	 * @param campaignIdList A list of campaigns whose users' information is
	 * 						 desired.
	 * 
	 * @param classIdList A list of classes whose users' information is 
	 * 					  desired.
	 */
	public UserReadRequest(String campaignIdList, String classIdList) {
		super();
		
		addToValidate(InputKeys.CAMPAIGN_URN_LIST, campaignIdList, true);
		addToValidate(InputKeys.CLASS_URN_LIST, classIdList, true);
	}
}
