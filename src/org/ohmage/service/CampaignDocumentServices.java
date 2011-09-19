package org.ohmage.service;

import java.util.Collection;
import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.CampaignDocumentDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to campaign-document 
 * associations.
 * 
 * @author John Jenkins
 */
public class CampaignDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private CampaignDocumentServices() {}
	
	/**
	 * Retrieves a List of campaign IDs that are associated with a document.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return A List of campaign unique identifiers.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getCampaignsAssociatedWithDocument(Request request, String documentId) throws ServiceException {
		try {
			return CampaignDocumentDaos.getCampaignsAssociatedWithDocument(documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns the document role for a given document with a given campaign.
	 * 
	 * @param request The Request performing this service.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The campaign's document role or null if it is not associated 
	 * 		   with the document.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static DocumentRoleCache.Role getDocumentRoleForCampaign(Request request, String campaignId, String documentId) throws ServiceException {
		try {
			return CampaignDocumentDaos.getCampaignDocumentRole(campaignId, documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and a campaign based on the campaign's role with the document.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the campaign and document. 
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate the campaign and document.
	 */
	public static void ensureRoleHighEnoughToDisassociateDocumentFromCampaign(Request request, DocumentRoleCache.Role role, String campaignId, String documentId) throws ServiceException {
		DocumentRoleCache.Role campaignDocumentRole = getDocumentRoleForCampaign(request, campaignId, documentId);
		
		if(role.compare(campaignDocumentRole) < 0) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "Insufficient permissions to disassociate the document '" + documentId + "' with the campaign '" + campaignId + "' as the campaign has a higher role.");
			throw new ServiceException("Insufficient permissions to disassociate the document '" + documentId + "' with the campaign '" + campaignId + "' as the campaign has a higher role.");
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and each of the campaigns in a collection based on the 
	 * campaigns' individual roles with the document.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the campaigns and document. 
	 * 
	 * @param campaignIds The campaigns' unique identifiers.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate a campaign and document.
	 */
	public static void ensureRoleHighEnoughToDisassociateDocumentFromCampaigns(Request request, DocumentRoleCache.Role role, Collection<String> campaignIds, String documentId) throws ServiceException {
		for(String campaignId : campaignIds) {
			ensureRoleHighEnoughToDisassociateDocumentFromCampaign(request, role, campaignId, documentId);
		}
	}
}