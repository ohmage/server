package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.SurveyResponseUpdateAwRequest;

/**
 * Creates an internal request for updating surveys.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseUpdateAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(SurveyResponseUpdateAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseUpdateAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a request object based on the parameters from the HTTP request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating survey response update request.");
		
		SurveyResponseUpdateAwRequest internalRequest = 
			new SurveyResponseUpdateAwRequest(request.getParameter(InputKeys.CAMPAIGN_URN),
					                          request.getParameter(InputKeys.SURVEY_KEY),
					                          request.getParameter(InputKeys.PRIVACY_STATE));
		
		internalRequest.setUserToken(request.getParameter(InputKeys.AUTH_TOKEN));
		internalRequest.setCampaignUrn(request.getParameter(InputKeys.CAMPAIGN_URN));
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT)); // push the client string into the Log4J NDC for the currently  
                                                                      // executing thread _ this means that it will be in every log
                                                                      // message for the current thread		
		
		return internalRequest;
	}
}
