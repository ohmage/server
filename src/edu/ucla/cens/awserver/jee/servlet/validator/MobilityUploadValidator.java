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
		String d = (String) httpServletRequest.getParameter("d");
		
		// Check for abnormal lengths (buffer overflow attack)
		// The max lengths are based on the column widths in the db
		
		if(greaterThanLength("user", "u", u, 15)
		  || greaterThanLength("client", "ci", ci, 250) 
		  || greaterThanLength("password", "p", p, 100)
		  || greaterThanLength("mobility data message", "d", d, 65535)) {
			_logger.warn("rejecting upload because parameter payload is too large");
			return false;
		}
		
		httpServletRequest.setAttribute("validatedParameterMap", parameterMap);
		
		return true;	
	}
}
 