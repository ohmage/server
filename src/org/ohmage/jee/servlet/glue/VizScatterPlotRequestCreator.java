package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VizScatterPlotRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a scatter plot visualization request.
 * 
 * @author John Jenkins
 */
public class VizScatterPlotRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(VizScatterPlotRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public VizScatterPlotRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Builds the request and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating scatter plot visualization request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return new VizScatterPlotRequest(token, 
				httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH), 
				httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
				httpRequest.getParameter(InputKeys.PROMPT_ID),
				httpRequest.getParameter(InputKeys.PROMPT2_ID));
	}
}