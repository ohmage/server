package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author Joshua Selsky
 */
public class SurveyResponseReadSurveyIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadSurveyIdValidationService.class);
	
	public SurveyResponseReadSurveyIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating survey ids against a campaign config");
		
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		
		if((null != req.getSurveyIdListString()) && (0 != req.getSurveyIdList().size())) {
			
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				
				List<String> surveyIds = req.getSurveyIdList(); 
				
				Configuration config = req.getConfiguration();
				
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
