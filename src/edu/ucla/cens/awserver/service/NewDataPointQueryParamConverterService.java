package edu.ucla.cens.awserver.service;

import java.util.Arrays;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public class NewDataPointQueryParamConverterService implements Service {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryParamConverterService.class);
	
	/**
	 * Converts the String representations of survey id, prompt id, user and column into Lists. Expects the awRequest
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
			
			req.setPromptIdList(Arrays.asList(split(promptIdListString)));
			
		} else {
			
			req.setSurveyIdList(Arrays.asList(split(surveyIdListString)));
		}
		
		req.setColumnList(Arrays.asList(split(columnListString)));
		req.setUserList(Arrays.asList(split(userListString)));
	}

	private String[] split(String string) {
		return string.split(",");
	}
}
