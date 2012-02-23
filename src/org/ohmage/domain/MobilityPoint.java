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
package org.ohmage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.MobilityPoint.SensorData.AccelData;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

import edu.ucla.cens.mobilityclassifier.Sample;

/**
 * This class is responsible for individual Mobility data points.
 * 
 * @author John Jenkins
 */
public class MobilityPoint {
	public static final String JSON_KEY_ID = "id";
	private static final String JSON_KEY_TIMESTAMP = "timestamp";
	private static final String JSON_KEY_TIMESTAMP_SHORT = "ts";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIME_SHORT = "t";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_TIMEZONE_SHORT = "tz";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION_STATUS_SHORT = "ls";
	private static final String JSON_KEY_LOCATION = "location";
	private static final String JSON_KEY_LOCATION_SHORT = "l";

	public static final String JSON_KEY_SUBTYPE = "subtype";
	public static final String JSON_KEY_SUBTYPE_SHORT = "st";
	
	// Mode-only
	private static final String JSON_KEY_MODE = "mode";
	private static final String JSON_KEY_MODE_SHORT = "m";
	
	// Sensor data
	private static final String JSON_KEY_DATA = "data";
	
	private final UUID id;
	private final long time;
	private final TimeZone timezone;
	
	public static enum LocationStatus { VALID, NETWORK, INACCURATE, STALE, UNAVAILABLE };
	private final LocationStatus locationStatus;
	private final Location location;
	
	/**
	 * Known Mobility privacy states.
	 * @author  John Jenkins
	 */
	public static enum PrivacyState {
		PRIVATE,
		SHARED;
		
		/**
		 * Converts a String value into a PrivacyState or throws an exception
		 * if there is no comparable privacy state.
		 * 
		 * @param privacyState The privacy state to be converted into a 
		 * 					   PrivacyState enum.
		 * 
		 * @return A comparable PrivacyState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									PrivacyState enum.
		 */
		public static PrivacyState getValue(String privacyState) {
			return valueOf(privacyState.toUpperCase());
		}
		
		/**
		 * Converts the privacy state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final PrivacyState privacyState;

	public static enum SubType { MODE_ONLY, SENSOR_DATA };
	private final SubType subType;
	
	public static enum Mode { STILL, WALK, RUN, BIKE, DRIVE, ERROR };
	
	// Mode-only
	private final Mode mode;
	
	// Sensor data
	/**
	 * This class is responsible for managing the sensor data in a single
	 * Mobility entry.
	 * 
	 * @author John Jenkins
	 */
	public static final class SensorData {
		private static final String JSON_KEY_SENSOR_DATA_MODE = "mode";
		private static final String JSON_KEY_SENSOR_DATA_MODE_SHORT = "m";
		
		private static final String JSON_KEY_SPEED = "speed";
		private static final String JSON_KEY_SPEED_SHORT = "sp";
		
		private static final String JSON_KEY_ACCEL_DATA = "accel_data";
		private static final String JSON_KEY_ACCEL_DATA_SHORT = "ad";
		private static final String JSON_KEY_ACCEL_DATA_X = "x";
		private static final String JSON_KEY_ACCEL_DATA_Y = "y";
		private static final String JSON_KEY_ACCEL_DATA_Z = "z";
		
		private static final String JSON_KEY_WIFI_DATA = "wifi_data";
		private static final String JSON_KEY_WIFI_DATA_SHORT = "wd";
		private static final String JSON_KEY_WIFI_DATA_TIMESTAMP = "timestamp";
		private static final String JSON_KEY_WIFI_DATA_TIMESTAMP_SHORT = "ts";
		private static final String JSON_KEY_WIFI_DATA_TIME = "time";
		private static final String JSON_KEY_WIFI_DATA_TIME_SHORT = "t";
		private static final String JSON_KEY_WIFI_DATA_TIMEZONE = "timezone";
		private static final String JSON_KEY_WIFI_DATA_TIMEZONE_SHORT = "tz";
		private static final String JSON_KEY_WIFI_DATA_SCAN = "scan";
		private static final String JSON_KEY_WIFI_DATA_SCAN_SHORT = "sc";
		
		private final Mode mode;
		private final Double speed;
		
		/**
		 * This class is responsible for managing tri-axle acceleration data
		 * points.
		 * 
		 * @author John Jenkins
		 */
		public static final class AccelData {
			private final Double x;
			private final Double y;
			private final Double z;
			
			/**
			 * Creates a tri-axle acceleration data point.
			 * 
			 * @param x The x-acceleration of the point.
			 * 
			 * @param y The y-acceleration of the point.
			 * 
			 * @param z The z-acceleration of the point.
			 */
			public AccelData(final Double x, final Double y, final Double z) {
				this.x = x;
				this.y = y;
				this.z = z;
			}

			/**
			 * Returns the x-acceleration of the data point.
			 * 
			 * @return The x-acceleration of the data point.
			 */
			public final Double getX() {
				return x;
			}

			/**
			 * Returns the y-acceleration of the data point.
			 * 
			 * @return The y-acceleration of the data point.
			 */
			public final Double getY() {
				return y;
			}

			/**
			 * Returns the z-acceleration of the data point.
			 * 
			 * @return The z-acceleration of the data point.
			 */
			public final Double getZ() {
				return z;
			}
			
			/**
			 * Creates a JSONObject from the information in this object.
			 * 
			 * @return Returns a JSONObject representing this object.
			 * 
			 * @throws JSONException There was an error building the 
			 * 						 JSONObject.
			 */
			public final JSONObject toJson() 
					throws JSONException {
				
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_ACCEL_DATA_X, x);
				result.put(JSON_KEY_ACCEL_DATA_Y, y);
				result.put(JSON_KEY_ACCEL_DATA_Z, z);
				
				return result;
			}
		}
		private final List<AccelData> accelData;
		
		/**
		 * This class is responsible for the WifiData in a sensor data upload.
		 * 
		 * @author John Jenkins
		 */
		public static final class WifiData {
			private static final String JSON_KEY_SSID = "ssid";
			private static final String JSON_KEY_SSID_SHORT = "ss";
			private static final String JSON_KEY_STRENGTH = "strength";
			private static final String JSON_KEY_STRENGTH_SHORT = "st";
			
			//private final Date timestamp;
			private final Long time;
			private final TimeZone timezone;
			private final Map<String, Double> scan;
			
			/**
			 * Creates a WifiData point with a timestamp and all of the scan
			 * information.
			 * 
			 * @param time The milliseconds since epoch at which time this
			 * 			   record was made.
			 * 
			 * @param timezone The timezone of the device when this record was
			 * 				   made.
			 * 
			 * @param scan The data collected from all of the WiFi points in 
			 * 			   range of the scan.
			 * 
			 * @throws DomainException Thrown if either of the parameters are
			 * 						   null or invalid values.
			 */
			private WifiData(
					final Long time, 
					final TimeZone timezone, 
					final JSONArray scan, 
					final Mode mode) 
					throws DomainException {
				
				// Validate the timestamp value.
				if(time == null) {
					if(Mode.ERROR.equals(mode)) {
						this.time = null;
					}
					else {
						throw new DomainException(
								ErrorCode.SERVER_INVALID_TIMESTAMP, 
								"The time is missing for a WiFiData record.");
					}
				}
				else {
					this.time = time;
				}
				
				// Validate the timezone value.
				if(timezone == null) {
					if(Mode.ERROR.equals(mode)) {
						this.timezone = null;
					}
					else {
						throw new DomainException(
								ErrorCode.SERVER_INVALID_TIMESTAMP, 
								"The time is missing for a WiFiData record.");
					}
				}
				else {
					this.timezone = timezone;
				}
				
				// Validate the scan value.
				if(scan == null) {
					this.scan = null;
				}
				else {
					// Create the local scan map.
					this.scan = new HashMap<String, Double>();
	
					// For each of the entries in the array, parse out the
					// necessary information.
					int numScans = scan.length();
					for(int i = 0; i < numScans; i++) {
						try {
							JSONObject jsonObject = scan.getJSONObject(i);
							
							// Get the SSID.
							String ssid;
							try {
								ssid = jsonObject.getString(JSON_KEY_SSID);
							}
							catch(JSONException notLong) {
								try {
									ssid = jsonObject.getString(
											JSON_KEY_SSID_SHORT);
								}
								catch(JSONException notShort) {
									if(Mode.ERROR.equals(mode)) {
										continue;
									}
									else {
										throw new DomainException(
											ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
											"The SSID is missing.", 
											notShort);
									}
								}
							}
							
							// Get the strength.
							Double strength;
							try {
								strength = 
										jsonObject.getDouble(
												JSON_KEY_STRENGTH);
							}
							catch(JSONException notLong) {
								try {
									strength = 
											jsonObject.getDouble(
													JSON_KEY_STRENGTH_SHORT);
								}
								catch(JSONException notShort) {
									if(Mode.ERROR.equals(mode)) {
										strength = null;
									}
									else {
										throw new DomainException(
											ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
											"The strength is missing or invalid.", 
											notShort);
									}
								}
							}
							
							// Add them to the map.
							this.scan.put(ssid, strength);
						}
						catch(JSONException e) {
							throw new DomainException(
									"The object changed while we were reading it.", 
									e);
						}
					}
				}
			}
			
			/**
			 * Creates a WiFiData object that contains a timestamp of when the
			 * record was made and the map of WiFi device IDs to their signal
			 * strength.
			 *  
			 * @param timestamp The date and time that this record was made.
			 * 
			 * @param scans A map of WiFi device IDs to their signal strength.
			 * 
			 * @throws DomainException Thrown if the timestamp or scans map are
			 * 						   null.
			 */
			public WifiData(
					final Long time, 
					final TimeZone timezone,
					final Map<String, Double> scans) 
					throws DomainException {
				
				if(time == null) {
					throw new DomainException(
							"The timestamp cannot be null.");
				}
				if(timezone == null) {
					throw new DomainException(
							"The timezone cannot be null.");
				}
				else if(scans == null) {
					throw new DomainException(
							"The map of scans cannot be null.");
				}
				
				this.time = time;
				this.timezone = timezone;
				this.scan = scans;
			}

			/**
			 * Returns an immutable copy of the scan.
			 * 
			 * @return An immutable copy of the scan. This may be null if the 
			 * 		   mode of this point is ERROR.
			 */
			public final Map<String, Double> getScan() {
				return Collections.unmodifiableMap(scan);
			}
			
			/**
			 * Creates a JSONObject that represents the information in this
			 * object.
			 * 
			 * @return Returns a JSONObject that represents this object.
			 * 
			 * @throws JSONException There was an error building the 
			 * 						 JSONObject.
			 */
			public final JSONObject toJson(
					final boolean abbreviated) 
					throws JSONException {

				JSONObject result = new JSONObject();
				
				/*
				if(time != null) {
					result.put(
							(abbreviated) ? 
									JSON_KEY_WIFI_DATA_TIMESTAMP_SHORT :
									JSON_KEY_WIFI_DATA_TIMESTAMP, 
							TimeUtils.getIso8601DateTimeString(
									new Date(time)));
				}
				*/

				if(time != null) {
				result.put(
						(abbreviated) ?
								JSON_KEY_WIFI_DATA_TIME_SHORT :
								JSON_KEY_WIFI_DATA_TIME, 
						time);
				}
				
				if(timezone != null) {
					result.put(
							(abbreviated) ?
									JSON_KEY_WIFI_DATA_TIMEZONE_SHORT :
									JSON_KEY_WIFI_DATA_TIMEZONE, 
							timezone.getID());
				}
				
				if(scan != null) {
					JSONArray scanJson = new JSONArray();
					
					for(String ssid : scan.keySet()) {
						JSONObject currScan = new JSONObject();
						
						currScan.put(
								(abbreviated) ?
										JSON_KEY_SSID_SHORT :
										JSON_KEY_SSID, 
								ssid);
						
						currScan.put(
								(abbreviated) ?
										JSON_KEY_STRENGTH_SHORT :
										JSON_KEY_STRENGTH, 
								scan.get(ssid));

						scanJson.put(currScan);
					}
					result.put(
							(abbreviated) ?
									JSON_KEY_WIFI_DATA_SCAN_SHORT :
									JSON_KEY_WIFI_DATA_SCAN, 
							scanJson);
				}
				
				return result;
			}
		}
		private final WifiData wifiData;
		
		/**
		 * Creates a new SensorData point from a JSONObject of sensor data.
		 * 
		 * @param sensorData The sensor data as a JSONObject.
		 * 
		 * @throws DomainException Thrown if the sensor data is null, not 
		 * 						   valid sensor data, or missing some 
		 * 						   component.
		 */
		private SensorData(JSONObject sensorData) throws DomainException {
			// Get the mode string.
			String modeString;
			try {
				modeString = sensorData.getString(JSON_KEY_SENSOR_DATA_MODE);
			}
			catch(JSONException notLong) {
				try {
					modeString = 
							sensorData.getString(
									JSON_KEY_SENSOR_DATA_MODE_SHORT);
				}
				catch(JSONException notShort) {
					throw new DomainException(
							ErrorCode.MOBILITY_INVALID_MODE, 
							"The mode is missing in the sensor data: " + 
									JSON_KEY_SENSOR_DATA_MODE, 
							notShort);
				}
			}
			
			// Convert the mode string into a valid Mode.
			try {
				mode = Mode.valueOf(modeString.toUpperCase());
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_MODE, 
						"The mode is not a known mode: " + modeString, 
						e);
			}
			
			// Get the speed.
			Double tSpeed;
			try {
				tSpeed = sensorData.getDouble(JSON_KEY_SPEED);
			}
			catch(JSONException notLong) {
				try {
					tSpeed = sensorData.getDouble(JSON_KEY_SPEED_SHORT);
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tSpeed = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_SPEED, 
								"The speed is missing or invalid.", 
								notShort);
					}
				}
			}
			speed = tSpeed;

			// Get the accelerometer data.
			List<AccelData> tAccelData = null;
			JSONArray accelDataJson = null;
			try {
				accelDataJson = sensorData.getJSONArray(JSON_KEY_ACCEL_DATA);
			}
			catch(JSONException notLong) {
				try {
					accelDataJson = 
							sensorData.getJSONArray(JSON_KEY_ACCEL_DATA_SHORT);
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tAccelData = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"The accelerometer data is missing or invalid.", 
								notShort);
					}
				}
			}
			
			if(accelDataJson != null) {
				int numAccelDataPoints = accelDataJson.length();
				
				// Create the resulting list and cycle through the 
				// JSONArray adding each of the entries.
				tAccelData = new ArrayList<AccelData>(numAccelDataPoints);
				for(int i = 0; i < numAccelDataPoints; i++) {
					try {
						JSONObject accelDataPointJson = 
								accelDataJson.getJSONObject(i);
						
						// Get the x-acceleration.
						Double x;
						try {
							x = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_X);
						}
						catch(JSONException e) {
							if(Mode.ERROR.equals(mode)) {
								x = null;
							}
							else {
								throw new DomainException(
										ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
										"The 'x' point was missing or invalid.", 
										e);
							}
						}
						
						// Get the y-acceleration.
						Double y;
						try {
							y = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Y);
						}
						catch(JSONException e) {
							if(Mode.ERROR.equals(mode)) {
								y = null;
							}
							else {
								throw new DomainException(
										ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
										"The 'y' point was missing or invalid.", 
										e);
							}
						}
						
						// Get the z-acceleration.
						Double z;
						try {
							z = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Z);
						}
						catch(JSONException e) {
							if(Mode.ERROR.equals(mode)) {
								z = null;
							}
							else {
								throw new DomainException(
										ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
										"The 'z' point was missing or invalid.", 
										e);
							}
						}
						
						// Add a new point.
						tAccelData.add(new AccelData(x, y, z));
					}
					catch(JSONException e) {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"An accelerometer data point is not a JSONObject.", 
								e);
					}
				}
			}
			accelData = tAccelData;
			
			// Get the WiFi data.
			WifiData tWifiData = null;
			JSONObject wifiDataJson = null;
			try {
				wifiDataJson = sensorData.getJSONObject(JSON_KEY_WIFI_DATA);
			}
			catch(JSONException notLong) {
				try {
					wifiDataJson = 
							sensorData.getJSONObject(JSON_KEY_WIFI_DATA_SHORT);
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tWifiData = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
								"The WiFi data is missing or invalid.", 
								notShort);
					}
				}
			}
			
			// If the WiFi data was found.
			if(wifiDataJson != null) {
				// Get the time and timezone.
				Long time;
				boolean timeFound = true;
				try {
					time = wifiDataJson.getLong(JSON_KEY_WIFI_DATA_TIME);
				}
				catch(JSONException noLongTime) {
					try {
						time = wifiDataJson.getLong(
								JSON_KEY_WIFI_DATA_TIME_SHORT);
					}
					catch(JSONException noTime) {
						timeFound = false;
						String timestamp = null;
						try {
							timestamp = 
									wifiDataJson.getString(
											JSON_KEY_WIFI_DATA_TIMESTAMP);
						}
						catch(JSONException noLongTimestamp) {	
							try {
								timestamp =
										wifiDataJson.getString(
												JSON_KEY_WIFI_DATA_TIMESTAMP_SHORT);
							}
							catch(JSONException noShortTimestamp) {
								if(Mode.ERROR.equals(mode)) {
									time = null;
								}
								else {
									throw new DomainException(
											ErrorCode.SERVER_INVALID_TIME, 
											"The time is missing.", 
											noShortTimestamp);
								}
							}
						}

						Date date = StringUtils.decodeDateTime(timestamp);
						if(date == null) {
							if(Mode.ERROR.equals(mode)) {
								time = null;
							}
							else {
								throw new DomainException(
										ErrorCode.SERVER_INVALID_TIMESTAMP,
										"The timestamp could not be decoded: " +
											sensorData.toString());
							}
						}
						else {
							time = date.getTime();
						}
					}
				}
				
				TimeZone timezone;
				try {
					timezone = 
							TimeZone.getTimeZone(
									wifiDataJson.getString(
											JSON_KEY_WIFI_DATA_TIMEZONE));
				}
				catch(JSONException noLongTimezone) {
					try {
						timezone =
								TimeZone.getTimeZone(
										wifiDataJson.getString(
												JSON_KEY_WIFI_DATA_TIMEZONE_SHORT));
					}
					catch(JSONException noShortTimezone) {
						if(Mode.ERROR.equals(mode)) {
							timezone = null;
						}
						else if(! timeFound) {
							timezone = TimeZone.getDefault();
						}
						else {
							throw new DomainException(
									ErrorCode.SERVER_INVALID_TIMEZONE, 
									"The timezone is missing.", 
									noShortTimezone);
						}
					}
				}
				
				// Get the scan.
				JSONArray scan;
				try {
					scan = wifiDataJson.getJSONArray(JSON_KEY_WIFI_DATA_SCAN);
				}
				catch(JSONException e) {
					if(Mode.ERROR.equals(mode)) {
						scan = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
								"The scan is missing.", 
								e);
					}
				}
					
					
				// Set the WifiData.
				tWifiData = new WifiData(time, timezone, scan, mode);
			}
			wifiData = tWifiData;
		}

		/**
		 * Returns the Mode for this record.
		 * 
		 * @return The record's mode.
		 */
		public final Mode getMode() {
			return mode;
		}

		/**
		 * Returns the speed for this record.
		 * 
		 * @return The record's speed.
		 */
		public final Double getSpeed() {
			return speed;
		}

		/**
		 * Returns all of the AccelData points for this record.
		 * 
		 * @return All of the record's AccelData points.
		 */
		public final List<AccelData> getAccelData() {
			return accelData;
		}

		/**
		 * Returns the WifiData from this record.
		 * 
		 * @return The record's WifiData. This may be null if the record did
		 * 		   not include this information.
		 */
		public final WifiData getWifiData() {
			return wifiData;
		}
		
		/**
		 * Creates a JSONObject that represents the information in this object.
		 * 
		 * @return Returns a JSONObject that represents this object.
		 * 
		 * @throws JSONException There was an error building the JSONObject.
		 */
		public final JSONObject toJson(
				final boolean abbreviated) 
				throws JSONException {
			
			JSONObject result = new JSONObject();
			
			result.put(
					(abbreviated) ?
							JSON_KEY_SENSOR_DATA_MODE_SHORT :
							JSON_KEY_SENSOR_DATA_MODE, 
							mode.name().toLowerCase());
			
			if(speed == null) {
				// Don't put it in the JSON.
			}
			else if(speed.isInfinite()) {
				if(Double.POSITIVE_INFINITY == speed.doubleValue()) {
					result.put(
							(abbreviated) ?
									JSON_KEY_SPEED_SHORT :
									JSON_KEY_SPEED, 
							"Infinity");
				}
				else {
					result.put(
							(abbreviated) ?
									JSON_KEY_SPEED_SHORT :
									JSON_KEY_SPEED, 
							"-Infinity");
				}
			}
			else if(speed.isNaN()) {
				result.put(
						(abbreviated) ?
								JSON_KEY_SPEED_SHORT :
								JSON_KEY_SPEED, 
						"NaN");
			}
			else {
				result.put(
						(abbreviated) ?
								JSON_KEY_SPEED_SHORT :
								JSON_KEY_SPEED, 
						speed);
			}
			
			if(wifiData == null) {
				result.put(
						(abbreviated) ?
								JSON_KEY_WIFI_DATA_SHORT :
								JSON_KEY_WIFI_DATA,
						new JSONObject());
			}
			else {
				result.put(
						(abbreviated) ?
								JSON_KEY_WIFI_DATA_SHORT :
								JSON_KEY_WIFI_DATA,
						wifiData.toJson(abbreviated));
			}
			
			if(accelData == null) {
				// Don't put it in the JSON.
			}
			else {
				JSONArray accelArray = new JSONArray();
				for(AccelData accelRecord : accelData) {
					accelArray.put(accelRecord.toJson());
				}
				result.put(
						(abbreviated) ?
								JSON_KEY_ACCEL_DATA_SHORT :
								JSON_KEY_ACCEL_DATA, 
						accelArray);
			}
			
			return result;
		}
	}
	private final SensorData sensorData;
	
	/**
	 * This is the classification information computed by the server's 
	 * classifier.
	 * 
	 * @author John Jenkins
	 */
	public final class ClassifierData {
		private static final String JSON_KEY_FFT = "fft";
		private static final String JSON_KEY_VARIANCE = "variance";
		
		//private static final String JSON_KEY_N95_FFT = "N95Fft";
		private static final String JSON_KEY_N95_VARIANCE = "N95Variance";
		
		private static final String JSON_KEY_AVERAGE = "average";
		private static final String JSON_KEY_MODE = "mode";
		
		private final List<Double> fft;
		private final Double variance;
		
		// This is no longer being collected, but it is being left here as a
		// reminder in case it is added again.
		//private final List<Double> n95Fft;
		private final Double n95Variance;
		
		private final Double average;
		private final Mode mode;
		
		/**
		 * Creates a ClassifierData object that only contains a mode.
		 * 
		 * @param mode The Mode from the server-side classifier.
		 * 
		 * @throws DomainException The mode is null.
		 */
		private ClassifierData(Mode mode) throws DomainException {
			if(mode == null) {
				throw new DomainException("The mode cannot be null.");
			}
			
			this.fft = null;
			this.variance = null;
			this.n95Variance = null;
			this.average = null;
			this.mode = mode;
		}
		
		/**
		 * Creates a ClassifierData object that contains all of the applicable
		 * keys from the JSONObject.
		 * 
		 * @param mode The mode of the Mobility point as retrieved before the
		 * 			   classifier information was retrieved.
		 * 
		 * @param classifierData The classifier data as a JSONObject.
		 * 
		 * @throws DomainException Thrown if the mode is missing or unknown.
		 */
		private ClassifierData(
				final Mode mode, 
				final JSONObject classifierData) 
				throws DomainException {
			
			Mode tMode;
			try {
				tMode = Mode.valueOf(classifierData.getString(JSON_KEY_MODE).toUpperCase());
			}
			catch(JSONException e) {
				if(Mode.ERROR.equals(mode)) {
					tMode = null;
				}
				else {
					throw new DomainException(
							ErrorCode.MOBILITY_INVALID_MODE, 
							"The mode is missing.", 
							e);
				}
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_MODE, 
						"The mode is unknown.", 
						e);
			}
			this.mode = tMode;
			
			List<Double> tFft = null;
			try {
				JSONArray fftArray = classifierData.getJSONArray(JSON_KEY_FFT);
				
				int numEntries = fftArray.length();
				tFft = new ArrayList<Double>(numEntries);
				for(int i = 0; i < numEntries; i++) {
					tFft.add(fftArray.getDouble(i));
				}
			}
			catch(JSONException e) {
				// If it is missing we don't care. It may be that only the mode
				// could be calculated.
			}
			fft = tFft;
			
			Double tVariance = null;
			try {
				tVariance = classifierData.getDouble(JSON_KEY_VARIANCE);
			}
			catch(JSONException e) {
				// If it is missing we don't care. It may be that only the mode
				// could be calculated.
			}
			variance = tVariance;
			
			Double tN95Variance = null;
			try {
				tN95Variance = classifierData.getDouble(JSON_KEY_N95_VARIANCE);
			}
			catch(JSONException e) {
				// If it is missing we don't care. It may be that only the mode
				// could be calculated.
			}
			n95Variance = tN95Variance;
			
			Double tAverage = null;
			try {
				tAverage = classifierData.getDouble(JSON_KEY_AVERAGE);
			}
			catch(JSONException e) {
				// If it is missing we don't care. It may be that only the mode
				// could be calculated.
			}
			average = tAverage;
		}
		
		/**
		 * Builds a ClassifierData object from the given data.
		 * 
		 * @param fft The FFT from the server's classifier.
		 * 
		 * @param variance The variance from the server's classifier.
		 * 
		 * @param n95Variance The variance from the server's classifier from 
		 * 					  the N95 data.
		 * 
		 * @param average The average of the Samples.
		 * 
		 * @param mode The Mode calculated by the server's classifier.
		 * 
		 * @throws DomainException The mode is null.
		 */
		private ClassifierData(
				final List<Double> fft, 
				final Double variance,
				final Double n95Variance,
				final Double average, 
				final Mode mode) 
				throws DomainException{
			
			if(mode == null) {
				throw new DomainException("The mode cannot be null.");
			}
			
			this.fft = fft;
			this.variance = variance;
			this.n95Variance = n95Variance;
			this.average = average;
			this.mode = mode;
		}

		/**
		 * Returns the FFT array.
		 * 
		 * @return The FFT array. May be null.
		 */
		public final List<Double> getFft() {
			return fft;
		}

		/**
		 * Returns the variance.
		 * 
		 * @return The variance. May be null.
		 */
		public final Double getVariance() {
			return variance;
		}

		/**
		 * Returns the N95 variance.
		 * 
		 * @return The N95 variance. May be null.
		 */
		public final Double getN95Variance() {
			return n95Variance;
		}

		/**
		 * Returns the average.
		 * 
		 * @return The average. May be null.
		 */
		public final Double getAverage() {
			return average;
		}

		/**
		 * Returns the Mode from this classification.
		 * 
		 * @return The Mode from this classification.
		 */
		public final Mode getMode() {
			return mode;
		}
		
		/**
		 * Creates a JSONObject that represents the information contained in 
		 * this object.
		 * 
		 * @return A JSONObject representing this object.
		 * 
		 * @throws JSONException There was an error building the JSONObject.
		 */
		public final JSONObject toJson() throws JSONException {
			JSONObject result = new JSONObject();
			
			if(mode != null) {
				result.put(JSON_KEY_MODE, mode.name().toLowerCase());
			}
			
			result.put(JSON_KEY_FFT, fft);
			result.put(JSON_KEY_VARIANCE, variance);
			
			result.put(JSON_KEY_N95_VARIANCE, n95Variance);
			
			result.put(JSON_KEY_AVERAGE, average);
			
			return result;
		}
	}
	private ClassifierData classifierData;
	
	/**
	 * Creates a Mobility object that represents all of the information in the 
	 * parameterized Mobility data point.
	 * 
	 * @param mobilityPoint A JSONObject that contains all of the required
	 * 						information for a Mobility data point.
	 * 
	 * @param privacyState The privacy state of the Mobility point.
	 * 
	 * @throws DomainException Thrown if Mobility point is null, invalid, or
	 * 						   contains insufficient information to build 
	 * 						   this object.
	 */
	public MobilityPoint(
			final JSONObject mobilityPoint, 
			final PrivacyState privacyState) 
			throws DomainException {
		
		try {
			id = UUID.fromString(mobilityPoint.getString(JSON_KEY_ID));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.MOBILITY_INVALID_ID, 
					"The Mobility point's ID is missing.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.MOBILITY_INVALID_ID, 
					"The Mobility point's ID is not a valid UUID.", 
					e);
		}
		
		// Get the time.
		long tTime;
		try {
			tTime = mobilityPoint.getLong(JSON_KEY_TIME);
		}
		catch(JSONException outerException) {
			try {
				tTime = mobilityPoint.getLong(JSON_KEY_TIME_SHORT);
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIME, 
						"The time is missing.", 
						innerException);
			}
		}
		time = tTime;
		
		// Get the timezone.
		// FIXME: This doesn't validate the timezone and instead just defaults
		// to GMT.
		TimeZone tTimezone;
		try {
			tTimezone = TimeZone.getTimeZone(mobilityPoint.getString(JSON_KEY_TIMEZONE));
		}
		catch(JSONException outerException) {
			try {
				tTimezone = TimeZone.getTimeZone(mobilityPoint.getString(JSON_KEY_TIMEZONE_SHORT));
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMEZONE, 
						"The timezone is missing.", 
						innerException);
			}
		}
		timezone = tTimezone;
		
		// Get the location status.
		LocationStatus tLocationStatus;
		try {
			tLocationStatus = LocationStatus.valueOf(mobilityPoint.getString(JSON_KEY_LOCATION_STATUS).toUpperCase());
		}
		catch(JSONException outerException) {
			try {
				tLocationStatus = LocationStatus.valueOf(mobilityPoint.getString(JSON_KEY_LOCATION_STATUS_SHORT).toUpperCase());
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
						"The location status is missing.", innerException);
			}
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"The location status is unknown.", 
					e);
		}
		locationStatus = tLocationStatus;
		
		// Get the location.
		Location tLocation;
		try {
			tLocation = new Location(mobilityPoint.getJSONObject(JSON_KEY_LOCATION));
		}
		catch(JSONException outerException) {
			try {
				tLocation = new Location(mobilityPoint.getJSONObject(JSON_KEY_LOCATION_SHORT));
			}
			catch(JSONException innerException) {
				// If there was no location information in the JSONObject, 
				// check to ensure that the location status was unavailable as
				// that is the only time this is acceptable.
				if(LocationStatus.UNAVAILABLE.equals(locationStatus)) {
					tLocation = null;
				}
				else {
					throw new DomainException(
							ErrorCode.SERVER_INVALID_LOCATION, 
							"The location is missing.", 
							innerException);
				}
			}
		}
		location = tLocation;
		
		// Get the subtype.
		String subTypeString;
		try {
			subTypeString = 
					mobilityPoint.getString(JSON_KEY_SUBTYPE).toUpperCase();
		}
		catch(JSONException notLong) {
			try {
				subTypeString = 
						mobilityPoint
							.getString(JSON_KEY_SUBTYPE_SHORT)
							.toUpperCase();
			}
			catch(JSONException notShort) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_SUBTYPE, 
						"The subtype is missing.", 
						notShort);
			}
		}
		
		try {
			subType = SubType.valueOf(subTypeString);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.MOBILITY_INVALID_SUBTYPE, 
					"The subtype is an unknown type.", 
					e);
		}
		
		// Based on the subtype, get the mode or sensor data.
		switch(subType) {
		case MODE_ONLY:
			String modeString;
			try {
				modeString = mobilityPoint.getString(JSON_KEY_MODE);
			}
			catch(JSONException outerException) {
				try {
					modeString = mobilityPoint.getString(JSON_KEY_MODE_SHORT);
				}
				catch(JSONException innerException) {
					throw new DomainException(
							ErrorCode.MOBILITY_INVALID_MODE, 
							"The subtype is '" + 
								SubType.MODE_ONLY.toString().toLowerCase() + 
								"', but the required key is missing: " + 
								JSON_KEY_MODE, 
							innerException);
				}
			}

			Mode tMode;
			try {
				tMode = Mode.valueOf(modeString.toUpperCase());
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_MODE, 
						"The mode is unknown: " + modeString, 
						e);
			}

			mode = tMode;
			sensorData = null;
			break;
			
		case SENSOR_DATA:
			try {
				sensorData = new SensorData(mobilityPoint.getJSONObject(JSON_KEY_DATA));
				mode = sensorData.mode;
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_JSON, 
						"The subtype is '" + SubType.SENSOR_DATA.toString().toLowerCase() + "', but the required key is missing: " + JSON_KEY_DATA,
						e);
			}
			break;
			
		default:
			// If we accepted the subtype then we must know what it is, but we
			// may not need to look for any further data based on that subtype.
			mode = null;
			sensorData = null;
		}
		
		// Set the server's classification to null.
		classifierData = null;
		
		this.privacyState = privacyState;
	}
	
	/**
	 * Creates a new MobilityPoint object that represents a Mobility data
	 * point based on the parameters. If it is a mode-only point, set sensor
	 * data, features, and classifier version to null.
	 * 
	 * @param id The Mobility point's universally unique identifier.
	 * 
	 * @param time The milliseconds since the epoch at which time this point
	 * 			   was created.
	 * 
	 * @param timezone The timezone of the device that created this point at 
	 * 				   time it was created.
	 * 
	 * @param locationStatus The location status of this point.
	 * 
	 * @param location The location of this point which may be null if the
	 * 				   location status correlates.
	 * 
	 * @param mode The user's mode when this point was created.
	 * 
	 * @param privacyState The privacy state of this point.
	 * 
	 * @param sensorData The optional sensor data that may have additionally
	 * 					 been collected at the time this point was created.
	 * 
	 * @param features The optional feature information calculated by the 
	 * 				   server once the point was uploaded.
	 * 
	 * @param classifierVersion The version of the classifier that was used to
	 * 							generate the 'features'.
	 * 
	 * @throws MobilityException Thrown if any of the required parameters are	
	 * 							 missing or any of the parameters are invalid.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or if any of the parameters are invalid.
	 */
	public MobilityPoint(
			final UUID id, 
			final Long time, 
			final TimeZone timezone,
			final LocationStatus locationStatus, 
			final JSONObject location, 
			final Mode mode, 
			final PrivacyState privacyState, 
			final JSONObject sensorData, 
			final JSONObject features, 
			final String classifierVersion) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID cannot be null.");
		}
		else {
			this.id = id;
		}
		
		if(time == null) {
			throw new DomainException("The time cannot be null.");
		}
		else {
			this.time = time;
		}
		
		if(timezone == null) {
			throw new DomainException("The timezone cannot be null.");
		}
		else {
			this.timezone = timezone;
		}
		
		if(locationStatus == null) {
			throw new DomainException("The location status cannot be null.");
		}
		else {
			this.locationStatus = locationStatus;
		}
		
		if(location == null) {
			this.location = null;
		}
		else {
			this.location = new Location(location);
		}
		
		if(mode == null) {
			throw new DomainException("The mode cannot be null.");
		}
		else {
			this.mode = mode;
		}
		
		if(privacyState == null) {
			throw new DomainException("The privacy state cannot be null.");
		}
		else {
			this.privacyState = privacyState;
		}
		
		if((sensorData == null) && (features == null) && (classifierVersion == null)) {
			subType = SubType.MODE_ONLY;
			
			this.sensorData = null;
			this.classifierData = null;
		}
		else {
			subType = SubType.SENSOR_DATA;
			
			this.sensorData = new SensorData(sensorData);
			this.classifierData = new ClassifierData(this.mode, features);
		}
	}
	
	/**
	 * Creates a new MobilityPoint object.
	 * 
	 * @param id The Mobility point's universally unique identifier.
	 * 
	 * @param time The milliseconds since epoch that this reading was made.
	 * 
	 * @param timezone The time zone of the device that is making this reading.
	 * 
	 * @param locationStatus The status of the location object.
	 * 
	 * @param location The location of the device that is making this reading.
	 * 				   This may be null if the location is unknown.
	 * 
	 * @param mode The mode as determined by Mobility.
	 * 
	 * @param sensorData The sensor data that was taken to generate the mode.
	 * 					 This may be null if this data is not being captured.
	 * 
	 * @throws DomainException Thrown if any of the required 
	 * 									parameters are missing.
	 */
	public MobilityPoint(
			final UUID id, 
			final Long time, 
			final TimeZone timezone,
			final LocationStatus locationStatus, 
			final Location location, 
			final Mode mode, 
			final SensorData sensorData) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID cannot be null.");
		}
		else {
			this.id = id;
		}
		
		if(time == null) {
			throw new DomainException("The time cannot be null.");
		}
		else {
			this.time = time;
		}
		
		if(timezone == null) {
			throw new DomainException("The timezone cannot be null.");
		}
		else {
			this.timezone = timezone;
		}
		
		if(locationStatus == null) {
			throw new DomainException("The location status cannot be null.");
		}
		else {
			this.locationStatus = locationStatus;
		}
		
		if((! LocationStatus.UNAVAILABLE.equals(locationStatus)) &&
				(location == null)) {
			throw new DomainException(
					"The location cannot be null if the location status is not '" + 
					LocationStatus.UNAVAILABLE.toString().toLowerCase() + "'.");
		}
		else {
			this.location = location;
		}
		
		if(mode == null) {
			throw new DomainException("The mode cannot be null.");
		}
		else {
			this.mode = mode;
		}
		
		if(sensorData == null) {
			subType = SubType.MODE_ONLY;
			
			this.sensorData = null;
			this.classifierData = null;
		}
		else {
			subType = SubType.SENSOR_DATA;
			
			this.sensorData = sensorData;
			this.classifierData = null;
		}
		
		privacyState = PrivacyState.PRIVATE;
	}
	
	/**
	 * Returns the point's universally unique identifier.
	 * 
	 * @return The point's universally unique identifier.
	 */
	public final UUID getId() {
		return id;
	}

	/**
	 * Returns the number of milliseconds since the epoch when this Mobility
	 * point was created.
	 * 
	 * @return The number of milliseconds since the epoch when this Mobility
	 * 		   point was created.
	 */
	public final long getTime() {
		return time;
	}

	/**
	 * Returns the phone's timezone when this Mobility point was created.
	 * 
	 * @return The phone's timezone when this Mobility point was created.
	 */
	public final TimeZone getTimezone() {
		return timezone;
	}

	/**
	 * Returns the Mobility point's privacy state.
	 * 
	 * @return The Mobility point's privacy state.
	 */
	public final PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns the status of the location value obtained when this Mobility
	 * point was created.
	 * 
	 * @return The status of the location value obtained when this Mobility
	 * 		   point was created.
	 * 
	 * @see #getLocation()
	 */
	public final LocationStatus getLocationStatus() {
		return locationStatus;
	}

	/**
	 * Returns a Location object representing the information collected about
	 * the phone's location when this Mobility point was created. 
	 * 
	 * @return A Location object representing the information collected about
	 * 		   the phone's location when this Mobility point was created.
	 * 
	 * @see #getLocationStatus()
	 */
	public final Location getLocation() {
		return location;
	}

	/**
	 * Returns the SubType of this Mobility reading which dictates whether the
	 * mode or the sensor data is valid.
	 * 
	 * @return The SubType of this Mobility reading which dictates whether the
	 * 		   mode or the sensor data is valid.
	 * 
	 * @see SubType
	 * @see #getMode()
	 * @see #getSensorData()
	 */
	public final SubType getSubType() {
		return subType;
	}

	/**
	 * This is the Mode that was calculated when this Mobility point was made.
	 * 
	 * @return The Mode that was calculated when this Mobility point was made.
	 */
	public final Mode getMode() {
		return mode;
	}

	/**
	 * Returns the additional sensor data that was recorded when this Mobility
	 * point was made. This will be null if the {@link #getSubType()} is not
	 * {@link SubType#SENSOR_DATA}.
	 * 
	 * @return The additional sensor data that was recorded when this Mobility
	 * 		   point was made or null if {@link #getSubType()} does not return
	 * 		   {@link SubType#SENSOR_DATA}.
	 */
	public final SensorData getSensorData() {
		return sensorData;
	}
	
	/**
	 * Returns the list of Samples from this Mobility point. This is only valid
	 * to call if the sub-type of this point is {@link SubType#SENSOR_DATA} and
	 * the mode is not {@link Mode#ERROR}.
	 * 
	 * @return A list of Samples from this Mobility point.
	 * 
	 * @throws DomainException The sub-type of this point is not 
	 * 						   {@link SubType#SENSOR_DATA} or the mode is
	 * 						   {@link Mode#ERROR}.
	 */
	public final List<Sample> getSamples() throws DomainException {
		if(! SubType.SENSOR_DATA.equals(subType)) {
			throw new DomainException(
					"There are no samples for Mobility points that are not of the subtype " + 
					SubType.SENSOR_DATA.toString());
		}
		if(Mode.ERROR.equals(mode)) {
			throw new DomainException(
					"There are no samples for Mobility points that are of the mode " + 
					Mode.ERROR.toString());
		}
		
		List<Sample> result = new ArrayList<Sample>(sensorData.accelData.size());
		for(AccelData dataPoint : sensorData.accelData) {
			Sample sample = new Sample();
			sample.setX(dataPoint.getX());
			sample.setY(dataPoint.getY());
			sample.setZ(dataPoint.getZ());
			result.add(sample);
		}
		return result;
	}
	
	/**
	 * Sets this Mobility point's classifier data from the server's classifier.
	 * 
	 * @param fft The FFT from the server's classifier.
	 * 
	 * @param variance The variance from the server's classifier.
	 * 
	 * @param n95Variance The variance from the server's classifier from 
	 * 					  the N95 data.
	 * 
	 * @param average The average of the Samples.
	 * 
	 * @param mode The Mode calculated by the server's classifier.
	 * 
	 * @throws DomainException Thrown if any of the values are null.
	 */
	public final void setClassifierData(
			final List<Double> fft,
			final Double variance,
			final Double n95Variance,
			final Double average, 
			final Mode mode) 
			throws DomainException {
			
		classifierData = 
				new ClassifierData(fft, variance, n95Variance, average, mode);
	}
	
	/**
	 * Sets this Mobility point's classifier data from the server's classifier,
	 * but it only sets the mode.
	 * 
	 * @param mode The mode that the classifier generated.
	 * 
	 * @throws DomainException The mode is null.
	 */
	public final void setClassifierModeOnly(Mode mode) throws DomainException {
		classifierData = new ClassifierData(mode);
	}
	
	/**
	 * Returns the classifier data that was generated by the server's 
	 * classifier.
	 * 
	 * @return The classifier data that was generated by the server's 
	 * 		   classifier or null if nothing has been set yet. 
	 */
	public final ClassifierData getClassifierData() {
		return classifierData;
	}
	
	/**
	 * Outputs this Mobility point as a JSONObject.
	 * 
	 * @param abbreviated Whether or not to use the abbreviated versions of the
	 * 					  JSON keys.
	 * 
	 * @param withData Whether or not to include only the mode or to include
	 * 				   whatever the subtype defines.
	 * 
	 * @return A JSONObject that represents this Mobility point.
	 * 
	 * @throws JSONException There was an error creating this JSONObject.
	 */
	public final JSONObject toJson(
			final boolean abbreviated, 
			final boolean withData) 
			throws JSONException {
		
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_ID, id.toString());
		result.put(((abbreviated) ? JSON_KEY_TIMESTAMP_SHORT : JSON_KEY_TIMESTAMP), TimeUtils.getIso8601DateTimeString(new Date(time)));
		result.put(((abbreviated) ? JSON_KEY_TIMEZONE_SHORT : JSON_KEY_TIMEZONE), timezone.getID());
		result.put(((abbreviated) ? JSON_KEY_TIME_SHORT : JSON_KEY_TIME), time);
		
		result.put(((abbreviated) ? JSON_KEY_LOCATION_STATUS_SHORT : JSON_KEY_LOCATION_STATUS), locationStatus.toString().toLowerCase());
		if(location != null) {
			result.put(((abbreviated) ? JSON_KEY_LOCATION_SHORT : JSON_KEY_LOCATION), location.toJson(abbreviated));
		}
		
		result.put(((abbreviated) ? JSON_KEY_MODE_SHORT : JSON_KEY_MODE), mode.toString().toLowerCase());
		
		if(withData) {
			// Subtype
			result.put(((abbreviated) ? JSON_KEY_SUBTYPE_SHORT : JSON_KEY_SUBTYPE), subType.toString().toLowerCase());
			
			// If subtype is mode-only, then just the mode.
			if(SubType.MODE_ONLY.equals(subType)) {
				result.put(((abbreviated) ? JSON_KEY_MODE_SHORT : JSON_KEY_MODE), mode.toString().toLowerCase());
			}
			// If subtype is sensor-data, then a data object.
			else if(SubType.SENSOR_DATA.equals(subType)) {
				result.put(JSON_KEY_DATA, sensorData.toJson(abbreviated));
			}
		}
		
		return result;
	}
}
