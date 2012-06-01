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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericRecord;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.DataStream.MetaData;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.domain.MobilityPoint.SensorData.SensorDataColumnKey;
import org.ohmage.domain.MobilityPoint.SensorData.WifiData.WifiDataColumnKey;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;
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
	
	/*
	private final DateTime date;
	private final String username;
	private final Collection<ColumnKey> columns;
	
	private List<MobilityPoint> result;
	*/
	
	private final StreamReadRequest regularReadRequest;
	private final StreamReadRequest extendedReadRequest;
	
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
		super(httpRequest);
		
		LOGGER.info("Creating a Mobility read request.");
		
		/*
		DateTime tDate = null;
		String tUsername = null;
		Collection<ColumnKey> tColumns = DEFAULT_COLUMNS;
		*/
		
		StreamReadRequest tRegularReadRequest = null;
		StreamReadRequest tExtendedReadRequest = null;
		
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
				
				/*
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
				*/
				
				Collection<ColumnKey> tColumns = null;
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
						//if(! tColumns.equals(DEFAULT_COLUMNS)) {
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
				
				if(tColumns == null) {
					tColumns = DEFAULT_COLUMNS;
				}
				
				// Create the columns set.
				Set<String> columnsSet = new HashSet<String>();
				for(ColumnKey columnKey : tColumns) {
					// Always consume a MobilityColumnKey.
					if(columnKey instanceof MobilityColumnKey) {
						// Only if it is the mode, then I will add it.
						if(MobilityColumnKey.MODE.equals(columnKey)) {
						
							columnsSet.add(
								((MobilityColumnKey) columnKey).toString(false));
						}
					}
					// We need to strip the "sensor_data" from the 
					// SensorDataColumnKeys.
					else if(columnKey instanceof SensorDataColumnKey) {
						columnsSet.add(
							((SensorDataColumnKey) columnKey).toString(false));
					}
					// If it is the SSID or STRENGTH of a WiFi scan, then we 
					// need to inject the "scan" between "wifi_data" and the
					// actual value.
					else if(WifiDataColumnKey.SSID.equals(columnKey) ||
							WifiDataColumnKey.STRENGTH.equals(columnKey)) {
						
						columnsSet.add(
							WifiDataColumnKey.NAMESPACE +
							WifiDataColumnKey.NAMESPACE_DIVIDOR + 
							WifiDataColumnKey.SCAN.toString(false) +
							WifiDataColumnKey.NAMESPACE_DIVIDOR +
							((WifiDataColumnKey) columnKey).toString(false));
					}
					else {
						columnsSet.add(columnKey.toString());
					}
				}
				
				// Convert the columns set into a string.
				StringBuilder columnsBuilder = new StringBuilder();
				boolean firstPass = true;
				for(String column : columnsSet) {
					if(firstPass) {
						firstPass = false;
					}
					else {
						columnsBuilder.append(',');
					}
					
					columnsBuilder.append(column);
				}
				
				tRegularReadRequest = 
					new StreamReadRequest(
						httpRequest,
						getParameterMap(),
						"edu.ucla.cens.Mobility",
						null,
						"regular",
						2012050700,
						date,
						date.plusDays(1),
						columnsBuilder.toString(),
						null,
						null);
				
				tExtendedReadRequest = 
					new StreamReadRequest(
						httpRequest,
						getParameterMap(),
						"edu.ucla.cens.Mobility",
						null,
						"extended",
						2012050700,
						date,
						date.plusDays(1),
						columnsBuilder.toString(),
						null,
						null);
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		/*
		date = tDate;
		username = tUsername;
		columns = tColumns;
		
		result = Collections.emptyList();
		*/
		
		regularReadRequest = tRegularReadRequest;
		extendedReadRequest = tExtendedReadRequest;
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
		
		regularReadRequest.service();
		extendedReadRequest.service();
		
		/*
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
		*/
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
		
		// Expire the response, but this may be a bad idea.
		expireResponse(httpResponse);
		
		// Set the content type to JSON.
		httpResponse.setContentType("application/json");
		
		// Connect a stream to the response.
		OutputStream outputStream;
		try {
			outputStream = getOutputStream(httpRequest, httpResponse);
		}
		catch(IOException e) {
			LOGGER.warn("Could not connect to the output stream.", e);
			return;
		}
		
		// Create the generator that will stream to the requester.
		JsonGenerator generator;
		try {
			generator =
				(new JsonFactory()).createJsonGenerator(outputStream);
		}
		catch(IOException generatorException) {
			LOGGER.error(
				"Could not create the JSON generator.",
				generatorException);
			
			try {
				outputStream.close();
			}
			catch(IOException streamCloseException) {
				LOGGER.warn(
					"Could not close the output stream.",
					streamCloseException);
			}
			
			return;
		}
		
		try {
			// Start the resulting object.
			generator.writeStartObject();
			
			// Add the result to the object.
			generator.writeObjectField("result", "success");
			
			// Start the data array.
			generator.writeArrayFieldStart("data");
			
			// Serialize the regular data.
			for(DataStream dataStream : regularReadRequest.getResults()) {
				// Start this point's object.
				generator.writeStartObject();
				
				MetaData metaData = dataStream.getMetaData();
				if(metaData == null) {
					LOGGER.error("The meta-data is missing.");
				}
				else {
					DateTime timestamp = metaData.getTimestamp();
					if(timestamp == null) {
						LOGGER.error("The timestamp is missing.");
					}
					else {
						generator.writeStringField(
							"ts", 
							TimeUtils.getIso8601DateString(timestamp, true));
						
						generator.writeStringField(
							"tz", 
							timestamp.getZone().toString());
					}
					
					Location location = metaData.getLocation();
					if(location == null) {
						generator.writeStringField("ls", "unavailable");
					}
					else {
						generator.writeStringField("ls", "valid");
						
						generator.writeObjectFieldStart("l");

						generator.writeNumberField("t", location.getTime());
						generator.writeStringField(
							"tz",
							location.getTimeZone().toString());
						generator.writeNumberField(
							"la",
							location.getLatitude());
						generator.writeNumberField(
							"lo",
							location.getLongitude());
						generator.writeNumberField(
							"ac",
							location.getAccuracy());
						generator.writeStringField(
							"pr",
							location.getProvider());
						
						generator.writeEndObject();
					}
				}
				
				generator.writeStringField("st", "mode_only");
				
				GenericContainer data = dataStream.getData();
				if(data instanceof GenericRecord) {
					GenericRecord dataRecord = (GenericRecord) data;
					
					String mode = dataRecord.get("mode").toString();
					generator.writeStringField("m", mode);
				}
				else {
					LOGGER.error("The record is malformed.");
				}
				
				// End this point's object.
				generator.writeEndObject();
			}
			
			// Serialize the extended data.
			for(DataStream dataStream : extendedReadRequest.getResults()) {
				// Start this point's object.
				generator.writeStartObject();
				
				MetaData metaData = dataStream.getMetaData();
				if(metaData == null) {
					LOGGER.error("The meta-data is missing.");
				}
				else {
					DateTime timestamp = metaData.getTimestamp();
					if(timestamp == null) {
						LOGGER.error("The timestamp is missing.");
					}
					else {
						generator.writeNumberField(
							"t",
							timestamp.getMillis());
						
						generator.writeStringField(
							"ts", 
							TimeUtils.getIso8601DateString(timestamp, true));
						
						generator.writeStringField(
							"tz", 
							timestamp.getZone().toString());
					}
					
					Location location = metaData.getLocation();
					if(location == null) {
						generator.writeStringField("ls", "unavailable");
					}
					else {
						generator.writeStringField("ls", "valid");
						
						generator.writeObjectFieldStart("l");

						generator.writeNumberField("t", location.getTime());
						generator.writeStringField(
							"tz",
							location.getTimeZone().toString());
						generator.writeNumberField(
							"la",
							location.getLatitude());
						generator.writeNumberField(
							"lo",
							location.getLongitude());
						generator.writeNumberField(
							"ac",
							location.getAccuracy());
						generator.writeStringField(
							"pr",
							location.getProvider());
						
						generator.writeEndObject();
					}
				}
				
				generator.writeStringField("st", "sensor_data");
				
				GenericContainer data = dataStream.getData();
				if(data instanceof GenericRecord) {
					GenericRecord dataRecord = (GenericRecord) data;
					
					String mode = dataRecord.get("mode").toString();
					generator.writeStringField("m", mode);
					
					// TODO: Write the sensor data.
					/*
					generator.writeObjectFieldStart("data");
					
					// End the sensor data.
					generator.writeEndObject();
					*/
					
					// TODO: Write the classification data.
				}
				else {
					LOGGER.error("The record is malformed.");
				}
				
				// End this point's object.
				generator.writeEndObject();
			}
			
			// End the data array.
			generator.writeEndArray();
			
			// End the resulting object.
			generator.writeEndObject();
		}
		catch(JsonProcessingException e) {
			LOGGER.error("The JSON could not be processed.", e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch(IOException e) {
			LOGGER.error(
				"The response could no longer be writtent to the response",
				e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		finally {
			// Flush and close the writer.
			try {
				generator.close();
			}
			catch(IOException e) {
				LOGGER.warn("Could not close the generator.", e);
			}
		}
		
		// The old way of doing it.
		/*
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
		*/
	}
}
