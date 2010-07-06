package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class MobilityModeCountQueryService implements Service {
	// private static Logger _logger = Logger.getLogger(MobilityModeCountQueryService.class);
	
	private CacheService _userRoleCacheService;
	private Dao _singleUserDao;
	private Dao _multiUserDao;
	
	/**
     *  @throws IllegalArgumentException if the provided CacheServices or Daos are null
     */
    public MobilityModeCountQueryService(CacheService userRoleCacheService, Dao singleUserDao, Dao multiUserDao) {
    	
    	if(null == userRoleCacheService) {
    		throw new IllegalArgumentException("a userRoleCacheService is required");    		
    	}    	
    	if(null == singleUserDao) {
    		throw new IllegalArgumentException("a singleUserDao is required");    		
    	}
    	if(null == multiUserDao) {
    		throw new IllegalArgumentException("a multiUserDao is required");    		
    	}
    	
    	_userRoleCacheService = userRoleCacheService;
    	
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
	}
}
