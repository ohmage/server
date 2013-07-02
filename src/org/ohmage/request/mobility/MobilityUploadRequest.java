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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.observer.StreamUploadRequest;
import org.ohmage.service.ObserverServices;

/**
 * <p>Creates a new Mobility data point. There are no restrictions on who can
 * upload data points only that they have an active account.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the user that is uploading this point and for whom
 *       the point applies.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The user's hashed password.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DATA}</td>
 *     <td>A JSONArray of JSONObjects where each JSONObject is an individual
 *       data point. Each data point must contain the following keys and 
 *       values:
 *       <ul>
 *         <li><b>date</b>: A date and time in the ISO 8601 format representing
 *           the time at which this Mobility data point was created.</li>
 *         <li><b>time</b>: The number of milliseconds since the epoch 
 *           representing the time at which this Mobility data point was 
 *           created.</li>
 *         <li><b>timezone</b>: The time zone of the device when it created 
 *           this Mobility data point.</li>
 *         <li><b>subtype</b>: The type of data point, which defines other
 *           aspects of this Mobility data point; one of
 *           {@link org.ohmage.domain.MobilityInformation.SubType}.</li>
 *         <li><b>location_status</b>: The status of the location information;
 *           one of
 *           {@link org.ohmage.domain.MobilityInformation.LocationStatus}.</li>
 *         <li><b>location</b>: The location information obtained when this
 *           Mobility data point was created. It may be absent if the
 *           'location_status' is
 *           {@value org.ohmage.domain.MobilityInformation.LocationStatus#UNAVAILABLE}.
 *           If present, it must contain the following information:
 *           <ul>
 *             <li><b>latitude</b>: The latitude of the device.</li>
 *             <li><b>longitude</b>: The longitude of the device.</li>
 *             <li><b>accuracy</b>: A double value representing the radius of
 *               the area in which the device most-likely was from the center
 *               as defined by the 'latitude' and 'longitude'.</li>
 *             <li><b>provider</b>: A string representing who supplied the GPS
 *               coordinates and accuracy.</li>
 *             <li><b>timestamp</b>: A date and time in the ISO 8601 format
 *               representing the date and time when this specific location 
 *               value was collected.</li>
 *           </ul></li>
 *       </ul>
 *       SubType: 
 *       {@value org.ohmage.domain.MobilityInformation.SubType#MODE_ONLY}
 *       <ul>
 *         <li><b>mode</b>: The device-calculated mode of the user; one of
 *           {@link org.ohmage.domain.MobilityInformation.Mode}</li>
 *       </ul>
 *       SubType:
 *       {@value org.ohmage.domain.MobilityInformation.SubType#SENSOR_DATA}
 *       <ul>
 *         <li><b>data</b>: The collected sensor data used to calculate the 
 *           user's mode. This must contain the following:
 *           <ul>
 *             <li><b>mode</b>: The device-calculated mode of the user; one of
 *               {@link org.ohmage.domain.MobilityInformation.Mode}</li>
 *             <li><b>speed</b>: A double value approximating the speed of the
 *               user.</li>
 *             <li><b>accel_data</b>: A JSONArray of JSONObjects representing
 *               the accelerometer data collected and used to determine this
 *               mode. Each JSONObject (accelerometer reading) must have the
 *               following format:
 *               <ul>
 *                 <li><b>x</b>: The 'x'-acceleration of the device.</li>
 *                 <li><b>y</b>: The 'y'-acceleration of the device.</li>
 *                 <li><b>z</b>: The 'z'-acceleration of the device.</li>
 *               </ul></li>
 *             <li><b>wifi_data</b>: A JSONObject explaining the WiFi data that
 *               was collected and used to calculate this mode. Each JSONObject
 *               must have the following format:
 *               <ul>
 *                 <li><b>scan</b>: A JSONArray of JSONObjects where each 
 *                   JSONObject represents the information about a single 
 *                   access point that whose information was gathered during 
 *                   the scan. Each JSONObject must have the following format:
 *                   <ul>
 *                     <li><b>ssid</b>: The SSID of the access point.</li>
 *                     <li><b>strength</b>: The strength as measured by radios
 *                       representing the strength of the signal from this
 *                       access point.</li>
 *                   </ul></li>
 *                 <li><b>timestamp</b>: A date and time in the ISO8601 format
 *                   representing the date and time when this WiFi scan took
 *                   place.</li>
 *               </ul></li>
 *           </ul></li>
 *       </ul></td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityUploadRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(MobilityUploadRequest.class);
	
	private static final String OBSERVER_ID = "edu.ucla.cens.Mobility";
	private static final long OBSERVER_VERSION = 2012061300;

	private static final String AUDIT_KEY_VALID_POINT_IDS = "accepted_point_id";
	private static final String AUDIT_KEY_INVALID_POINTS = "invalid_mobility_point";
	private static final String JSON_KEY_ACCEPTED_IDS = "accepted_ids";
	private static final String JSON_KEY_INVALID_INDICIES = "invalid_indicies";
	
	private final Collection<String> validIds;
	private final Map<Integer, String> invalidPointsMap;
	private final Collection<JSONObject> invalidPointsJson;
	
	private final StreamUploadRequest streamUploadRequest;
	
	/**
	 * Creates a Mobility upload request.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the parameters for
	 * 					  this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityUploadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null);
		
		LOGGER.info("Creating a Mobility upload request.");
		
		validIds = new LinkedList<String>();
		invalidPointsMap = new HashMap<Integer, String>();
		invalidPointsJson = new LinkedList<JSONObject>();
		
		StreamUploadRequest tStreamUploadRequest = null;
		
		if(! isFailed()) {
			try {
				String[] dataArray = getParameterValues(InputKeys.DATA);
				if(dataArray.length == 0) {
					throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_DATA,
						"The upload data is missing: " + 
							ErrorCode.MOBILITY_INVALID_DATA);
				}
				else if(dataArray.length > 1) {
					throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_DATA,
						"Multiple data parameters were given: " + 
							ErrorCode.MOBILITY_INVALID_DATA);
				}
				else {
					JSONArray jsonDataArray;
					try {
						jsonDataArray = new JSONArray(dataArray[0]);
					}
					catch(JSONException e) {
						throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_DATA,
							"The data is not well formed.",
							e);
					}
					
					JSONArray resultDataArray = new JSONArray();
					for(int i = 0; i < jsonDataArray.length(); i++) {
						JSONObject pointJson;
						try {
							pointJson = jsonDataArray.getJSONObject(i);
						}
						catch(JSONException e) {
							throw new ValidationException(
								ErrorCode.MOBILITY_INVALID_DATA,
								"A Mobility data point was not a JSON object.",
								e);
						}
						
						MobilityPoint point;
						try {
							point = 
								new MobilityPoint(
									pointJson,
									MobilityPoint.PrivacyState.PRIVATE);
						}
						catch(DomainException e) {
							invalidPointsMap.put(i, e.getMessage());
							invalidPointsJson.add(pointJson);
							continue;
						}

						validIds.add(point.getId().toString());
						
						try {
							JSONObject jsonPoint = new JSONObject();
							if(MobilityPoint.Mode.ERROR.equals(point.getMode())) {
								jsonPoint.put("stream_id", "error");
								
								// Create the error object.
								JSONObject errorObject = new JSONObject();
								errorObject.put("mode", MobilityPoint.Mode.ERROR.toString().toLowerCase());
								
								jsonPoint.put("data", errorObject);
								jsonPoint.put("stream_version", 2012061300);
							}
							else if(MobilityPoint.SubType.MODE_ONLY.equals(point.getSubType())) {
								jsonPoint.put("stream_id", "mode_only");
								
								// Create the mode object.
								JSONObject modeObject = new JSONObject();
								modeObject.put("mode", point.getMode().toString());
								
								jsonPoint.put("data", modeObject);
								jsonPoint.put("stream_version", 2012050700);
							}
							else {
								jsonPoint.put("stream_id", "extended");
								
								// Add the sensor data and rename it to "data".
								Collection<ColumnKey> columns = new LinkedList<ColumnKey>();
								columns.add(MobilityColumnKey.SENSOR_DATA);
								JSONObject mobilityJson;
								try {
									mobilityJson = point.toJson(false, columns);
								}
								catch(DomainException e) {
									throw new ValidationException(
										"The point could not be converted back to a JSON object.",
										e);
								}
								jsonPoint.put("data", mobilityJson.getJSONObject("sensor_data"));
								jsonPoint.put("stream_version", 2012050700);
							}
							
							JSONObject metadata = new JSONObject();
							metadata.put("id", point.getId().toString());
							metadata.put("time", point.getTime());
							metadata.put("timezone", point.getTimezone().getID());
							
							Location location = point.getLocation();
							if(location != null) {
								try {
									metadata.put(
										"location", 
										location.toJson(
											false, 
											LocationColumnKey.ALL_COLUMNS));
								}
								catch(DomainException e) {
									throw new ValidationException(
										"The location could not be converted back to a JSON object.",
										e);
								}
							}
							jsonPoint.put("metadata", metadata);
	
							resultDataArray.put(jsonPoint);
						}
						catch(JSONException e) {
							throw new ValidationException(
								"The stream information could not be built.",
								e);
						}
					}
					
					tStreamUploadRequest =
						new StreamUploadRequest(
							httpRequest,
							getParameterMap(),
							OBSERVER_ID,
							OBSERVER_VERSION,
							resultDataArray.toString(),
							false);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		streamUploadRequest = tStreamUploadRequest;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility upload request.");
		
		if(! streamUploadRequest.isFailed()) {
			
			try {
				LOGGER.info("Verifying that the Mobility observer exists.");
				ObserverServices
					.instance().getObserver(OBSERVER_ID, OBSERVER_VERSION);
			}
			catch(ServiceException e) {
				e.failRequest(this);
				e.logException(LOGGER, true);
			}
				
			LOGGER.info("Delegating to the stream upload service layer.");
			streamUploadRequest.service();
		}
	}

	/**
	 * Responds to the request with either a success message or a failure 
	 * message that contains an error code and an error text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobility upload request.");
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else if(streamUploadRequest.isFailed()) {
			streamUploadRequest.respond(httpRequest, httpResponse);
		}
		else {
			JSONObject response = new JSONObject();
			
			try {
				response.put(JSON_KEY_INVALID_INDICIES, invalidPointsMap);
				response.put(JSON_KEY_ACCEPTED_IDS, validIds);
			}
			catch(JSONException e) {
				LOGGER.error("Error creating the response.", e);
				setFailed();
			}
			
			super.respond(httpRequest, httpResponse, response);
		}
	}
	
	/**
	 * Adds to the parent's audit information map the invalid Mobility points.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> result = super.getAuditInformation();
		
		// If the stream upload request was created, add its audit information
		// to the current information.
		if(streamUploadRequest != null) {
			// Get the stream upload request's audit information.
			Map<String, String[]> streamAuditInfo =
				streamUploadRequest.getAuditInformation();
			
			// Combine this audit information with the stream upload request's
			// audit information.
			for(String key : streamAuditInfo.keySet()) {
				String[] previousArray = result.get(key);
				String[] currArray = streamAuditInfo.get(key);
				
				// If this audit information doesn't have values for the
				// current key, just save the stream upload request's audit
				// information by itself.
				if(previousArray == null) {
					previousArray = currArray;
				}
				// If this audit information does have value for the current
				// key, create a new array that is the concatenation of the
				// current array and the stream upload request's array.
				else {
					String[] newArray =
						new String[previousArray.length + currArray.length];
					
					for(int i = 0; i < previousArray.length; i++) {
						newArray[i] = previousArray[i];
					}
					for(int i = 0; i < currArray.length; i++) {
						newArray[previousArray.length + i] = currArray[i];
					}
					
					previousArray = newArray;
				}
				
				// Save the "previous" array, which should now include all
				// values.
				result.put(key, previousArray);
			}
		}
		
		result.put(AUDIT_KEY_VALID_POINT_IDS, validIds.toArray(new String[0]));
		
		if(invalidPointsJson != null) {
			String[] invalidPointsArray = new String[invalidPointsJson.size()];
			int numPointsAdded = 0;
			for(JSONObject invalidPoint : invalidPointsJson) {
				invalidPointsArray[numPointsAdded] = invalidPoint.toString();
				numPointsAdded++;
			}
			result.put(AUDIT_KEY_INVALID_POINTS, invalidPointsArray);
		}
		
		return result;
	}
}
