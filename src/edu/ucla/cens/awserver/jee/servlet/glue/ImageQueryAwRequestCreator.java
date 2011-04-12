package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * Builds an AwRequest for the image query API feature.
 * 
 * @author selsky
 */
public class ImageQueryAwRequestCreator implements AwRequestCreator {
	
	public ImageQueryAwRequestCreator() {
		
	}
	
	public AwRequest createFrom(HttpServletRequest request) {
		
		String userNameRequestParam = request.getParameter("user");
		String client = request.getParameter("client");
		String campaignUrn = request.getParameter("campaign_urn");
		String authToken = request.getParameter("token");
		String imageId = request.getParameter("id");  
		
		MediaQueryAwRequest awRequest = new MediaQueryAwRequest();

		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setMediaId(imageId);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                      // it will be in every log message for the thread
		
		return awRequest;
	}
}
