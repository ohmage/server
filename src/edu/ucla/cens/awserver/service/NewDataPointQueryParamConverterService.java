package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public class NewDataPointQueryParamConverterService implements Service {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryParamConverterService.class);
	
	/**
	 * Converts the String representations of survey id, prompt id, user and column lists into String arrays. Expects the awRequest
	 * to be a NewDataPointQueryAwRequest. Assumes the values have already been validated and that the string representations are
	 * comma-separated values.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("converting string parameters to arrays");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest; 
		
		String surveyIdListString = req.getSurveyIdListString();
		String promptIdListString = req	.getPromptIdListString();
		String columnListString = req.getColumnListString();
		String userListString = req.getUserListString();
		
		if(null == surveyIdListString) {
			
			req.setPromptIdListArray(split(promptIdListString));
			
		} else {
			
			req.setSurveyIdListArray(split(surveyIdListString));
		}
		
		req.setColumnListArray(split(columnListString));
		req.setUserListArray(split(userListString));
	}

	private String[] split(String string) {
		return string.split(",");
	}
}
