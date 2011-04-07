package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.domain.DataPointFunctionQueryMetadata;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;

/**
 * Builds an AwRequest for the data point function query API feature. This class is identical to DataPointQueryAwRequestCreator
 * except that it only allows one "i" parameter value.
 * 
 * @author selsky
 */
public class DataPointFunctionQueryAwRequestCreator implements AwRequestCreator {
	private DataPointFunctionQueryMetadata _metadata;
	
	public DataPointFunctionQueryAwRequestCreator(DataPointFunctionQueryMetadata metadata) {
		if(null == metadata || metadata.isEmpty()) {
			throw new IllegalArgumentException("metadata cannot be null or empty");
		}
		_metadata = metadata;
	}
	
	public AwRequest createFrom(HttpServletRequest request) {
		String startDate = request.getParameter("s");
		String endDate = request.getParameter("e");
		String userNameRequestParam = request.getParameter("u");
		String client = request.getParameter("ci");
		String campaignUrn = request.getParameter("c");
		String authToken = request.getParameter("t");
		String functionName = request.getParameter("i");  
		
		DataPointFunctionQueryAwRequest awRequest = new DataPointFunctionQueryAwRequest();
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setFunctionName(functionName);
		awRequest.setMetadata(_metadata);
		
        NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}
