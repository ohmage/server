package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

public class DocumentUpdateRequest extends ResultListAwRequest {
	/**
	 * Builds this request with the given parameters.
	 * 
	 * @param documentId The identifier for this document.
	 * 
	 * @param newName A new name for this document.
	 * 
	 * @param newDescription A new description for this document.
	 * 
	 * @param newPrivacyState The new privacy state for this document.
	 * 
	 * @param newContents The new contents of this document.
	 * 
	 * @param campaignRoleListAdd A list of campaigns and their respective 
	 * 							  roles to begin associating with this
	 * 							  document.
	 * 
	 * @param campaignListRemove A list of campaigns to disassociate with this
	 * 							 document.
	 * 
	 * @param classRoleListAdd A list of classes and their respective roles to
	 * 						   begin associating with this document.
	 * 
	 * @param classListRemove A list of classes to disassociate with this
	 * 						  document.
	 * 
	 * @param userRoleListAdd A list of users and their respective roles to 
	 * 						  begin associating with this document.
	 * 
	 * @param userListRemove A list of users to disassociate with this 
	 * 						 document.
	 */
	public DocumentUpdateRequest(String documentId, String newName, String newDescription, String newPrivacyState,
			String newContents, String campaignRoleListAdd, String campaignListRemove, String classRoleListAdd, String classListRemove,
			String userRoleListAdd, String userListRemove) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document ID cannot be null or whitespace only.");
		}
		else {
			addToValidate(InputKeys.DOCUMENT_ID, documentId, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newName)) {
			addToValidate(InputKeys.DOCUMENT_NAME, newName, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newDescription)) {
			addToValidate(InputKeys.DESCRIPTION, newDescription, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newPrivacyState)) {
			addToValidate(InputKeys.PRIVACY_STATE, newPrivacyState, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newContents)) {
			addToValidate(InputKeys.DOCUMENT, newContents, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(campaignRoleListAdd)) {
			addToValidate(InputKeys.CAMPAIGN_ROLE_LIST_ADD, campaignRoleListAdd, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(campaignListRemove)) {
			addToValidate(InputKeys.CAMPAIGN_LIST_REMOVE, campaignListRemove, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(classRoleListAdd)) {
			addToValidate(InputKeys.CLASS_ROLE_LIST_ADD, classRoleListAdd, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(classListRemove)) {
			addToValidate(InputKeys.CLASS_LIST_REMOVE, classListRemove, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userRoleListAdd)) {
			addToValidate(InputKeys.USER_ROLE_LIST_ADD, userRoleListAdd, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListRemove)) {
			addToValidate(InputKeys.USER_LIST_REMOVE, userListRemove, true);
		}
	}
}
