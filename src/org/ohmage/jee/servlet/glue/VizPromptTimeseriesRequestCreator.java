package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VizPromptTimeseriesRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a prompt timeseries visualization request.
 * 
 * @author John Jenkins
 */
public class VizPromptTimeseriesRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(VizPromptTimeseriesRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public VizPromptTimeseriesRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Builds the request and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating user timeseries visualization request.");
		
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		
		return new VizPromptTimeseriesRequest(token, 
				httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH), 
				httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
				httpRequest.getParameter(InputKeys.PROMPT_ID));
	}
}