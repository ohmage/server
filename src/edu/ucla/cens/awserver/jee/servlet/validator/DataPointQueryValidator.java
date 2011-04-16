package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the data point API.
 * 
 * @author selsky
 */
public class DataPointQueryValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DataPointQueryValidator.class);
	private List<String> _parameterList;
	
	/**
	 */
	public DataPointQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"start_date","end_date","user","campaign_urn","client","prompt_id","auth_token"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		Map<String,String[]> parameterMap = getParameterMap(httpServletRequest); 
		
		// Check for missing or extra parameters
		if(parameterMap.size() != _parameterList.size()) {				
			_logger.warn("an incorrect number of parameters was found for an data point query: " + parameterMap.size());
			return false;
		}
		
		// Check for duplicate parameter values (except for "i")
		Iterator<?> iterator = parameterMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String[] valuesForKey = (String[]) parameterMap.get(key);
			
			if(valuesForKey.length != 1 && ! "prompt_id".equals(key)) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return false;
			}
		}
		
		// Check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		String startDate = (String) httpServletRequest.getParameter("start_date");
		String endDate = (String) httpServletRequest.getParameter("end_date");
		String user = (String) httpServletRequest.getParameter("user");
		String campaignUrn = (String) httpServletRequest.getParameter("campaign_urn");
		String client = (String) httpServletRequest.getParameter("client");
		String authToken = (String) httpServletRequest.getParameter("auth_token");
		
		String[] promptIdArray = httpServletRequest.getParameterValues("prompt_id");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("startDate", "start_date", startDate, 10) 
		   || greaterThanLength("endDate", "end_date", endDate, 10)
		   || greaterThanLength("campaignUrn", "campaign_urn", campaignUrn, 250)
		   || greaterThanLength("client", "client",client, 250)		   
		   || greaterThanLength("authToken", "auth_token", authToken, 36)
		   || greaterThanLength("userName", "user", user, 15)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		int x = 0;
		for(String promptId : promptIdArray) { 
			if(greaterThanLength("dataPointId", "prompt_id[" + x + "]", promptId, 250)) {
				_logger.warn("found an input parameter that exceeds its allowed length");
				return false;
			}
			x++;
		}
		
		return true;
	}
}
