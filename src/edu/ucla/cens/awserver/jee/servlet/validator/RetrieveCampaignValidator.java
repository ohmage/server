package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author selsky
 */
public class RetrieveCampaignValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(RetrieveCampaignValidator.class);
	private List<String> _parameterList;
	
	public RetrieveCampaignValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"auth_token",
		 		                                                          "client",
		 		                                                          "output_format", 
		 		                                                          "campaign_urn_list",
		 		                                                          "start_date",
		 		                                                          "end_date",
		 		                                                          "privacy_state",
		 		                                                          "running_state",
		 		                                                          "user_role",
		 		                                                          "class_urn_list"}
		));
	}
	
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		Map<String, String[]> parameterMap = getParameterMap(httpServletRequest);
		
		// check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		// check for parameters with duplicate values
		if(containsDuplicateParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		// auth_token, client, and output_format are required. all of the other params are optional
		String authToken = httpServletRequest.getParameter("auth_token");
		if(null == authToken) {
			_logger.info("missing auth_token parameter in request");
			return false;
		}
		String client = httpServletRequest.getParameter("client");
		if(null == client) {
			_logger.info("missing client parameter in request");
			return false;
		}
		String outputFormat = httpServletRequest.getParameter("output_format");
		if(null == outputFormat) {
			_logger.info("missing output_format parameter in request");
			return false;
		}
		
		// perform sanity check on the optional params anyway
		String campaignUrnList = httpServletRequest.getParameter("campaign_urn_list");
		String startDate = httpServletRequest.getParameter("start_date");
		String endDate = httpServletRequest.getParameter("end_date");
		String privacyState = httpServletRequest.getParameter("privacy_state");
		String runningState = httpServletRequest.getParameter("running_state");
		String userRole = httpServletRequest.getParameter("user_role");
		String classUrnList = httpServletRequest.getParameter("class_urn_list");
 		
		if(greaterThanLength("auth token", "auth_token", authToken, 36)
		   || greaterThanLength("client", "client", client, 250)
		   || greaterThanLength("output format", "output_format", outputFormat, 6)
		   || greaterThanLength("campaign URN list", "cammpaign_urn_list", campaignUrnList, 2550) // max of 10 URNs (our db column 
		                                                                                          // restriction is 255 chars)
		   || greaterThanLength("start date", "start_date", startDate, 10)
		   || greaterThanLength("end date", "end_date", endDate, 10)
		   || greaterThanLength("privacy state", "privacy_state", privacyState, 7)
		   || greaterThanLength("running state", "running_state", runningState, 7)
		   || greaterThanLength("user role", "user_role", userRole, 11)
		   || greaterThanLength("class URN list", "class_urn_list", classUrnList, 2550) // max of 10 URNs as above for cmapaignUrnList
		) {
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
