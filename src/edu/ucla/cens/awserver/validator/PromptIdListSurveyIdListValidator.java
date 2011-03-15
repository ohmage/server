package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the prompt id list for a new data point query.
 * 
 * @author selsky
 */
public class PromptIdListSurveyIdListValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(PromptIdListSurveyIdListValidator.class);
	
	public PromptIdListSurveyIdListValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("validating prompt id list or survey id list");
		
		if(! (awRequest instanceof NewDataPointQueryAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String promptIdListString = ((NewDataPointQueryAwRequest) awRequest).getPromptIdListString();
		String surveyIdListString = ((NewDataPointQueryAwRequest) awRequest).getSurveyIdListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptIdListString) && StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "empty prompt id list and empty survey id list found");
			return false;	
			
		} else if (! StringUtils.isEmptyOrWhitespaceOnly(promptIdListString) 
						&& ! StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "both prompt id list and survey id list found - only one is allowed");
			return false;
			
		}
		
		return checkList(awRequest, surveyIdListString == null ? promptIdListString : surveyIdListString);
		
	}
	
	private boolean checkList(AwRequest awRequest, String listAsString) {
		// first check for the special value for retrieving all items
		if("urn:sys:special:all".equals(listAsString)) {
			
			return true;
			
		} else {
			
			String[] ids = listAsString.split(",");
			
			if(ids.length > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 ids in query: " + listAsString);
				return false;
				
			} else {
				
				for(int i = 0; i < ids.length; i++) {
					if(! _regexpPattern.matcher(ids[i]).matches()) {
						getAnnotator().annotate(awRequest, "malformed id: " + ids[i]);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
