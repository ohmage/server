package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MobilityQueryAwRequest;

/**
 * Builds an AwRequest for the mobility data point API feature.
 * 
 * @author selsky
 */
public class MobilityQueryAwRequestCreator implements AwRequestCreator {
	
	public MobilityQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest request) {

		String date = request.getParameter("date");
		String userNameRequestParam = request.getParameter("user");
		String client = request.getParameter("client");
		String authToken = request.getParameter("auth_token");
		  
		
		MobilityQueryAwRequest awRequest = new MobilityQueryAwRequest();
		awRequest.setStartDate(date);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}
