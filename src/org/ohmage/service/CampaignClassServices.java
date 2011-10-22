package org.ohmage.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.impl.CampaignClassQueries;

/**
 * This class contains the services that pertain to campaign-class 
 * associations.
 * 
 * @author John Jenkins
 */
public class CampaignClassServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private CampaignClassServices() {}

	/**
	 * Verifies that the list of classes doesn't cover all of the classes
	 * associated with the campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param classIds The collection of class IDs.
	 * 
	 * @throws ServiceException Thrown if the collection of classes contains 
	 * 							all of the classes to which the campaign is
	 * 							associated or if there is an error.
	 */
	public static void verifyNotDisassocitingAllClassesFromCampaign(
			final String campaignId, final Collection<String> classIds) 
			throws ServiceException {
		
		try {
			Set<String> classIdsCopy = new HashSet<String>(classIds);
			classIdsCopy.removeAll(CampaignClassQueries.getClassesAssociatedWithCampaign(campaignId));
			
			if(classIdsCopy.size() == 0) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user is not allowed to disassociate all classes from the campaign.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}