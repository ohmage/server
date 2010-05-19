package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.MostRecentSurveyActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.DateUtils;

/**
 * Service that dispatches directly to a DAO without performing any pre- or post-processing.
 * 
 * @author selsky
 */
public class MostRecentSurveyQueryService implements Service {
	private CacheService _userRoleCacheService;
	private Dao _singleUserQueryDao;
	private Dao _multiUserQueryDao;
	
	/**
     * Creates and instance of this class and passes dao to the super class constructor.
     * 
     *   @throws IllegalArgumentException if the provided CacheService is null
     *   @throws IllegalArgumentException if either of the provided Daos are null
     */
    public MostRecentSurveyQueryService(CacheService userRoleCacheService, Dao singleUserQueryDao, Dao multiUserQueryDao) {
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
     * TODO document me
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
		
		// Calculate the hours since the last update
		
		List<?> results = awRequest.getResultList();
		int size = results.size();
		
		for(int i = 0; i < size; i++) {
			
			MostRecentSurveyActivityQueryResult result = (MostRecentSurveyActivityQueryResult) results.get(i);
			
			if(result.getTimestamp() != null && result.getTimezone() != null) {
				long updateTime = result.getTimestamp().getTime() + DateUtils.systemTimezoneOffset(result.getTimezone());
				
				// Dates before the epoch will break this calculation 
				double difference = System.currentTimeMillis() - updateTime;
				
				// convert to hours
				result.setValue(((difference / 1000) / 60) / 60);
				
			} else {
				
				result.setValue(0d);
			}
		}
	}
}
