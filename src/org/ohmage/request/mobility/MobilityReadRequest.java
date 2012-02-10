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
package org.ohmage.request.mobility;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.MobilityValidators;
import org.ohmage.validator.UserValidators;

/**
 * Reads the Mobility information about a user during a single day.<br />
 * <br />
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
 *     <td>{@value org.ohmage.request.InputKeys#DATE}</td>
 *     <td>The date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user for whom the data is desired. If omitted,
 *       the requesting user is used.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadRequest.class);
	
	private final Date date;
	private final String username;
	
	private List<MobilityPoint> result;
	
	/**
	 * Creates a Mobility read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public MobilityReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a Mobility read request.");
		
		Date tDate = null;
		String tUsername = null;
		
		if(! isFailed()) {
			try {
				String[] t;
				
				t = getParameterValues(InputKeys.DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The date value is missing: " + InputKeys.DATE);
				}
				else if(t.length == 1) {
					tDate = MobilityValidators.validateDate(t[0]);
					
					if(tDate == null) {
						throw new ValidationException(
								ErrorCode.SERVER_INVALID_DATE, 
								"The date value is missing: " + 
										InputKeys.DATE);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple date values were given: " + 
									InputKeys.DATE);
				}
				
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME, 
							"Multiple usernames to query were given: " + 
									InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tUsername = UserValidators.validateUsername(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		date = tDate;
		username = tUsername;
		
		result = Collections.emptyList();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if((username != null) && (! username.equals(getUser().getUsername()))) {
				boolean isPlausible;
				try {
					isPlausible = 
							StringUtils.decodeBoolean(
									PreferenceCache.instance().lookup(
											PreferenceCache.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
				}
				catch(CacheMissException e) {
					throw new ServiceException(e);
				}
				
				try {
					UserServices.instance().verifyUserIsAdmin(
							getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					if(isPlausible) {
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
									getUser().getUsername(), 
									username);
					}
					else {
						throw new ServiceException(
								ErrorCode.MOBILITY_INSUFFICIENT_PERMISSIONS,
								"A user is not allowed to query Mobility information about another user.");
					}
				}
				
				UserServices.instance().checkUserExistance(username, true);
			}
			
			Calendar startDate = TimeUtils.convertDateToCalendar(date);
			startDate.set(Calendar.MILLISECOND, 0);
			startDate.set(Calendar.SECOND, 0);
			startDate.set(Calendar.MINUTE, 0);
			startDate.set(Calendar.HOUR_OF_DAY, 0);
			
			Calendar endDate = new GregorianCalendar();
			endDate.setTimeInMillis(startDate.getTimeInMillis());
			endDate.add(Calendar.DAY_OF_YEAR, 1);
			
			result = MobilityServices.instance().retrieveMobilityData(
					(username == null) ? getUser().getUsername() : username,
					new Date(startDate.getTimeInMillis()), 
					new Date(endDate.getTimeInMillis()), 
					null, 
					null, 
					null);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobiltiy read request.");

		JSONArray resultJson = new JSONArray();
		
		for(MobilityPoint mobilityPoint : result) {
			try {
				resultJson.put(mobilityPoint.toJson(true, false));
			}
			catch(JSONException e) {
				LOGGER.error("Error creating the JSONObject.", e);
				setFailed();
				resultJson = null;
				break;
			}
		}
			
		respond(httpRequest, httpResponse, JSON_KEY_DATA, resultJson);
	}
}
