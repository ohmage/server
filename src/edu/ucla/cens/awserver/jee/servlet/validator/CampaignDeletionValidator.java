package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Checks that all required parameters exist and that they aren't oversized.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignDeletionValidator.class);
	private List<String> _parameterList;
	
	/**
	 * Basic constructor.
	 */
	public CampaignDeletionValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[] { "auth_token", "campaign_urn" }));
	}
	
	/**
	 * Basic HTTP validation that all the required parameters exist and aren't
	 * rediculously sized.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		if(! basicValidation(getParameterMap(httpRequest), _parameterList)) {
			return false;
		}
		
		String token = httpRequest.getParameter("auth_token");
		String urn = httpRequest.getParameter("campaign_urn");
		
		if(greaterThanLength("authToken", "auth_token", token, 36)) {
			_logger.warn("auth_token is too long.");
			return false;
		}
		else if(greaterThanLength("campaignUrn", "campaign_urn", urn, 255)) {
			_logger.warn("campaign_urn is too long.");
			return false;
		}
		
		return true;
	}

}
