package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
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
     
	/**
	 * Checks whether a participant user is attempting to run queries against other user's data. Assumes the logged-in user 
	 * belongs to the campaign in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("verifying the logged-in user's role and whether it has access to other user's data");
		
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
			
			// hackeroo - this should be a strategy or something like it
			if(awRequest instanceof NewDataPointQueryAwRequest) {
				// assumes that the userNameList has been validated (i.e., that there is at least one user in the list) 
				
				NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
				
				if(req.getUserList().size() != 1) {
					
					_logger.warn("logged in participant attempting to run query for another user. " 
						+ " logged in user: " +  req.getUser().getUserName() + " query user: "
						+ req.getUserListString());
					getAnnotator().annotate(req, "logged in user and query user must be the same for users with a role "  
						+ "of participant");
					
				} else {
					
					String u = req.getUserList().get(0);
					
					if(! req.getUser().getUserName().equals(u)) {
						
						_logger.warn("logged in participant attempting to run query for another user. " 
							+ " logged in user: " +  awRequest.getUser().getUserName() + " query user: "
							+ awRequest.getUserNameRequestParam());
						getAnnotator().annotate(awRequest, "logged in user and query user must be the same for users with a role "  
							+ "of participant");
					}
				}
				 
			} else {

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
}
