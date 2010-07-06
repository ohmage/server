package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class EmaQueryService implements Service {
	// private static Logger _logger = Logger.getLogger(EmaQueryService.class);
	
	private CacheService _userRoleCacheService;
	private Dao _loggedInUserDao;
	private Dao _selectedUserDao;
	
	/**
     * @throws IllegalArgumentException if the provided CacheService or Daos are null
     */
    public EmaQueryService(CacheService userRoleCacheService, Dao loggedInUserDao, Dao selectedUserDao) {
    	
    	if(null == userRoleCacheService) {
    		throw new IllegalArgumentException("a userRoleCacheService is required");    		
    	}
    	if(null == loggedInUserDao) {
    		throw new IllegalArgumentException("a loggedInUserDao is required");    		
    	}
    	if(null == selectedUserDao) {
    		throw new IllegalArgumentException("a selectedUserDao is required");    		
    	}
    	
    	_userRoleCacheService = userRoleCacheService;
    	_loggedInUserDao = loggedInUserDao;
    	_selectedUserDao = selectedUserDao;
    }
	
    /**
     * TODO document me
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
		
		if(isAdminOrResearcher && (null != awRequest.getUserNameRequestParam())) { // admins or researchers can look up EMA stats
			                                                                       // for any user in their campaign. they can also 
			                                                                       // choose to look up their own data by not
			                                                                       // passing a user name.
			_selectedUserDao.execute(awRequest);
			
		} else { // participants can only lookup their own data
			
			_loggedInUserDao.execute(awRequest);
		}
	}
}
