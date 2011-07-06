/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.validator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;


/**
 * Validator for inbound data to the "new" data point API.
 * 
 * @author selsky
 */
public class SurveyResponseReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadValidator.class);
	
	public SurveyResponseReadValidator() {
		// Do nothing.
	}
	
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		Map<String,String[]> parameterMap = getParameterMap(httpRequest); 

// required parameters
//		(r) auth_token
//		(r) campaign_urn
//		(r) client
//		(r) column_list
//		(r) output_format		
//		(r) user_list
		
// optional parameters		
//		(o) end_date
//		(o) pretty_print
//		(o) prompt_id_list
//		(o) privacy_state
//		(o) return_id
//		(o) sort_order
//		(o) start_date
//		(o) suppress_metadata
//		(o) survey_id_list
//		(o) collapse
		
		String campaignUrn = (String) httpRequest.getParameter("campaign_urn");
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignUrn)) {
			_logger.warn("missing campaign_urn parameter");
			return false;
		}
		
		String client = (String) httpRequest.getParameter("client");
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			_logger.warn("missing client parameter");
			return false;
		}
		
		String columnList = (String) httpRequest.getParameter("column_list");
		if(StringUtils.isEmptyOrWhitespaceOnly(columnList)) {
			_logger.warn("missing column_list parameter");
			return false;
		}
		
		String users = (String) httpRequest.getParameter("user_list");
		if(StringUtils.isEmptyOrWhitespaceOnly(users)) {
			_logger.warn("missing users parameter");
			return false;
		}
		
		String outputFormat = (String) httpRequest.getParameter("output_format");
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			_logger.warn("missing output_format parameter");
			return false;
		}
		
		// Check for duplicate values for any keys
		Iterator<?> iterator = parameterMap.keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String[] valuesForKey = (String[]) parameterMap.get(key);
			
			if(valuesForKey.length != 1) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return false;
			}
		}

		String startDate = (String) httpRequest.getParameter("start_date");
		String endDate = (String) httpRequest.getParameter("end_date");
		String promptIds = (String) httpRequest.getParameter("prompt_id_list");
		String surveyIds = (String) httpRequest.getParameter("survey_id_list");
		String prettyPrint = (String) httpRequest.getParameter("pretty_print");
		String suppressMetadata = (String) httpRequest.getParameter("suppress_metadata");
		String returnId = (String) httpRequest.getParameter("return_id");
		String sortOrder = (String) httpRequest.getParameter("sort_order");
		String privacyState = (String) httpRequest.getParameter("privacy_state");
		String collapse = (String) httpRequest.getParameter("collapse");
		
		// Check for abnormal lengths (buffer overflow attack, sanity check)
		
		if(greaterThanLength("startDate", "start_date", startDate, 10)                         // enforce "yyyy-mm-dd" length 
		   || greaterThanLength("endDate", "end_date", endDate, 10)                            // enforce "yyyy-mm-dd" length                                                 
		   || greaterThanLength("campaignUrn", "campaign_urn", campaignUrn, 250)               // enforce the db column length  
		   || greaterThanLength("client", "client", client, 255)	                           // enforce the db column length	   
		   || greaterThanLength("users", "user_list", users, 150)                              // allows up to 10 users
		   || greaterThanLength("promptIdList", "prompt_id_list", promptIds, 2500)             // arbitrary, but longer than this would be abnormal
		   || greaterThanLength("surveyIdlist", "survey_id_list", surveyIds, 2500)             // arbitrary, but longer than this would be abnormal 
		   || greaterThanLength("columnList", "column_list", columnList, 2500)                 // arbitrary, but longer than this would be abnormal
		   || greaterThanLength("outputFormat", "output_format", outputFormat, 12)             // longest value allowed is "json-columns" 
		   || greaterThanLength("prettyPrint", "pretty_print", prettyPrint, 5)                 // longest value allowed is "false"
		   || greaterThanLength("suppressMetadata", "suppress_metadata", suppressMetadata, 5)  // longest value allowed is "false"
		   || greaterThanLength("returnId", "return_id", returnId, 5)                          // longest value allowed is "false"
		   || greaterThanLength("sortOrder", "sort_order", sortOrder, 21)                      // longest value allowed is "user,timestamp,survey"
		   || greaterThanLength("privacyState", "privacy_state", privacyState, 7)              // longest value allowed is "private"
		   || greaterThanLength("collapse", "collapse", collapse, 5)) {                        // longest value allowed is "false"
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		
		return true;
	}
}
