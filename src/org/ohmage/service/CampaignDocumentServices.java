package org.ohmage.service;

import java.util.Collection;
import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignDocumentQueries;

/**
 * This class contains the services that pertain to campaign-document 
 * associations.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class CampaignDocumentServices {
	private static CampaignDocumentServices instance;
	private ICampaignDocumentQueries campaignDocumentQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignDocumentQueries is null
	 */
	private CampaignDocumentServices(ICampaignDocumentQueries iCampaignDocumentQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(iCampaignDocumentQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignDocumentQueries is required.");
		}
		
		campaignDocumentQueries = iCampaignDocumentQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static CampaignDocumentServices instance() {
		return instance;
	}
	
	/**
	 * Retrieves a List of campaign IDs that are associated with a document.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return A List of campaign unique identifiers.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getCampaignsAssociatedWithDocument(
			final String documentId) throws ServiceException {
		
		try {
			return campaignDocumentQueries.getCampaignsAssociatedWithDocument(documentId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns the document role for a given document with a given campaign.
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
	public Document.Role getDocumentRoleForCampaign(
			final String campaignId, final String documentId) 
			throws ServiceException {
		
		try {
			return campaignDocumentQueries.getCampaignDocumentRole(campaignId, documentId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and a campaign based on the campaign's role with the document.
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
	public void ensureRoleHighEnoughToDisassociateDocumentFromCampaign(
			final Document.Role role, final String campaignId, 
			final String documentId) throws ServiceException {
		
		Document.Role campaignDocumentRole = getDocumentRoleForCampaign(campaignId, documentId);
		
		if(role.compare(campaignDocumentRole) < 0) {
			throw new ServiceException(
					ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
					"Insufficient permissions to disassociate the document '" +
						documentId + 
						"' with the campaign '" + 
						campaignId + 
						"' as the campaign has a higher role.");
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and each of the campaigns in a collection based on the 
	 * campaigns' individual roles with the document.
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
	public void ensureRoleHighEnoughToDisassociateDocumentFromCampaigns(
			final Document.Role role, final Collection<String> campaignIds, 
			final String documentId) throws ServiceException {
		
		for(String campaignId : campaignIds) {
			ensureRoleHighEnoughToDisassociateDocumentFromCampaign(role, campaignId, documentId);
		}
	}
}