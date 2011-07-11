package org.ohmage.service;

import java.util.List;

import org.ohmage.dao.CampaignDocumentDaos;
import org.ohmage.dao.DataAccessException;
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
}
