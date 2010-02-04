package edu.ucla.cens.awserver.jee.servlet.glue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.datatransfer.AwRequestImpl;
import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Transformer for creating an AwRequest for authentication.
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
		String subdomain = StringUtils.retrieveSubdomainFromUrlString(request.getRequestURL().toString());
		
		String userName = request.getParameter("u");
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
		
		// _logger.info(jsonData);
		
		UserImpl user = new UserImpl();
		user.setUserName(userName);
		
		AwRequestImpl awRequest = new AwRequestImpl();
		
		awRequest.setAttribute("startTime", System.currentTimeMillis());
		
		awRequest.setUser(user);
		awRequest.setAttribute("subdomain", subdomain);
		awRequest.setAttribute("requestType", requestType);
		awRequest.setAttribute("phoneVersion", phoneVersion);
		awRequest.setAttribute("protocolVersion", protocolVersion);
		awRequest.setAttribute("jsonData", jsonData);
		
		String requestUrl = request.getRequestURL().toString();
		if(null != request.getQueryString()) {
			requestUrl += "?" + request.getQueryString(); 
		}
		
		awRequest.setAttribute("requestUrl", requestUrl); // output in reponse in case of error
		
		return awRequest;
	}
}

