package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;

/**
 * Builds an AwRequest for the user stats API feature.
 * 
 * @author selsky
 */
public class UserStatsQueryAwRequestCreator implements AwRequestCreator {
	
	public UserStatsQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		String userNameRequestParam = request.getParameter("user");
		String client = request.getParameter("client");
		String campaignName = request.getParameter("campaign_urn");
		String authToken = request.getParameter("auth_token");
		
		UserStatsQueryAwRequest awRequest = new UserStatsQueryAwRequest();
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignName);
		
		return awRequest;
	}
}
