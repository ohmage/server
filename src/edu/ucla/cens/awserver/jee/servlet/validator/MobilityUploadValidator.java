package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author selsky
 */
public class MobilityUploadValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(MobilityUploadValidator.class);
	private List<String> _parameterList;
	
	public MobilityUploadValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"u","ci","d","p"}));
	}
	
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		Map<String, String[]> parameterMap = requestToMap(httpServletRequest);
		
		if(! basicValidation(parameterMap, _parameterList)) {
			return false;
		}
		
		// Tomcat will URL Decode the parameters 
		
		String u = (String) httpServletRequest.getParameter("u"); 
		String p = (String) httpServletRequest.getParameter("p");
		String ci = (String) httpServletRequest.getParameter("ci");
		
		// Check for abnormal lengths (buffer overflow attack)
		// The max lengths are based on the column widths in the db
		
		if(greaterThanLength("user", "u", u, 15)
		   || greaterThanLength("client", "ci", ci, 250)
		   || greaterThanLength("password", "p", p, 100)
		) {
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		// The JSON data is not checked because its length is so variable and potentially huge (some messages are 700000+ characters
		// when URL-encoded). It will be heavily validated once inside the main application validation layer.
		
		// The default setting for Tomcat is to disallow requests that are greater than 2MB
		
		httpServletRequest.setAttribute("validatedParameterMap", parameterMap);
		
		return true;	
	}
}
 