package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.UserDate;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Service that dispatches directly to a DAO without performing any pre- or post-processing.
 * 
 * @author selsky
 */
public class PromptGroupCountQueryService implements Service {
	private CacheService _campaignPromptGroupItemCountCacheService;
	private CacheService _userRoleCacheService;
	private Dao _singleUserDao;
	private Dao _multiUserDao;
	
	/**
     * Creates and instance of this class and passes dao to the super class constructor.
     * 
     *  @throws IllegalArgumentException if the provided CacheService is null
     */
    public PromptGroupCountQueryService(CacheService userRoleCacheService, /*CacheService campaignPromptGroupItemCountCacheService,*/
    	Dao singleUserDao, Dao multiUserDao) {
    	
    	if(null == userRoleCacheService) {
    		throw new IllegalArgumentException("a userRoleCacheService is required");    		
    	}
//    	if(null == campaignPromptGroupItemCountCacheService) {
//    		throw new IllegalArgumentException("a campaignPromptCountCacheService is required");    		
//    	}
    	if(null == singleUserDao) {
    		throw new IllegalArgumentException("a singleUserDao is required");    		
    	}
    	if(null == multiUserDao) {
    		throw new IllegalArgumentException("a multiUserDao is required");    		
    	}
    	
    	_userRoleCacheService = userRoleCacheService;
//    	_campaignPromptGroupItemCountCacheService = campaignPromptGroupItemCountCacheService;
    	
    	_singleUserDao = singleUserDao;
    	_multiUserDao = multiUserDao;
    }
	
    /**
     * TODO document me, factor out code that is similar to other classes that perform queries for either single or multiple users
     */
	public void execute(AwRequest awRequest) {
		
		// -- start duplicate logic with SuccessfulLocationUpdatesQueryService
		
		boolean isAdminOrResearcher = false;
		
		List<Integer> list = awRequest.getUser().getCampaignRoles().get(Integer.valueOf(awRequest.getUser().getCurrentCampaignId()));
		
		for(Integer i : list) {
			
			String role = (String) _userRoleCacheService.lookup(i);
			
			if("researcher".equals(role) || "admin".equals(role)) {
				isAdminOrResearcher = true;
				break;
			}
		}
		
		if(isAdminOrResearcher) {
			
			_multiUserDao.execute(awRequest);
			
		} else {
			
			_singleUserDao.execute(awRequest);
		}
		
		// -- end duplicate logic with SuccessfulLocationUpdatesQueryService
		
		//TODO - add count calculation..

	}
}
