/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.request.clazz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignClassServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Gathers all classes and then searches through them removing those that do
 * not match the search criteria. A missing or empty string for a parameter 
 * means that it will not be used to filter the list. If no parameters are 
 * given, information about every class in the system will be returned.</p>
 * <p>The requester must be an admin.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN}</td>
 *     <td>A string to search for within the URN of every class and only return
 *       information about those that contain this string and that match the 
 *       rest of the parameters.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_NAME}</td>
 *     <td>A string to search for within the name of every class and only  
 *       return information about those that contain this string and that match  
 *       the rest of the parameters.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>A string to search for within the description of every class and 
 *       only return information about those that contain this string and that  
 *       match the rest of the parameters.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_SKIP}</td>
 *     <td>The number of classes to skip before processing to facilitate 
 *       paging.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_RETURN}</td>
 *     <td>The number of classes to return after skipping to facilitate paging.
 *       </td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassSearchRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassSearchRequest.class);
	
	private static final String JSON_KEY_USERNAMES = "usernames";
	private static final String JSON_KEY_CAMPAIGNS = "campaigns";
	
	private final String classId;
	private final String className;
	private final String classDescription;
	
	private final int numToSkip;
	private final int numToReturn;
	
	private final Map<Clazz, Map<String, Clazz.Role>> classes;
	private final Map<Clazz, Collection<String>> classToCampaignIdsMap;
	private int totalNumResults;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the
	 * 					  parameters to and metadata for this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ClassSearchRequest(final HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.EITHER, null);
		
		String tClassId = null;
		String tClassName = null;
		String tClassDescription = null;
		
		int tNumToSkip = 0;
		int tNumToReturn = Clazz.MAX_NUM_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a class search request.");
			
			String[] t = null;
			try {
				t = getParameterValues(InputKeys.CLASS_URN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID,
							"Multiple class IDs were given: " +
								InputKeys.CLASS_URN);
				}
				else if(t.length == 1) {
					tClassId = t[0];
				}
				
				t = getParameterValues(InputKeys.CLASS_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_NAME,
							"Multiple class names were given: " + 
								InputKeys.CLASS_NAME);
				}
				else if(t.length == 1) {
					tClassName = t[0];
				}
				
				t = getParameterValues(InputKeys.DESCRIPTION);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_DESCRIPTION,
							"Multiple class descriptions were given: " +
								InputKeys.DESCRIPTION);
				}
				else if(t.length == 1) {
					tClassDescription = t[0];
				}
				
				t = getParameterValues(InputKeys.NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
							"Multiple number to skip parameters were given: " + 
								InputKeys.NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = ClassValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
							"Multiple number to return parameters were given: " +
								InputKeys.NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = ClassValidators.validateNumToReturn(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		classId = tClassId;
		className = tClassName;
		classDescription = tClassDescription;
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		classes = new HashMap<Clazz, Map<String, Clazz.Role>>();
		classToCampaignIdsMap = new HashMap<Clazz, Collection<String>>();
		totalNumResults = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the class search request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}

		try {
			LOGGER.info("Checking that the user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Searching for the classes that satisfy the parameters.");
			Collection<String> classIds = 
				ClassServices.instance().
					classIdSearch(classId, className, classDescription);
			totalNumResults = classIds.size();
			
			if(numToSkip >= classIds.size()) {
				classIds.clear();
			}
			else if(numToReturn >= 0) {
				List<String> sortedClassIds = new ArrayList<String>(classIds);
				Collections.sort(sortedClassIds);
				
				int lastIndex = numToSkip + numToReturn;
				if(lastIndex > totalNumResults) {
					lastIndex = totalNumResults;
				}
				
				classIds = sortedClassIds.subList(numToSkip, lastIndex);
			}
			
			LOGGER.info("Gathering the detailed information about the classes.");
			classes.putAll(
				ClassServices.instance().getClassesInformation(
						getUser().getUsername(), 
						classIds,
						null,
						null,
						null,
						true));
			
			LOGGER.info("Gathering the IDs for the campaigns associated with each class.");
			for(Clazz clazz : classes.keySet()) {
				classToCampaignIdsMap.put(
						clazz, 
						CampaignClassServices.instance().getCampaignIdsForClass(
								clazz.getId())
							);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to a class search request.");
		JSONObject result = null;
		JSONObject metadata = null;
		
		if(! isFailed()) {
			metadata = new JSONObject();
			result = new JSONObject();
			
			try {
				metadata.put(
						Request.JSON_KEY_TOTAL_NUM_RESULTS, 
						totalNumResults);
				
				for(Clazz clazz : classes.keySet()) {
					JSONObject classJson = clazz.toJson(false);
					
					classJson.put(JSON_KEY_USERNAMES, classes.get(clazz).keySet());
					classJson.put(JSON_KEY_CAMPAIGNS, classToCampaignIdsMap.get(clazz));
					
					result.put(clazz.getId(), classJson);
				}
			}
			catch(JSONException e) {
				LOGGER.error("There was an error building the result.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, metadata, result);
	}
}
