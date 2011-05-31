package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

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
		_parameterList = new ArrayList<String>(Arrays.asList(new String[] { InputKeys.AUTH_TOKEN, InputKeys.CAMPAIGN_URN }));
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
		
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String urn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(greaterThanLength("authToken", InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength("campaignUrn", InputKeys.CAMPAIGN_URN, urn, 255)) {
			_logger.warn(InputKeys.CAMPAIGN_URN + " is too long.");
			return false;
		}
		else if(client == null) {
			_logger.warn("Missing " + InputKeys.CLIENT);
			return false;
		}
		
		return true;
	}

}
