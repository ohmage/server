package edu.ucla.cens.awserver.jee.servlet.writer;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import edu.ucla.cens.awserver.domain.PromptContext;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public interface NewDataPointQueryOutputBuilder {
	
	String createMultiResultOutput(int totalNumberOfResults, NewDataPointQueryAwRequest req, 
			                       Map<String, PromptContext> promptContextMap, Map<String, List<Object>> columnMap)
								   throws JSONException;
	
	String createZeroResultOutput(NewDataPointQueryAwRequest req, Map<String, List<Object>> columnMap) throws JSONException;

}
