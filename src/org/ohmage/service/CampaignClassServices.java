package org.ohmage.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignClassQueries;

/**
 * This class contains the services that pertain to campaign-class 
 * associations.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class CampaignClassServices {	
	private static CampaignClassServices instance;
	private ICampaignClassQueries campaignClassQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 *  
	 * @throws IllegalArgumentException if iCampaignClassQueries is null
	 */
	private CampaignClassServices(ICampaignClassQueries iCampaignClassQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iCampaignClassQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignClassQueries is required.");
		}
		
		campaignClassQueries = iCampaignClassQueries;
		instance = this;
	}

	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static CampaignClassServices instance() {
		return instance;
	}
	
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
	public void verifyNotDisassocitingAllClassesFromCampaign(
			final String campaignId, final Collection<String> classIdsToRemove,
			final Collection<String> classIdsToAdd) 
			throws ServiceException {
		
		try {
			Set<String> currClassIds = new HashSet<String>(campaignClassQueries.getClassesAssociatedWithCampaign(campaignId));
			if(classIdsToAdd != null) {
				currClassIds.addAll(classIdsToAdd);
			}
			currClassIds.removeAll(classIdsToRemove);
			
			if(currClassIds.size() == 0) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user is not allowed to disassociate all classes from the campaign.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns the list of campaign IDs associated with a given class.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @return The list of campaign IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getCampaignIdsForClass(
			final String classId)
			throws ServiceException {
		
		try {
			return campaignClassQueries.getCampaignsAssociatedWithClass(classId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}