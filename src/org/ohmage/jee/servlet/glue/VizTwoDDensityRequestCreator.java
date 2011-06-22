package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VizScatterPlotRequest;
import org.ohmage.util.CookieUtils;

/**
 * Builds a 2D density visualization request.
 * 
 * @author John Jenkins
 */
public class VizTwoDDensityRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(VizTwoDDensityRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public VizTwoDDensityRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Builds the request and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating 2D density visualization request.");
		
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		
		return new VizScatterPlotRequest(token, 
				httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH), 
				httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
				httpRequest.getParameter(InputKeys.PROMPT_ID),
				httpRequest.getParameter(InputKeys.PROMPT2_ID));
	}
}