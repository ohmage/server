package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MobilityQueryAwRequest;

/**
 * Builds an AwRequest for the chunked mobility data point API feature.
 * 
 * @author selsky
 */
public class ChunkedMobilityQueryAwRequestCreator implements AwRequestCreator {
	
	public ChunkedMobilityQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest request) {

		String startDate = request.getParameter("s");
		String endDate = request.getParameter("e");
		String userNameRequestParam = request.getParameter("u");
		String client = request.getParameter("ci");
		String authToken = request.getParameter("t");
		  
		
		MobilityQueryAwRequest awRequest = new MobilityQueryAwRequest();
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		
        NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}
