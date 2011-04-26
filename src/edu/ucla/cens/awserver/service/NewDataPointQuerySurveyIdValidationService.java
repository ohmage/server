package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
 */
public class NewDataPointQuerySurveyIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(NewDataPointQuerySurveyIdValidationService.class);
	
	public NewDataPointQuerySurveyIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
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
