package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		_logger.info("converting string parameters to lists");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest; 
		
		String surveyIdListString = req.getSurveyIdListString();
		String promptIdListString = req	.getPromptIdListString();
		String columnListString = req.getColumnListString();
		String userListString = req.getUserListString();
		
		if(null == surveyIdListString) {
			
			String[] splitList = split(promptIdListString);
			List<String> ids = new ArrayList<String>();
			
			for(String entry : splitList) {
				ids.add(entry.substring(0, "urn:sys:upload:data:prompt:id:".length()));
			}
			
			req.setPromptIdList(ids);
			
		} else {
			
			String[] splitList = split(promptIdListString);
			List<String> ids = new ArrayList<String>();
			
			for(String entry : splitList) {
				ids.add(entry.substring(0, "urn:sys:upload:data:survey:id:".length()));
			}
			
			req.setSurveyIdList(ids);
		}
		
		req.setColumnList(Arrays.asList(split(columnListString)));
		req.setUserList(Arrays.asList(split(userListString)));
	}

	private String[] split(String string) {
		return string.split(",");
	}
}
