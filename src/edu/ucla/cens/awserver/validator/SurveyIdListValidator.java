package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates the survey id list for a new data point query. 
 * 
 * @see PromptIdListValidator
 * @author selsky
 */
public class SurveyIdListValidator extends AbstractAnnotatingRegexpValidator {
	
	public SurveyIdListValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		if(! (awRequest instanceof NewDataPointQueryAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String surveyIdListString = ((NewDataPointQueryAwRequest) awRequest).getSurveyIdListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "empty user survey id list found");
			return false;
		
		}
		
		// first check for the special "all users" value
		if("urn:awm:special:all".equals(surveyIdListString)) {
			
			return true;
			
		} else {
			
			String[] surveyIds = surveyIdListString.split(",");
			
			if(surveyIds.length > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 survey ids in query: " + surveyIdListString);
				return false;
				
			} else {
				
				for(int i = 0; i < 10; i++) {
					if(! _regexpPattern.matcher(surveyIds[i]).matches()) {
						getAnnotator().annotate(awRequest, "incorrect survey id: " + surveyIds[i]);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
