package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.RetrieveConfigAwRequest;

/**
 * @author selsky
 */
public class RetrieveConfigAwRequestCreator implements AwRequestCreator {

	public RetrieveConfigAwRequestCreator() {
		
	}
	
	/**
	 * Pushes the phone version into the Log4J NDC and creates an AwRequest with the login information and phone version.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		String campaignName = request.getParameter("c");
		String client = request.getParameter("ci");
		String userToken = request.getParameter("t");
		 
		NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that it
		                          // will be in every log message for the thread
		
		AwRequest awRequest = new RetrieveConfigAwRequest();
		awRequest.setCampaignUrn(campaignName);
		awRequest.setClient(client);
		awRequest.setUserToken(userToken);
				
		return awRequest;
	}
}
