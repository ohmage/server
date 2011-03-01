package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.cache.ConfigurationCacheService;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.domain.ConfigQueryResult;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Handles dispatch to DAOs and Caches to retrieve results for Configuration API queries.
 * 
 * @author selsky
 */
public class ConfigurationRetrievalService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(ConfigurationRetrievalService.class);
	private ConfigurationCacheService _configCacheService;
	private CacheService _userRoleCacheService;
	private Dao _findAllCampaignsForUserDao;
	private Dao _findAllUsersForCampaignDao;
	
	public ConfigurationRetrievalService(Dao findAllCampaignsForUserDao, Dao findAllUsersForCampaignDao, AwRequestAnnotator annotator, 
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
		_findAllCampaignsForUserDao.execute(awRequest); // the DAO must set a list of strings (campaign names): AwRequest.setResultList
		
		SortedMap<CampaignNameVersion, Configuration> configurationMap 
			= _configCacheService.lookupByCampaigns(awRequest.getResultList());
		
		Set<CampaignNameVersion> configurationKeys = configurationMap.keySet();
		List<ConfigQueryResult> results = new ArrayList<ConfigQueryResult>();
		
		List<String> campaignNameList = new ArrayList<String>();
		String currentCampaignName = null;
		
		// grab all of the unique campaigns from the configuration map
		for(CampaignNameVersion key : configurationKeys) {
			String name = key.getCampaignName();
			if(! name.equals(currentCampaignName)) {
				campaignNameList.add(name);
				currentCampaignName = name;
			}
		}
		
		// build the output for each campaign
		for(String campaignName : campaignNameList) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("building output for campaign: " + campaignName);
			}
			
			ConfigQueryResult result = new ConfigQueryResult();
			
			result.setCampaignName(campaignName);
			
			// determine the maximum user role for the current campaign and set it on the result
			// TODO at some point the user role system needs to change; do users actually need more than one role?
			List<Integer> list = awRequest.getUser().getCampaignRoles().get(campaignName);
			boolean isAdminOrResearcher = false;
			
			for(Integer i : list) {
				String role = (String) _userRoleCacheService.lookup(i);
				
				if("researcher".equals(role) || "admin".equals(role)) {
					isAdminOrResearcher = true;
					result.setUserRole(role);
					break;
				}
			}
			
			if(! isAdminOrResearcher) {
				result.setUserRole("participant");
			}
			
			// get all of the users for the current campaign if the current user is a researcher or admin
			
			List<String> userList = new ArrayList<String>();
			
			if(isAdminOrResearcher) {
				awRequest.setCampaignName(campaignName);
				
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
			
			result.setUserList(userList);
			
			// now get all of the configurations for the campaign
			Map<String, String> versionXmlMap = new HashMap<String, String>();
			
			for(CampaignNameVersion key : configurationKeys) {
				if(key.getCampaignName().equals(campaignName)) {                                  // strip newlines from the XML
					versionXmlMap.put(key.getCampaignVersion(), configurationMap.get(key).getXml().replaceAll("\\n", " "));
				}
			}
			
			result.setVersionXmlMap(versionXmlMap);
			
			results.add(result);
		}
		
		awRequest.setResultList(results);
	}
}
