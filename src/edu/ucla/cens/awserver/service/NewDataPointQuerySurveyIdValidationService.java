package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.ConfigurationCacheService;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
 */
public class NewDataPointQuerySurveyIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(NewDataPointQuerySurveyIdValidationService.class);
	private ConfigurationCacheService _configurationCacheService;
	
	public NewDataPointQuerySurveyIdValidationService(AwRequestAnnotator annotator, ConfigurationCacheService configurationCacheService) {
		super(annotator);
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a ConfigurationCacheService is required");
		}
		_configurationCacheService = configurationCacheService;
	}
	
	/**
	 * Checks the survey ids from the query (if any survey ids exist) to make sure that they belong to the campaign name-version
	 * in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("validating survey ids against a campaign config");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		if((null != req.getSurveyIdListString()) && (0 != req.getSurveyIdList().size())) {
			
			if(! "urn:awm:special:all".equals(req.getSurveyIdListString())) {
				
				List<String> surveyIds = req.getSurveyIdList(); 
				
				Configuration config = (Configuration) _configurationCacheService.lookup(req.getCampaignUrn());
				
				for(String surveyId : surveyIds) {
					
					if(! config.surveyIdExists(surveyId)) {
						
						getAnnotator().annotate(awRequest, "survey " + surveyId + " does not exist");
						return;
						
					}
				}
			}
		}
	}
}
