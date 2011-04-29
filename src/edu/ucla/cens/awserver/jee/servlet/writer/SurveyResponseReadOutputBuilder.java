package edu.ucla.cens.awserver.jee.servlet.writer;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import edu.ucla.cens.awserver.domain.PromptContext;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;

/**
 * @author selsky
 */
public interface SurveyResponseReadOutputBuilder {
	
	String createMultiResultOutput(int totalNumberOfResults, SurveyResponseReadAwRequest req, 
			                       Map<String, PromptContext> promptContextMap, Map<String, List<Object>> columnMap)
								   throws JSONException;
	
	String createZeroResultOutput(SurveyResponseReadAwRequest req, Map<String, List<Object>> columnMap) throws JSONException;

}
