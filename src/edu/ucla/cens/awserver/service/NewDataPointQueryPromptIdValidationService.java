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
public class NewDataPointQueryPromptIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryPromptIdValidationService.class);
	
	public NewDataPointQueryPromptIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks the prompt ids from the query (if any prompt ids exist) to make sure that they belong to the campaign name-version
	 * in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("validating prompt ids against a campaign config");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		if((null != req.getPromptIdListString()) && (0 != req.getPromptIdList().size())) {
			
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				
				List<String> promptIds = req.getPromptIdList();
				
				Configuration config = req.getConfiguration();
				
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
