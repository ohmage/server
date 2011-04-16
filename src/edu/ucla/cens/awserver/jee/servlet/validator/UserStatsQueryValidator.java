package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the user stats API.
 * 
 * @author selsky
 */
public class UserStatsQueryValidator extends AbstractGzipHttpServletRequestValidator {
 	private static Logger _logger = Logger.getLogger(UserStatsQueryValidator.class);
	private List<String> _parameterList;
	
	/**
	 */
	public UserStatsQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"user","campaign_urn","client","auth_token"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String campaignUrn = (String) httpServletRequest.getParameter("campaign_urn");
		String client = (String) httpServletRequest.getParameter("client");
		String authToken = (String) httpServletRequest.getParameter("auth_token");
		String user = (String) httpServletRequest.getParameter("user");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("campaignUrn", "campaign_urn", campaignUrn, 250)
		   || greaterThanLength("client", "client",client, 500)		   
		   || greaterThanLength("authToken", "auth_token", authToken, 36)
		   || greaterThanLength("userName", "user", user, 15)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
