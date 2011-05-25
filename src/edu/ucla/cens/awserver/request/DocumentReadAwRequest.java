package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * A request for a list of documents and their information.
 * 
 * @author John Jenkins
 */
public class DocumentReadAwRequest extends ResultListAwRequest {
	public static final String KEY_DOCUMENT_INFORMATION = "document_read_request_document_information";
	
	/**
	 * Creates a request for reading document information.
	 * 
	 * @param personalDocuments A String whether documents belonging to the
	 * 							currently logged in user through user
	 * 							associations only should be returned.
	 * 
	 * @param campaignUrnList A list of campaigns to which document information
	 * 						  is being requested.
	 * 
	 * @param classUrnList A list of classes to which document information is
	 * 					   being requested.
	 */
	public DocumentReadAwRequest(String personalDocuments, String campaignUrnList, String classUrnList) {
		if(StringUtils.isEmptyOrWhitespaceOnly(personalDocuments)) {
			throw new IllegalArgumentException("The value for personal documents cannot be null or whitespace only.");
		}
		
		addToValidate(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS, personalDocuments, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(campaignUrnList)) {
			addToValidate(InputKeys.CAMPAIGN_URN_LIST, campaignUrnList, true);
		}
		else if(! StringUtils.isEmptyOrWhitespaceOnly(classUrnList)) {
			addToValidate(InputKeys.CLASS_URN_LIST, classUrnList, true);
		}
	}
}
