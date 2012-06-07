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

import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
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
		super(httpRequest);
		
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
						null,
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
						null,
						null,
						null);
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
		
		/*
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
			
			// FIXME: This doesn't take into account the columns list. The 
			// easiest way to fix this is to attempt to insert everything like
			// it exists, and, when it doesn't, just keep on keeping on.
			
			// Serialize the regular data.
			for(DataStream dataStream : regularReadRequest.getResults()) {
				// Start this point's object.
				generator.writeStartObject();
				
				MetaData metaData = dataStream.getMetaData();
				if(metaData == null) {
					LOGGER.error("The meta-data is missing.");
				}
				else {
					String id = metaData.getId();
					if(id == null) {
						LOGGER.error("The ID is missing.");
					}
					else {
						generator.writeStringField("id", id);
					}
					
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
					
					Object mode = dataRecord.get("mode");
					if(mode instanceof Utf8) {
						generator.writeStringField("m", mode.toString());
					}
					else if(mode != null) {
						LOGGER.error("The mode is not a string.");
					}
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
					
					Object mode = dataRecord.get("mode");
					if(mode instanceof Utf8) {
						generator.writeStringField("m", mode.toString());
					}
					else if(mode != null) {
						LOGGER.error("The mode is not a string.");
					}
					
					// Write the sensor data.
					writeSensorData(generator, dataRecord);
					
					// Write the classification data.
					writeClassificationData(generator, dataRecord);
				}
				else {
					LOGGER.info("The record is malformed.");
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
		*/
	}
	
	/**
	 * Adds the sensor data to the generator.
	 * 
	 * @param generator The JSON generator.
	 * 
	 * @param dataRecord The data record.
	 * 
	 * @throws IOException There was an error writing to the generator.
	 * 
	 * @throws JsonGenerationException There was an error generating the JSON.
	 */
	private void writeSensorData(
			final JsonGenerator generator,
			final GenericRecord dataRecord)
			throws IOException, JsonGenerationException {
		
		generator.writeObjectFieldStart("data");
		
		try {
			Object speed = dataRecord.get("speed");
			if(speed instanceof Number) {
				writeNumber(generator, "sp", (Number) speed);
			}
			else if(speed != null) {
				LOGGER.error("The speed is not a number.");
			}
			
			Object accelData = dataRecord.get("accel_data");
			if(accelData instanceof GenericArray) {
				@SuppressWarnings("unchecked")
				GenericArray<GenericRecord> accelDataArray = 
					(GenericArray<GenericRecord>) accelData;
				generator.writeArrayFieldStart("ad");
				
				try {
					for(GenericRecord accelRecord : accelDataArray) {
						generator.writeStartObject();
						
						try {
							Object x = accelRecord.get("x");
							if(x instanceof Number) {
								writeNumber(generator, "x", (Number) x);
							}
							else if(x != null) {
								LOGGER.error("The x-value is not a number.");
							}

							Object y = accelRecord.get("y");
							if(y instanceof Number) {
								writeNumber(generator, "y", (Number) y);
							}
							else if(y != null) {
								LOGGER.error("The y-value is not a number.");
							}

							Object z = accelRecord.get("z");
							if(z instanceof Number) {
								writeNumber(generator, "z", (Number) z);
							}
							else if(z != null) {
								LOGGER.error("The z-value is not a number.");
							}
						}
						finally {
							generator.writeEndObject();
						}
					}
				}
				finally {
					generator.writeEndArray();
				}
			}
			else if(accelData != null) {
				LOGGER.error("The accelerometer data is not an array.");
			}
			
			Object wifiData = dataRecord.get("wifi_data");
			if(wifiData instanceof GenericRecord) {
				GenericRecord wifiDataRecord = (GenericRecord) wifiData;
				generator.writeObjectFieldStart("wd");
				
				try {
					// Write the time.
					Object time = wifiDataRecord.get("time");
					if(time instanceof Number) {
						writeNumber(generator, "t", (Number) time);
					}
					else if(time != null) {
						LOGGER.error("The time is not a number.");
					}
					
					// Write the time zone.
					Object timezone = wifiDataRecord.get("timezone");
					if(timezone instanceof Utf8) {
						generator.writeStringField(
							"tz", 
							timezone.toString());
					}
					else if(timezone != null) {
						LOGGER.error(
							"The time zone is not a string.");
					}
					
					// Write the scan.
					Object scan = wifiDataRecord.get("scan");
					if(scan instanceof GenericArray) {
						@SuppressWarnings("unchecked")
						GenericArray<GenericRecord> scanArray =
							(GenericArray<GenericRecord>) scan;
						generator.writeArrayFieldStart("sc");
						
						try {
							for(GenericRecord scanRecord : scanArray) {
								generator.writeStartObject();
								
								try {
									Object ssid = scanRecord.get("ssid");
									if(ssid instanceof Utf8) {
										generator.writeStringField(
											"ss", 
											ssid.toString());
									}
									else if(ssid != null) {
										LOGGER.error(
											"The SSID is not a string.");
									}
									
									Object strength = 
										scanRecord.get("strength");
									if(strength instanceof Number) {
										writeNumber(
											generator, 
											"st", 
											(Number) strength);
									}
									else if(strength != null) {
										LOGGER.error(
											"The strength is not a number.");
									}
								}
								finally {
									generator.writeEndObject();
								}
							}
						}
						finally {
							generator.writeEndArray();
						}
					}
					else if(scan != null) {
						LOGGER.error("The scan is not an array.");
					}
				}
				finally {
					generator.writeEndObject();
				}
			}
			else if(wifiData != null) {
				LOGGER.error("The WiFi data is not an object.");
			}
		}
		finally {
			// End the sensor data.
			generator.writeEndObject();
		}
	}
	
	/**
	 * Writes the classification data to the output.
	 * 
	 * @param generator The generator.
	 * 
	 * @param dataRecord The classification data record.
	 * 
	 * @throws JsonGenerationException There was an error writing the JSON.
	 * 
	 * @throws IOException There was an error writing to the output stream.
	 */
	private void writeClassificationData(
			final JsonGenerator generator,
			final GenericRecord dataRecord)
			throws JsonGenerationException, IOException {
		
		// FIXME:
		// We still need to decide how we are going to generate and save the
		// classification data.
	}
	
	/**
	 * Takes a number and writes it to the generator with the given key.
	 * 
	 * @param generator The generator.
	 * 
	 * @param key The key to associate with the value.
	 * 
	 * @param value The number value to write.
	 * 
	 * @throws JsonGenerationException There was an error writing the JSON.
	 * 
	 * @throws IOException There was an error writing to the output stream.
	 */
	private void writeNumber(
			final JsonGenerator generator, 
			final String key, 
			final Number value)
			throws JsonGenerationException, IOException {
		
		if(value instanceof Double) {
			generator.writeNumberField(key, (Double) value);
		}
		else if(value instanceof Float) {
			generator.writeNumberField(key, (Float) value);
		}
		else if(value instanceof Integer) {
			generator.writeNumberField(key, (Integer) value);
		}
		else if(value instanceof Long) {
			generator.writeNumberField(key, (Long) value);
		}
		else {
			LOGGER.error("The value is not a number.");
		}
	}
}
