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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
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
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#MOBILITY_WITH_SENSOR_DATA}
 *       </td>
 *     <td>A boolean flag indicating whether or not to include sensor data with
 *       each point. This includes things like the accelerometer data and the
 *       WiFi scan results.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#COLUMN_LIST}
 *       </td>
 *     <td>A list of the columns to return data. The order in this list will be
 *       reflected in the resulting list. If omitted, the result will be all of
 *       the columns available.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadRequest.class);
	
	private static final Collection<ColumnKey> DEFAULT_COLUMNS;
	static {
		Collection<ColumnKey> columnKeys = new ArrayList<ColumnKey>();
		
		columnKeys.add(MobilityColumnKey.ID);
		columnKeys.add(MobilityColumnKey.MODE);
		columnKeys.add(MobilityColumnKey.TIME);
		columnKeys.add(MobilityColumnKey.TIMEZONE);
		columnKeys.add(MobilityColumnKey.TIMESTAMP);
		
		columnKeys.add(LocationColumnKey.STATUS);
		columnKeys.add(LocationColumnKey.LATITUDE);
		columnKeys.add(LocationColumnKey.LONGITUDE);
		columnKeys.add(LocationColumnKey.PROVIDER);
		columnKeys.add(LocationColumnKey.ACCURACY);
		columnKeys.add(LocationColumnKey.TIME);
		columnKeys.add(LocationColumnKey.TIMEZONE);
		
		DEFAULT_COLUMNS = Collections.unmodifiableCollection(columnKeys);
	}
	
	private final DateTime date;
	private final String username;
	private final Collection<ColumnKey> columns;
	
	private List<MobilityPoint> result;
	
	/**
	 * Creates a Mobility read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a Mobility read request.");
		
		DateTime tDate = null;
		String tUsername = null;
		Collection<ColumnKey> tColumns = DEFAULT_COLUMNS;
		
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
				
				t = getParameterValues(InputKeys.MOBILITY_WITH_SENSOR_DATA);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_INCLUDE_SENSOR_DATA_VALUE, 
							"Multiple \"include sensor data\" values to query were given: " + 
									InputKeys.MOBILITY_WITH_SENSOR_DATA);
				}
				else if(t.length == 1) {
					if(MobilityValidators.validateIncludeSensorDataValue(t[0])) {
						tColumns = MobilityColumnKey.ALL_COLUMNS;
					}
				}
				
				t = getParameterValues(InputKeys.COLUMN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
							"Multiple column lists were given: " +
									InputKeys.COLUMN_LIST);
				}
				else if(t.length == 1) {
					if(! StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						if(! tColumns.equals(DEFAULT_COLUMNS)) {
							throw new ValidationException(
									ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
									"Both '" +
										InputKeys.MOBILITY_WITH_SENSOR_DATA +
										"' and '" +
										InputKeys.COLUMN_LIST +
										"' were present. Only one may be present.");
						}
						else {
							tColumns = 
									MobilityValidators.validateColumns(
											t[0],
											true);
						}
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		date = tDate;
		username = tUsername;
		columns = tColumns;
		
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
				LOGGER.info("Checking if reading Mobility points about another user is even allowed.");
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
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
							getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");
					if(isPlausible) {
						LOGGER.info("Checking if the requester is allowed to read Mobility points about the user.");
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
			
			DateTime startDate = 
					new DateTime(
						date.getYear(), 
						date.getMonthOfYear(), 
						date.getDayOfMonth(),
						0, 
						0);
			
			DateTime endDate = startDate.plusDays(1);
			
			LOGGER.info("Gathering the Mobility points.");
			result = MobilityServices.instance().retrieveMobilityData(
					(username == null) ? getUser().getUsername() : username,
					startDate, 
					endDate, 
					null, 
					null, 
					null);
			LOGGER.info("Found " + result.size() + " results.");
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
				resultJson.put(mobilityPoint.toJson(true, columns));
			}
			catch(JSONException e) {
				LOGGER.error("Error creating the JSONObject.", e);
				setFailed();
				resultJson = null;
				break;
			}
			catch(DomainException e) {
				LOGGER.error("Error creating the JSONObject.", e);
				setFailed();
				resultJson = null;
				break;
			}
		}
			
		respond(httpRequest, httpResponse, JSON_KEY_DATA, resultJson);
	}
}
