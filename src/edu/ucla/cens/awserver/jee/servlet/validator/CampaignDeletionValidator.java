package edu.ucla.cens.awserver.jee.servlet.validator;

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
	
	/**
	 * Basic constructor.
	 */
	public CampaignDeletionValidator() {
		// Do nothing.
	}
	
	/**
	 * Basic HTTP validation that all the required parameters exist and aren't
	 * rediculously sized.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
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
