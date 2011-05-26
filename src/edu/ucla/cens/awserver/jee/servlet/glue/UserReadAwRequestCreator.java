package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.UserReadRequest;

/**
 * Creates a user read request.
 * 
 * @author John Jenkins
 */
public class UserReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(UserReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public UserReadAwRequestCreator() {
		super();
	}

	/**
	 * Creates a request object for processing a user read request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating request for reading user information.");
		
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String campaignUrnList = httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST);
		String classUrnList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		
		UserReadRequest request = new UserReadRequest(campaignUrnList, classUrnList);
		request.setUserToken(authToken);
		
		return request;
	}
}