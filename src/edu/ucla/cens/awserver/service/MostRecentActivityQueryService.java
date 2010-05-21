package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.MobilityActivityQueryResult;
import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.domain.PromptActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.DateUtils;

/**
 * Service for finding the timestamp of the most recent activity (either mobility uploads or prompt response uploads). If the 
 * currently logged-in user is a researcher or an admin, this service will return the timestamp of the most recent activity for 
 * all of the users in a campaign. If an activity is found, the timestamp is normalized to the server timezone and the difference in
 * hours is calculated.  
 * 
 * @author selsky
 */
public class MostRecentActivityQueryService implements Service {
	private CacheService _userRoleCacheService;
	private Dao _singleUserQueryDao;
	private Dao _multiUserQueryDao;
	
	/**
     * @param userRoleCacheService a lookup cache for determining user roles
     * @param singleUserQueryDao retrieve most recent activity query results for a single user
     * @param multiUserQueryDao retrieve most recent activity query results for multiple users
     * 
     * @throws IllegalArgumentException if the provided CacheService is null
     * @throws IllegalArgumentException if either of the provided DAOs are null
     */
    public MostRecentActivityQueryService(CacheService userRoleCacheService, Dao singleUserQueryDao, Dao multiUserQueryDao) {
    	if(null == userRoleCacheService) {
    		throw new IllegalArgumentException("a userRoleCacheService is required");    		
    	}
    	if(null == singleUserQueryDao) {
    		throw new IllegalArgumentException("a singleUserQueryDao is required");    		
    	}
    	if(null == multiUserQueryDao) {
    		throw new IllegalArgumentException("a multiUserQueryDao is required");    		
    	}
    	
    	_userRoleCacheService = userRoleCacheService;
    	_singleUserQueryDao = singleUserQueryDao;
    	_multiUserQueryDao = multiUserQueryDao;
    }
	
    /**
     * Retrieves the currently logged in user from the provided AwRequest and dispatches to the appropriate DAO depending on the 
     * role of the currently logged in user. Retrieves the query results from the AwRequest, normalizes the timezone to the server
     * timezone, and calculates the hours since the most recent activity.
     */
	public void execute(AwRequest awRequest) {
		
		boolean isAdminOrResearcher = false;
		
		// Get the roles the user has for the current campaign
		// TODO - modify User to make this less convoluted?
		List<Integer> list = awRequest.getUser().getCampaignRoles().get(Integer.valueOf(awRequest.getUser().getCurrentCampaignId()));
		
		for(Integer i : list) {
			
			String role = (String) _userRoleCacheService.lookup(i);
			
			if("researcher".equals(role) || "admin".equals(role)) {
				isAdminOrResearcher = true;
				break;
			}
		}
		
		if(isAdminOrResearcher) {
			
			_multiUserQueryDao.execute(awRequest);
			
		} else {
			
			_singleUserQueryDao.execute(awRequest);
		}
		
		List<?> results = awRequest.getResultList();
		int size = results.size();
		
		
		// For each result, find the most recent upload type and calculate the hours since the last update 
		for(int i = 0; i < size; i++) {
			
			MostRecentActivityQueryResult result = (MostRecentActivityQueryResult) results.get(i);
			MobilityActivityQueryResult mobilityResult = result.getMobilityActivityQueryResult();
			PromptActivityQueryResult promptResult = result.getPromptActivityQueryResult();
			
			// Normalize the timestamps to the server timezone in case they have different timezones
			long mobilityTime = 0L;
			
			if(null != mobilityResult) {
				
				mobilityTime = mobilityResult.getMobilityTimestamp().getTime() 
					+ DateUtils.systemTimezoneOffset(mobilityResult.getMobilityTimezone()); 
			}
			
			long promptResponseTime = 0L;
			
			if(null != promptResult) {
				promptResponseTime = promptResult.getPromptTimestamp().getTime() 
					+ DateUtils.systemTimezoneOffset(promptResult.getPromptTimezone()); 
			}
						
			double difference = 0d;
			
			if(promptResponseTime == 0 && mobilityTime == 0) { // No uploads found for this particular user
				
				result.setHoursSinceLastActivity(0d);
				
			} else {
			
				if(mobilityTime > promptResponseTime) {
					
					difference = System.currentTimeMillis() - mobilityTime;
					result.setMaxFieldLabel("mobility");
					
				} else {
					
					difference = System.currentTimeMillis() - promptResponseTime;
					result.setMaxFieldLabel("prompt");
				}
				
				result.setHoursSinceLastActivity(((difference / 1000) / 60) / 60); // milliseconds to hours
			}
		}
	}
}
