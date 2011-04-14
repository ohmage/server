package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.cache.ConfigurationCacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.ConfigQueryResult;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Handles dispatch to DAOs and Caches to retrieve results for Configuration API queries.
 * 
 * @author selsky
 */
public class RetrieveCampaignService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(RetrieveCampaignService.class);
	private ConfigurationCacheService _configCacheService;
	private CacheService _userRoleCacheService;
	private Dao _findAllCampaignsForUserDao;
	private Dao _findAllUsersForCampaignDao;
	
	public RetrieveCampaignService(Dao findAllCampaignsForUserDao, Dao findAllUsersForCampaignDao, AwRequestAnnotator annotator, 
		ConfigurationCacheService configurationCacheService, CacheService userRoleCacheService) {
		
		super(annotator);
		
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a ConfigurationCacheService is required");
		}
		if(null == userRoleCacheService) {
			throw new IllegalArgumentException("a user role CacheService is required");
		}
		if(null == findAllCampaignsForUserDao) {
			throw new IllegalArgumentException("findAllCampaignsForUserDao cannot be null");
		}
		if(null == findAllUsersForCampaignDao) {
			throw new IllegalArgumentException("findAllUsersForCampaignDao cannot be null");
		}
		
		_configCacheService = configurationCacheService;
		_userRoleCacheService = userRoleCacheService;
		_findAllCampaignsForUserDao = findAllCampaignsForUserDao;
		_findAllUsersForCampaignDao = findAllUsersForCampaignDao;
	}
	
	/**
	 * Runs the giant configuration query: finds all configurations for the campaigns the currently logged in user belongs to; 
	 * finds the user role of the currently logged in user; finds all of the users for each campaign if the currently logged in user
	 * is an admin or researcher; creates a ConfigQueryResult for each campaign and pushes the list of results into the AwRequest.
	 * 
	 * TODO - this method does too much and should be split up into separate classes.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_findAllCampaignsForUserDao.execute(awRequest); // the DAO must set a list of strings (campaign URNs): AwRequest.setResultList
		
		SortedMap<String, Configuration> configurationMap = _configCacheService.lookupByCampaigns(awRequest.getResultList());
		
		Set<String> configurationKeys = configurationMap.keySet();
		List<ConfigQueryResult> results = new ArrayList<ConfigQueryResult>();
		
		// build the output for each campaign
		for(String urn : configurationKeys) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("building output for campaign: " + urn);
			}
			
			String role = null;
			String xml = null;
			List<String> userList = new ArrayList<String>();
			
			// determine the maximum user role for the current campaign and set it on the result
			// TODO drop the "maximum" restriction because users can have many roles and the roles provide access to non-overlapping
			// functionality in the system
			List<Integer> list = awRequest.getUser().getCampaignRoles().get(urn);
			boolean isAdminOrResearcher = false;
			
			for(Integer i : list) {
				role = (String) _userRoleCacheService.lookup(i);
				
				if("supervisor".equals(role)) {
					isAdminOrResearcher = true;
					break;
				}
			}
			
			if(! isAdminOrResearcher) {
				role = "participant";
			}
			
			// get all of the users for the current campaign if the current user is a researcher or admin
			
			if(isAdminOrResearcher) {
				awRequest.setCampaignUrn(urn);
				
				_findAllUsersForCampaignDao.execute(awRequest);
				
				List<?> users = awRequest.getResultList();
				int size = users.size();
				
				if(0 == size) { // logical error! There must be at least one user found  (the current user from the AwRequest)
					throw new ServiceException("no users found for campaign");
				}
				
				for(int i = 0; i < size; i++) {
					userList.add((String) users.get(i));
				}
				
			} else {
				
				userList.add(awRequest.getUser().getUserName());
			}
			
			xml = configurationMap.get(urn).getXml().replaceAll("\\n", " ");
			
			results.add(new ConfigQueryResult(urn, role, userList, xml));
		}
		
		awRequest.setResultList(results);
	}
}
