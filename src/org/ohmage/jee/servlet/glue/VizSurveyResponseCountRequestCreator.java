package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VizSurveyResponseCountRequest;
import org.ohmage.util.CookieUtils;

/**
 * Builds a survey response count visualization request.
 * 
 * @author John Jenkins
 */
public class VizSurveyResponseCountRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(VizSurveyResponseCountRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public VizSurveyResponseCountRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Builds the request and returns it.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating survey response count visualization request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return new VizSurveyResponseCountRequest(token, 
				httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH), 
				httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
				httpRequest.getParameter(InputKeys.PRIVACY_STATE),
				httpRequest.getParameter(InputKeys.START_DATE),
				httpRequest.getParameter(InputKeys.END_DATE));
	}
}