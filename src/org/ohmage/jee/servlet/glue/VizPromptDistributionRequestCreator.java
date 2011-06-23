package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VizPromptDistributionRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a prompt distribution visualization request.
 * 
 * @author John Jenkins
 */
public class VizPromptDistributionRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(VizPromptDistributionRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public VizPromptDistributionRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Builds the request and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating user timeseries visualization request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return new VizPromptDistributionRequest(token, 
				httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH), 
				httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
				httpRequest.getParameter(InputKeys.PROMPT_ID));
	}
}