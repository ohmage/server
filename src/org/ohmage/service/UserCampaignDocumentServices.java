package org.ohmage.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserCampaignDocumentDaos;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services for user-campaign-document relationships.
 * 
 * @author John Jenkins
 */
public class UserCampaignDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserCampaignDocumentServices() {}
	
	/**
	 * Verifies that a user can associate documents with a campaign. The only 
	 * restriction is that the user must belong to the campaign in some 
	 * capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignId The campaign ID of the campaign in question.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to associate documents with this
	 * 							campaign.
	 */
	public static void userCanAssociateDocumentsWithCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<String> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.size() == 0) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is not a member of the following campaign and, therefore, cannot associate documents with it: " + campaignId);
				throw new ServiceException("The user is not a member of the following campaign and, therefore, cannot associate documents with it: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user can disassociate documents from a campaign. The
	 * only restriction is that the user must belong to the campaign in some
	 * capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignId The campaign ID of the campaign in question.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to disassociate documents from this
	 * 							campaign.
	 */
	public static void userCanDisassociateDocumentsFromCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<String> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.size() == 0) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is not a member of the following campaign and, therefore, cannot disassociate documents from it: " + campaignId);
				throw new ServiceException("The user is not a member of the following campaign and, therefore, cannot disassociate documents from it: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user can associate documents with all of the campaigns
	 * in a list. The only restriction is that the user must belong to each of
	 * the campaigns.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignIds A List of campaign IDs where each campaign ID will be
	 * 					  checked that the user is a member of it.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to associate documents with any of
	 * 							the campaigns.
	 */
	public static void userCanAssociateDocumentsWithCampaigns(Request request, String username, Collection<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			userCanAssociateDocumentsWithCampaign(request, username, campaignId);
		}
	}
	
	/**
	 * Verifies that a user can disassociate documents from all of the 
	 * campaigns in a List. The only restrictino is that the user must belong
	 * to each of the campaigns.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignIds A List of campaign IDs where each campaign ID will be
	 * 					  checked that the user is a member of it.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to disassociate documents with any
	 * 							of the campaigns.
	 */
	public static void userCanDisassociateDocumentsFromCampaigns(Request request, String username, Collection<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			userCanDisassociateDocumentsFromCampaign(request, username, campaignId);
		}
	}
	
	/**
	 * Retrieves a List of DocumentInformation for each of the visible 
	 * documents associated with a campaign. Visibility is based on the user's
	 * role in the campaign and the documents' privacy state.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param campaignId The campaign ID for the campaign whose documents are
	 * 					 desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   represents a document associated with this campaign.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<DocumentInformation> getVisibleDocumentsSpecificToCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			return UserCampaignDocumentDaos.getVisibleDocumentsToUserInCampaign(username, campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of DocumentInformation objects for all of the visible
	 * documents associated with all of the campaigns. Visibility is based on
	 * the user's role in the campaigns and the documents' privacy state. 
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param campaignIds A List of campaign IDs for the campaigns whose 
	 * 					  documents are desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   represents a document associated with any of the campaigns in
	 * 		   the list. This will not contain duplicates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<DocumentInformation> getVisibleDocumentsSpecificToCampaigns(Request request, String username, List<String> campaignIds) throws ServiceException {
		Set<DocumentInformation> resultSet = new HashSet<DocumentInformation>();
		for(String campaignId : campaignIds) {
			resultSet.addAll(getVisibleDocumentsSpecificToCampaign(request, username, campaignId));
		}
		return new ArrayList<DocumentInformation>(resultSet);
	}
	
	/**
	 * Retrieves whether or not the user is a supervisor in any of the 
	 * campaigns with which the document is associated.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return Returns true if the user is a supervisor in any campaign to which
	 * 		   the document is associated.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static boolean getUserIsSupervisorInAnyCampaignAssociatedWithDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			return UserCampaignDocumentDaos.getUserIsSupervisorInAnyCampaignAssociatedWithDocument(username, documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
