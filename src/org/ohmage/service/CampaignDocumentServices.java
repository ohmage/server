package org.ohmage.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.dao.UserCampaignDocumentDaos;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.DocumentInformation;
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
	public static List<DocumentInformation> getDocumentsSpecificToCampaign(Request request, String username, String campaignId) throws ServiceException {
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
	public static List<DocumentInformation> getDocumentsSpecificToCampaigns(Request request, String username, List<String> campaignIds) throws ServiceException {
		Set<DocumentInformation> resultSet = new HashSet<DocumentInformation>();
		for(String campaignId : campaignIds) {
			resultSet.addAll(getDocumentsSpecificToCampaign(request, username, campaignId));
		}
		return new ArrayList<DocumentInformation>(resultSet);
	}
}
