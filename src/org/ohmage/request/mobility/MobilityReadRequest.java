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
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.domain.MobilityPoint.SubType;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.MobilityValidators;

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
public class MobilityReadRequest extends Request {
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
	
	private final StreamReadRequest regularReadRequest;
	private final StreamReadRequest extendedReadRequest;
	
	private final Collection<ColumnKey> columns;
	private final List<MobilityPoint> points;
	
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
		super(httpRequest, null);
		
		LOGGER.info("Creating a Mobility read request.");
		
		StreamReadRequest tRegularReadRequest = null;
		StreamReadRequest tExtendedReadRequest = null;
		
		Collection<ColumnKey> tColumns = null;
		
		if(! isFailed()) {
			try {
				String[] t;
				
				DateTime date;
				t = getParameterValues(InputKeys.DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The date value is missing: " + InputKeys.DATE);
				}
				else if(t.length == 1) {
					date = MobilityValidators.validateDate(t[0]);
					
					if(date == null) {
						throw new ValidationException(
								ErrorCode.SERVER_INVALID_DATE, 
								"The date value is missing: " + 
										InputKeys.DATE);
					}
					else {
						date = 
							new DateTime(
								date.getYear(), 
								date.getMonthOfYear(), 
								date.getDayOfMonth(),
								0, 
								0,
								DateTimeZone.UTC);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple date values were given: " + 
									InputKeys.DATE);
				}
				
				tColumns = null;
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
						if(tColumns == null) {
							tColumns = 
								MobilityValidators.validateColumns(
										t[0],
										true);
						}
						else {
							throw new ValidationException(
									ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
									"Both '" +
										InputKeys.MOBILITY_WITH_SENSOR_DATA +
										"' and '" +
										InputKeys.COLUMN_LIST +
										"' were present. Only one may be present.");
						}
					}
				}
				if(tColumns == null) {
					tColumns = DEFAULT_COLUMNS;
				}
				
				// Always get all of the columns.
				try {
					tRegularReadRequest = 
						new StreamReadRequest(
							httpRequest,
							getParameterMap(),
							false,
							TokenLocation.EITHER,
							"edu.ucla.cens.Mobility",
							null,
							"regular",
							2012050700,
							date,
							date.plusDays(1),
							null,
							null,
							null);
					
					tExtendedReadRequest = 
						new StreamReadRequest(
							httpRequest,
							getParameterMap(),
							false,
							TokenLocation.EITHER,
							"edu.ucla.cens.Mobility",
							null,
							"extended",
							2012050700,
							date,
							date.plusDays(1),
							null,
							null,
							null);
				}
				catch(IllegalArgumentException e) {
					throw new ValidationException(
						"There was an error creating the request.",
						e);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		regularReadRequest = tRegularReadRequest;
		extendedReadRequest = tExtendedReadRequest;
		
		columns = tColumns;
		points = new ArrayList<MobilityPoint>();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		// If any of the sub-requests have failed, then return.
		if(regularReadRequest.isFailed() || extendedReadRequest.isFailed()) {
			return;
		}
		
		LOGGER.info("Servicing the Mobility read request.");
		
		try {
			// Service the read requests.
			regularReadRequest.service();
			if(regularReadRequest.isFailed()) {
				return;
			}
			extendedReadRequest.service();
			if(extendedReadRequest.isFailed()) {
				return;
			}
			
			LOGGER.info("Aggregating the resulting points.");
			Collection<DataStream> regularResults = 
				regularReadRequest.getResults();
			for(DataStream dataStream : regularResults) {
				try {
					points.add(
						new MobilityPoint(
							dataStream, 
							SubType.MODE_ONLY,
							MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException e) {
					throw new ServiceException(
						"One of the points was invalid.",
						e);
				}
			}

			Collection<DataStream> extendedResults = 
				extendedReadRequest.getResults();
			for(DataStream dataStream : extendedResults) {
				try {
					points.add(
						new MobilityPoint(
							dataStream, 
							SubType.SENSOR_DATA,
							MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException e) {
					throw new ServiceException(
						"One of the points was invalid.",
						e);
				}
			}
			
			LOGGER.info("Sorting the aggregated points.");
			Collections.sort(points);
			
			// Run them through the classifier.
			LOGGER.info("Classifying the points.");
			MobilityServices.instance().classifyData(
				regularReadRequest.getUser().getUsername(),
				points);
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

		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else if(regularReadRequest.isFailed()) {
			regularReadRequest.respond(httpRequest, httpResponse);
		}
		else if(extendedReadRequest.isFailed()) {
			extendedReadRequest.respond(httpRequest, httpResponse);
		}
		
		JSONObject resultObject = new JSONObject();
		try {
			JSONArray resultArray = new JSONArray();
			for(MobilityPoint mobilityPoint : points) {
				resultArray.put(mobilityPoint.toJson(true, columns));
			}
			resultObject.put(JSON_KEY_DATA, resultArray);
		}
		catch(JSONException e) {
			LOGGER.error("Error creating the JSONObject.", e);
			setFailed();
		}
		catch(DomainException e) {
			LOGGER.error("Error creating the JSONObject.", e);
			setFailed();
		}

		super.respond(httpRequest, httpResponse, resultObject);
	}
}