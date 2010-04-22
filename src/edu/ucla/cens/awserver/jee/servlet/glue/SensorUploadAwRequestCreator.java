package edu.ucla.cens.awserver.jee.servlet.glue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SensorUploadAwRequest;

/**
 * Transformer for creating an AwRequest for the upload feature.
 * 
 * @author selsky
 */
public class SensorUploadAwRequestCreator implements AwRequestCreator {
//	private static Logger _logger = Logger.getLogger(SensorUploadAwRequestCreator.class);
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public SensorUploadAwRequestCreator() {
		
	}
	
	/**
	 *  Pulls the u (userName), t (type), phv (phone version), prv (protocol version), and d (json data) parameters out of the 
	 *  HttpServletRequest and places them in a new AwRequest. Also places the subdomain from the request URL into the AwRequest.
	 *  Validation of the data is performed within a controller.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		String sessionId = request.getSession(false).getId(); // for upload logging to connect app logs to uploads
		
		String userName = request.getParameter("u");
		String campaignId = request.getParameter("c");
		String password = request.getParameter("p");
		String requestType = request.getParameter("t");
		String phoneVersion = request.getParameter("phv");
		String protocolVersion = request.getParameter("prv");
		String jsonData = null; 
		try {
			
			String jd = request.getParameter("d");
			
			if(null != jd) {
				jsonData = URLDecoder.decode(jd, "UTF-8");
			}
			
		} catch(UnsupportedEncodingException uee) { // if UTF-8 is not recognized we have big problems
			
			throw new IllegalStateException(uee);
		}
		
		UserImpl user = new UserImpl();
		user.setUserName(userName);
		user.setPassword(password);
		user.setCurrentCampaignId(campaignId);
		
		AwRequest awRequest = new SensorUploadAwRequest();

		awRequest.setStartTime(System.currentTimeMillis());
		awRequest.setSessionId(sessionId);
		awRequest.setUser(user);
		awRequest.setRequestType(requestType);
		awRequest.setPhoneVersion(phoneVersion);
		awRequest.setProtocolVersion(protocolVersion);
		awRequest.setJsonDataAsString(jsonData);
				
		String requestUrl = request.getRequestURL().toString();
		if(null != request.getQueryString()) {
			requestUrl += "?" + request.getQueryString(); 
		}
		
		awRequest.setRequestUrl(requestUrl); // output in reponse in case of error
		
		return awRequest;
	}
}

