package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;

/** 
 * @author selsky
 */
public class MediaUploadAwRequestCreator implements AwRequestCreator {
//	private static Logger _logger = Logger.getLogger(MediaUploadAwRequestCreator.class);
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public MediaUploadAwRequestCreator() {
		
	}
	
	/**
	 * For media upload, the AwRequest object is created during the initial validation in order to avoid multiple parses of the 
	 * inbound data out of the HttpServletRequest. Here the AwRequest is simply retrieved out of the HttpServletRequest.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		AwRequest awRequest = (AwRequest) request.getAttribute("awRequest");
		
		if(null == awRequest) {
			throw new IllegalStateException("missing AwRequest in HttpServletRequest - did the validation process not run?");
		}
		
		String sessionId = request.getSession(false).getId(); // for upload logging to connect app logs to upload logs
		String client = awRequest.getClient();
		
		awRequest.setSessionId(sessionId);
		
		String requestUrl = request.getRequestURL().toString();
		if(null != request.getQueryString()) {
			requestUrl += "?" + request.getQueryString(); 
		}
		
		awRequest.setRequestUrl(requestUrl); // placed in the request for use in logging messages
		
		NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that it 
                                  // will be in every log message for the thread
		
		return awRequest;
	}
}

