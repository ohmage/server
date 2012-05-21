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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.MobilityValidators;

/**
 * Gathers the Mobility information about the user and the combines the 
 * information into chunks to reduce its size.<br />
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
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The earliest date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The latest date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#MOBILITY_CHUNK_DURATION_MINUTES}</td>
 *     <td>The number of minutes which defines the size of the buckets in
 *       minutes.</td>
 *     <td>true</td>
 *   </tr>
 * </table> 
 * 
 * @author John Jenkins
 */
public class MobilityReadChunkedRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadChunkedRequest.class);
	
	private static final String JSON_KEY_MODE_COUNT = "mc";
	private static final String JSON_KEY_DURATION = "d";
	private static final String JSON_KEY_TIMESTAMP = "ts";
	private static final String JSON_KEY_TIMEZONE = "tz";
	private static final String JSON_KEY_LOCATION_STATUS = "ls";
	private static final String JSON_KEY_LOCATION = "l";
	
	//private static final int POINTS_PER_CHUNK = 10;
	// 10 minutes
	private static final long DEFAULT_MILLIS_PER_CHUNK = 1000 * 60 * 10;
	
	// 10 days
	private static final long MAX_MILLIS_BETWEEN_START_AND_END_DATES = 1000 * 60 * 60 * 24 * 10; 
	
	private final DateTime startDate;
	private final DateTime endDate;
	private final long millisPerChunk;
	
	private List<MobilityPoint> result;
	
	/**
	 * Creates a new Mobility read chunked request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityReadChunkedRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a Mobility read chunked request.");
		
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		Long tMillisPerChunk = DEFAULT_MILLIS_PER_CHUNK;
		
		if(! isFailed()) {
			try {
				String[] t;
				
				// Get the start date.
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The start date is missing: " + 
									InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = MobilityValidators.validateDate(t[0]);
					
					if(tStartDate == null) {
						throw new ValidationException(
								ErrorCode.SERVER_INVALID_DATE, 
								"The start date is missing: " + 
										InputKeys.START_DATE);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start dates were given: " + 
									InputKeys.START_DATE);
				}
				
				// Get the end date.
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The end date is missing: " + 
									InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = MobilityValidators.validateDate(t[0]);
					
					if(tEndDate == null) {
						throw new ValidationException(
								ErrorCode.SERVER_INVALID_DATE, 
								"The end date is missing: " + 
										InputKeys.END_DATE);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple end dates were given: " + 
									InputKeys.END_DATE);
				}
				
				// Ensure that the duration between the start and end dates
				// doesn't exceed our maximum.
				if((tEndDate.getMillis() - tStartDate.getMillis()) > MAX_MILLIS_BETWEEN_START_AND_END_DATES) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The maximum time range between the start and end dates is 10 days.");
				}
				
				// Get the duration frequency.
				t = getParameterValues(
						InputKeys.MOBILITY_CHUNK_DURATION_MINUTES);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_CHUNK_DURATION, 
							"Multiple chunk durations were given: " + 
									InputKeys.MOBILITY_CHUNK_DURATION_MINUTES);
				}
				else if(t.length == 1) {
					tMillisPerChunk = 
							MobilityValidators.validateChunkDuration(t[0]);
					
					if(tMillisPerChunk == null) {
						tMillisPerChunk = DEFAULT_MILLIS_PER_CHUNK;
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		startDate = tStartDate;
		endDate = tEndDate;
		millisPerChunk = tMillisPerChunk;
		
		result = Collections.emptyList();
	}

	/**
	 * Service the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility read chunked request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Gathering the data.");
			result =
					MobilityServices.instance().retrieveMobilityData(
						getUser().getUsername(), 
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
	 * Responds to the Mobility read chunked request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobility read chunked request.");
		
		Map<Long, List<MobilityPoint>> millisToPointMap =
				new HashMap<Long, List<MobilityPoint>>();
		
		// Bucket the items preserving order in the bucket.
		for(MobilityPoint mobilityPoint : result) {
			long time = mobilityPoint.getTime();
			
			// Get this point's bucket.
			time = (time / millisPerChunk) * millisPerChunk;
			
			List<MobilityPoint> bucket = millisToPointMap.get(time);
			
			if(bucket == null) {
				bucket = new LinkedList<MobilityPoint>();
				millisToPointMap.put(time, bucket);
			}
			
			bucket.add(mobilityPoint);
		}

		JSONArray outputArray = new JSONArray();
		
		// Process the buckets.
		try {
			for(Long time : millisToPointMap.keySet()) {
				Map<String, Integer> modeCountMap =
						new HashMap<String, Integer>();
				String timestamp = null;
				DateTimeZone timezone = null;
				LocationStatus locationStatus = null;
				Location location = null;
				
				for(MobilityPoint mobilityPoint : millisToPointMap.get(time)) {
					// The first point sets the information.
					if(timestamp == null) {
						timestamp = 
								TimeUtils.getIso8601DateString(
									mobilityPoint.getDate(), 
									true);
						timezone = mobilityPoint.getTimezone();
						
						locationStatus = mobilityPoint.getLocationStatus();
						location = mobilityPoint.getLocation();
					}
					
					// For all points, get the mode.
					String mode = mobilityPoint.getMode().toString().toLowerCase();
					Integer count = modeCountMap.get(mode);
					
					if(count == null) {
						modeCountMap.put(mode, 1);
					}
					else {
						modeCountMap.put(mode, count + 1);
					}
				}
				
				JSONObject currResult = new JSONObject();
				currResult.put(JSON_KEY_MODE_COUNT, modeCountMap);
				currResult.put(JSON_KEY_DURATION, millisPerChunk);
				currResult.put(JSON_KEY_TIMESTAMP, timestamp);
				currResult.put(JSON_KEY_TIMEZONE, timezone.getID());
				currResult.put(
						JSON_KEY_LOCATION_STATUS, 
						locationStatus.toString().toLowerCase());
				try {
					currResult.put(
							JSON_KEY_LOCATION, 
							((location == null) ? null : location.toJson(true, LocationColumnKey.ALL_COLUMNS)));
				} 
				catch(DomainException e) {
					LOGGER.error("Error creating the JSON.", e);
					setFailed();
				}
				
				outputArray.put(currResult);
			}
		}
		catch(JSONException e) {
			LOGGER.error("Error building the response." , e);
			setFailed();
		}
		
		super.respond(httpRequest, httpResponse, JSON_KEY_DATA, outputArray);
	}
}
