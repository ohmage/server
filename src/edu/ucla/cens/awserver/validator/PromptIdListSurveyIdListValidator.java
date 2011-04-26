package edu.ucla.cens.awserver.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		return checkList(awRequest, surveyIdListString == null ? promptIdListString : surveyIdListString, surveyIdListString == null);
		
	}
	
	private boolean checkList(AwRequest awRequest, String listAsString, boolean isPrompt) {
		// first check for the special value for retrieving all items
		if("urn:ohmage:special:all".equals(listAsString)) {
			
			return true;
			
		} else {
			
			List<String> ids = Arrays.asList(listAsString.split(","));
			
			if(ids.size() > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 ids in query: " + listAsString);
				return false;
			
			} else {
				
				Set<String> idSet = new HashSet<String>(ids);
				
				if(idSet.size() != ids.size()) {
					
					getAnnotator().annotate(awRequest, "found duplicate id in list: " + ids);
					return false;
					
				}
				
				for(String id : ids) {
					if(! _regexpPattern.matcher(id).matches()) {
						getAnnotator().annotate(awRequest, "malformed id: " + id);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
