package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.ConfigurationCacheService;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
 */
public class NewDataPointQueryPromptIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryPromptIdValidationService.class);
	private ConfigurationCacheService _configurationCacheService;
	
	public NewDataPointQueryPromptIdValidationService(AwRequestAnnotator annotator, ConfigurationCacheService configurationCacheService) {
		super(annotator);
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a ConfigurationCacheService is required");
		}
		_configurationCacheService = configurationCacheService;
	}
	
	/**
	 * Checks the prompt ids from the query (if any prompt ids exist) to make sure that they belong to the campaign name-version
	 * in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("validating prompt ids against a campaign config");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		if((null != req.getPromptIdListString()) && (0 != req.getPromptIdListArray().length)) {
			
			if(! "urn:sys:special:all".equals(req.getPromptIdListString())) {
				
				String[] promptIds = req.getPromptIdListArray(); 
				String campaignName = req.getCampaignName();
				String campaignVersion = req.getCampaignVersion();
				
				Configuration config = 
					(Configuration) _configurationCacheService.lookup(new CampaignNameVersion(campaignName, campaignVersion));
				
				for(String promptId : promptIds) {
					
					if(null == config.getSurveyIdForPromptId(promptId)) {
						
						getAnnotator().annotate(awRequest, "prompt id " + promptId + " does not exist for campaign");
						return;
						
					}
				}
			}
		}
	}
}
