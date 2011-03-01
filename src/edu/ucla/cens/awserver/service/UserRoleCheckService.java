package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service that checks that the currently logged-in user is running a query against their own data or, if the logged-in user is 
 * running queries against another user's data, checks that the logged-in user is a researcher or admin.  
 * 
 * @author selsky
 */
public class UserRoleCheckService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserRoleCheckService.class);
	private CacheService _userRoleCacheService;
	
	public UserRoleCheckService(AwRequestAnnotator annotator, CacheService userRoleCacheService) {
		super(annotator);
		
		if(null == userRoleCacheService) {
			throw new IllegalArgumentException("a user role cache service is required");
		}
		
		_userRoleCacheService = userRoleCacheService;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// check whether a non-researcher or non-admin user is attempting to run queries for other users
 
		List<Integer> list = awRequest.getUser().getCampaignRoles().get(awRequest.getCampaignName());
		boolean isAdminOrResearcher = false;
		
		for(Integer i : list) {
			String role = (String) _userRoleCacheService.lookup(i);
			
			if("researcher".equals(role) || "admin".equals(role)) {
				isAdminOrResearcher = true;
				break;
			}
		}
		
		if(! isAdminOrResearcher) { // participants can only run queries for themselves
			
			if(! awRequest.getUser().getUserName().equals(awRequest.getUserNameRequestParam())) {
				_logger.warn("logged in participant attempting to run query for another user. " 
					+ " logged in user: " +  awRequest.getUser().getUserName() + " query user: "
					+ awRequest.getUserNameRequestParam());
				getAnnotator().annotate(awRequest, "logged in user and query user must be the same for users with a role "  
					+ "of participant");
			}
		}
	}
}
