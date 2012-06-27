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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream.MetaData;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.MobilityPoint.ClassifierData.ClassifierDataColumnKey;
import org.ohmage.domain.MobilityPoint.SensorData.AccelData;
import org.ohmage.domain.MobilityPoint.SensorData.AccelData.AccelDataColumnKey;
import org.ohmage.domain.MobilityPoint.SensorData.SensorDataColumnKey;
import org.ohmage.domain.MobilityPoint.SensorData.WifiData.WifiDataColumnKey;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

import edu.ucla.cens.mobilityclassifier.AccessPoint;
import edu.ucla.cens.mobilityclassifier.Sample;
import edu.ucla.cens.mobilityclassifier.WifiScan;

/**
 * This class is responsible for individual Mobility data points.
 * 
 * @author John Jenkins
 */
public class MobilityPoint implements Comparable<MobilityPoint> {
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Column names for Mobility information.
	 * 
	 * @author John Jenkins
	 */
	public static enum MobilityColumnKey implements ColumnKey {
		/**
		 * The unique identifier for the Mobility point.
		 */
		ID ("id", "id"),
		/**
		 * The time at which the Mobility point was generated.
		 */
		TIME ("time", "t"),
		/**
		 * The time stamp of the Mobility point.
		 */
		TIMESTAMP ("timestamp", "ts"),
		/**
		 * The time zone of the device.
		 */
		TIMEZONE ("timezone", "tz"),
		/**
		 * The location of the device.
		 */
		LOCATION ("location", "l"),
		/**
		 * The sub-type of the Mobility point.
		 */
		SUB_TYPE ("subtype", "st"),
		/**
		 * The sensor data.
		 */
		SENSOR_DATA ("sensor_data", "data"),
		/**
		 * The classifier data.
		 */
		CLASSIFIER_DATA ("classifier_data", "cd"),
		/**
		 * The mode of the Mobility point.
		 */
		MODE ("mode", "m");
		
		/**
		 * The string that may be optionally be placed before a key to better
		 * namespace it.
		 */
		public static final String NAMESPACE = "mobility";
		
		/**
		 * A pre-built, unmodifiable list that contains all of the 
		 * LocationColumnKey keys.
		 */
		public static final List<ColumnKey> ALL_COLUMNS;
		static {
			List<ColumnKey> keys = new ArrayList<ColumnKey>();
			
			keys.add(ID);
			keys.add(MODE);
			keys.add(TIME);
			keys.add(TIMESTAMP);
			keys.add(TIMEZONE);
			keys.add(SUB_TYPE);
			keys.addAll(LocationColumnKey.ALL_COLUMNS);
			keys.addAll(SensorDataColumnKey.ALL_COLUMNS);
			keys.addAll(ClassifierDataColumnKey.ALL_COLUMNS);
				
			ALL_COLUMNS = Collections.unmodifiableList(keys);
		}
		
		private final String key;
		private final String abbreviatedKey;
		
		/**
		 * Creates a MobilityColumnKey object with the human-readable and
		 * abbreviated versions of the key.
		 * 
		 * @param key The long, human-readable name for this key.
		 * 
		 * @param abbreviatedKey A short abbreviation for this key.
		 */
		private MobilityColumnKey(
				final String key, 
				final String abbreviatedKey) {
			
			this.key = key;
			this.abbreviatedKey = abbreviatedKey;
		}
		
		/**
		 * Converts this key to a string with the {@link #NAMESPACE} before it.
		 * 
		 * @return The {@link #NAMESPACE} and 
		 * 		   {@link ColumnKey#NAMESPACE_DIVIDOR} followed by this key's 
		 * 		   value.
		 */
		@Override
		public String toString() {
			return NAMESPACE + NAMESPACE_DIVIDOR + key;
		}
		
		/**
		 * Returns this key as a human-readable or abbreviated string.
		 * 
		 * @param abbreviated Whether or not to return an abbreviated version
		 * 					  of this key.
		 * 
		 * @return This key as a human-readable or abbreviated string.
		 */
		public String toString(final boolean abbreviated) {
			return (abbreviated) ? abbreviatedKey : key;
		}
		
		/**
		 * Converts a string, either the human-readable version or the 
		 * abbreviated version, to its MobilityColumnKey object.
		 *  
		 * @param value The string value to convert.
		 * 
		 * @return The MobilityColumnKey that represents this object.
		 * 
		 * @throws IllegalArgumentException The string could not be converted 
		 * 									into a MobilityColumnKey object.
		 */
		public static List<ColumnKey> valueOfString(final String value) {
			if(value == null) {
				throw new IllegalArgumentException("The value is null.");
			}
			
			String sanitizedValue = value.trim().toLowerCase();
			if(NAMESPACE.equals(sanitizedValue)) {
				return ALL_COLUMNS;
			}
			
			if(sanitizedValue.startsWith(NAMESPACE + NAMESPACE_DIVIDOR)) {
				sanitizedValue = 
						value.substring(
								NAMESPACE.length() +
								NAMESPACE_DIVIDOR.length());
			}
			
			for(MobilityColumnKey currKey : values()) {
				if(currKey.key.equals(sanitizedValue) ||
						currKey.abbreviatedKey.equals(sanitizedValue)) {

					if(LOCATION.equals(currKey)) {
						return LocationColumnKey.ALL_COLUMNS;
					}
					else if(SENSOR_DATA.equals(currKey)) {
						return SensorDataColumnKey.ALL_COLUMNS;
					}
					else {
						List<ColumnKey> result = new ArrayList<ColumnKey>(1);
						result.add(currKey);
						return result;
					}
				}
			}
			
			throw new IllegalArgumentException("Unknown column key.");
		}
	}

	private final UUID id;
	private final long time;
	private final DateTimeZone timezone;
	
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
		/**
		 * Column names for sensor data information.
		 * 
		 * @author John Jenkins
		 */
		public static enum SensorDataColumnKey implements ColumnKey {
			/**
			 * The mode from this sensor data column key.
			 */
			MODE ("mode", "m"),
			/**
			 * The speed from this sensor data information.
			 */
			SPEED ("speed", "sp"),
			/**
			 * 
			 */
			ACCELEROMETER_DATA ("accel_data", "ad"),
			/**
			 * 
			 */
			WIFI_DATA ("wifi_data", "wd");
			
			/**
			 * The string that may be optionally be placed before a key to 
			 * better namespace it.
			 */
			public static final String NAMESPACE = "sensor";
			
			/**
			 * A pre-built, unmodifiable list that contains all of the 
			 * LocationColumnKey keys.
			 */
			public static final List<ColumnKey> ALL_COLUMNS;
			static {
				List<ColumnKey> keys = new ArrayList<ColumnKey>();
				keys.add(MODE);
				keys.add(SPEED);
				keys.addAll(AccelDataColumnKey.ALL_COLUMNS);
				keys.addAll(WifiDataColumnKey.ALL_COLUMNS);
				
				ALL_COLUMNS = Collections.unmodifiableList(keys);
			}

			private final String key;
			private final String abbreviatedKey;
			
			/**
			 * Creates a SensorDataColumnKey object with the human-readable and
			 * abbreviated versions of the key.
			 * 
			 * @param key The long, human-readable name for this key.
			 * 
			 * @param abbreviatedKey A short abbreviation for this key.
			 */
			private SensorDataColumnKey(
					final String key, 
					final String abbreviatedKey) {
				
				this.key = key;
				this.abbreviatedKey = abbreviatedKey;
			}
			
			/**
			 * Converts this key to a string with the {@link #NAMESPACE} before it.
			 * 
			 * @return The {@link #NAMESPACE} and 
			 * 		   {@link ColumnKey#NAMESPACE_DIVIDOR} followed by this 
			 * 		   key's value.
			 */
			@Override
			public String toString() {
				return NAMESPACE + NAMESPACE_DIVIDOR + key;
			}
			
			/**
			 * Returns this key as a human-readable or abbreviated string.
			 * 
			 * @param abbreviated Whether or not to return an abbreviated 
			 * 					  version of this key.
			 * 
			 * @return This key as a human-readable or abbreviated string.
			 */
			public String toString(final boolean abbreviated) {
				return (abbreviated) ? abbreviatedKey : key;
			}
			
			/**
			 * Converts a string, either the human-readable version or the 
			 * abbreviated version, to its MobilityColumnKey object.
			 *  
			 * @param value The string value to convert.
			 * 
			 * @return The ColumnKey that represents this object.
			 * 
			 * @throws IllegalArgumentException The string could not be 
			 * 									converted into a 
			 * 									SensorDataColumnKey object or 
			 * 									any of the inner-class objects.
			 */
			public static List<ColumnKey> valueOfString(final String value) {
				if(value == null) {
					throw new IllegalArgumentException("The value is null.");
				}
				
				String sanitizedValue = value.trim().toLowerCase();
				if(NAMESPACE.equals(sanitizedValue)) {
					return ALL_COLUMNS;
				}
				
				if(sanitizedValue.startsWith(NAMESPACE + NAMESPACE_DIVIDOR)) {
					sanitizedValue = 
							value.substring(
									NAMESPACE.length() +
									NAMESPACE_DIVIDOR.length());
				}

				for(SensorDataColumnKey currKey : values()) {
					if(currKey.key.equals(sanitizedValue) ||
							currKey.abbreviatedKey.equals(sanitizedValue)) {
						
						if(ACCELEROMETER_DATA.equals(currKey)) {
							return AccelDataColumnKey.ALL_COLUMNS;
						}
						else if(WIFI_DATA.equals(currKey)) {
							return WifiDataColumnKey.ALL_COLUMNS;
						}
						else {
							List<ColumnKey> result = new ArrayList<ColumnKey>(1);
							result.add(currKey);
							return result;
						}
					}
				}
				
				throw new IllegalArgumentException("Unknown column key.");
			}
			
			/**
			 * Checks if a collection of column keys contains any of the
			 * keys in this enum or any of the enums that belong to a 
			 * SensorData object.
			 * 
			 * @param columns The collection of columns.
			 * 
			 * @return True if the collection contains any of these column
			 * 		   keys; false, otherwise.
			 */
			public static boolean containsSensorDataColumnKey(
					final Collection<ColumnKey> columns) {
				
				for(SensorDataColumnKey currKey : values()) {
					if(columns.contains(currKey)) {
						return true;
					}
				}
				
				for(AccelDataColumnKey currKey : AccelDataColumnKey.values()) {
					if(columns.contains(currKey)) {
						return true;
					}
				}
				
				for(WifiDataColumnKey currKey : WifiDataColumnKey.values()) {
					if(columns.contains(currKey)) {
						return true;
					}
				}
				
				return false;
			}
		}
		
		private final Mode mode;
		private final Double speed;
		
		/**
		 * This class is responsible for managing tri-axle acceleration data
		 * points.
		 * 
		 * @author John Jenkins
		 */
		public static final class AccelData {
			/**
			 * Column names for accelerometer data information.
			 * 
			 * @author John Jenkins
			 */
			public static enum AccelDataColumnKey implements ColumnKey {
				/**
				 * The 'x'-component of this accelerometer reading.
				 */
				X ("x", "x"),
				/**
				 * The 'y'-component of this accelerometer reading.
				 */
				Y ("y", "y"),
				/**
				 * The 'z'-component of this accelerometer reading.
				 */
				Z ("z", "z");
				
				/**
				 * The string that may be optionally be placed before a key to 
				 * better "namespace" it.
				 */
				public static final String NAMESPACE = "accel";
				
				/**
				 * A pre-built, unmodifiable list that contains all of the 
				 * LocationColumnKey keys.
				 */
				public static final List<ColumnKey> ALL_COLUMNS;
				static {
					List<ColumnKey> keys = new LinkedList<ColumnKey>();
					
					keys.add(X);
					keys.add(Y);
					keys.add(Z);
					
					ALL_COLUMNS = Collections.unmodifiableList(keys);
				}
				
				private final String key;
				private final String abbreviatedKey;
				
				/**
				 * Creates an AccelDataColumnKey object with the human-readable
				 * and abbreviated versions of the key.
				 * 
				 * @param key The long, human-readable name for this key.
				 * 
				 * @param abbreviatedKey A short abbreviation for this key.
				 */
				private AccelDataColumnKey(
						final String key, 
						final String abbreviatedKey) {
					
					this.key = key;
					this.abbreviatedKey = abbreviatedKey;
				}
				
				/**
				 * Converts this key to a string with the {@link #NAMESPACE} 
				 * before it.
				 * 
				 * @return The {@link #NAMESPACE} and 
				 * 		   {@link ColumnKey#NAMESPACE_DIVIDOR} followed by this
				 * 		   key's value.
				 */
				@Override
				public String toString() {
					return NAMESPACE + NAMESPACE_DIVIDOR + key;
				}
				
				/**
				 * Returns this key as a human-readable or abbreviated string.
				 * 
				 * @param abbreviated Whether or not to return an abbreviated 
				 * 					  version of this key.
				 * 
				 * @return This key as a human-readable or abbreviated string.
				 */
				public String toString(final boolean abbreviated) {
					return (abbreviated) ? abbreviatedKey : key;
				}
				
				/**
				 * Converts a string, either the human-readable version or the 
				 * abbreviated version, to its AccelDataColumnKey object.
				 *  
				 * @param value The string value to convert.
				 * 
				 * @return The AccelDataColumnKey that represents this object.
				 * 
				 * @throws IllegalArgumentException The string could not be 
				 * 									converted into a 
				 * 									AccelDataColumnKey object.
				 */
				public static List<ColumnKey> valueOfString(
						final String value) {
					
					if(value == null) {
						throw new IllegalArgumentException(
								"The value is null.");
					}
					
					String sanitizedValue = value.trim().toLowerCase();
					if(NAMESPACE.equals(sanitizedValue)) {
						return ALL_COLUMNS;
					}
					
					if(sanitizedValue.startsWith(NAMESPACE + NAMESPACE_DIVIDOR)) {
						sanitizedValue = 
								value.substring(
										NAMESPACE.length() +
										NAMESPACE_DIVIDOR.length());
					}
					
					for(AccelDataColumnKey currKey : values()) {
						if(currKey.key.equals(sanitizedValue) ||
								currKey.abbreviatedKey.equals(sanitizedValue)) {
							
							List<ColumnKey> result = 
									new ArrayList<ColumnKey>(1);
							result.add(currKey);
							return result;
						}
					}
					
					throw new IllegalArgumentException("Unknown column key.");
				}
				
				/**
				 * Checks if a collection of column keys contains any of the
				 * keys in this enum.
				 * 
				 * @param columns The collection of columns.
				 * 
				 * @return True if the collection contains any of these column
				 * 		   keys; false, otherwise.
				 */
				public static boolean containsAccelDataColumnKey(
						final Collection<ColumnKey> columns) {
					
					for(AccelDataColumnKey currKey : values()) {
						if(columns.contains(currKey)) {
							return true;
						}
					}
					
					return false;
				}
			}
			
			private final Double x;
			private final Double y;
			private final Double z;
			
			/**
			 * Creates an AccelData object from a generic record.
			 * 
			 * @param accelDataRecord The generic record.
			 * 
			 * @throws DomainException The record is missing data.
			 */
			private AccelData(
					final GenericRecord accelDataRecord)
					throws DomainException {

				Object xObject = 
					accelDataRecord.get(AccelDataColumnKey.X.toString(false));
				if(xObject instanceof Number) {
					x = ((Number) xObject).doubleValue();
				}
				else {
					throw new DomainException(
						"The x-value is not a number.");
				}

				Object yObject = 
					accelDataRecord.get(AccelDataColumnKey.Y.toString(false));
				if(yObject instanceof Number) {
					y = ((Number) yObject).doubleValue();
				}
				else {
					throw new DomainException(
						"The y-value is not a number.");
				}

				Object zObject = 
					accelDataRecord.get(AccelDataColumnKey.Z.toString(false));
				if(zObject instanceof Number) {
					z = ((Number) zObject).doubleValue();
				}
				else {
					throw new DomainException(
						"The z-value is not a number.");
				}
			}
			
			/**
			 * Processes an acceleration data point into an AccelData object.
			 * 
			 * @param accelData The JSON data point to process.
			 * 
			 * @param mode The pre-processed mode. This is needed, because if 
			 * 			   it is {@link Mode#ERROR} then any of the values may 
			 * 			   be missing or invalid.
			 * 
			 * @throws DomainException Thrown if the JSON is invalid and the
			 * 						   mode is not {@link Mode#ERROR}.
			 */
			private AccelData(
					final JSONObject accelData, 
					final Mode mode) 
					throws DomainException {
				
				// Get the x-acceleration.
				Double tX = null;
				try {
					tX = accelData.getDouble(
							AccelDataColumnKey.X.toString(false));
				}
				catch(JSONException e) {
					if(Mode.ERROR.equals(mode)) {
						tX = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"The 'x' point was missing or invalid.", 
								e);
					}
				}
				x = tX;
				
				// Get the y-acceleration.
				Double tY = null;
				try {
					tY = accelData.getDouble(
							AccelDataColumnKey.Y.toString(false));
				}
				catch(JSONException e) {
					if(Mode.ERROR.equals(mode)) {
						tY = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"The 'y' point was missing or invalid.", 
								e);
					}
				}
				y = tY;
				
				// Get the z-acceleration.
				Double tZ = null;
				try {
					tZ = accelData.getDouble(
							AccelDataColumnKey.Z.toString(false));
				}
				catch(JSONException e) {
					if(Mode.ERROR.equals(mode)) {
						tZ = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"The 'z' point was missing or invalid.", 
								e);
					}
				}
				z = tZ;
			}
			
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
			 * Creates a JSONObject that represents the accelerometer data with
			 * only the information whose column value is present.
			 * 
			 * @param columns A collection of columns dictating which variables
			 * 				  should be included in the resulting object. If 
			 * 				  this is empty, an empty JSONObject will be 
			 * 				  returned. This cannot be null and if all columns 
			 * 				  are desired, a quick fix is to use
			 * 				  {@link AccelDataColumnKey#ALL_COLUMNS}.
			 * 
			 * @return Returns a JSONObject that represents this object with 
			 * 		   only the requested columns.
			 * 
			 * @throws JSONException There was an error building the 
			 * 						 JSONObject.
			 * 
			 * @throws DomainException The columns collection was null.
			 */
			public final JSONObject toJson(
					final Collection<ColumnKey> columns) 
					throws JSONException, DomainException {
				
				if(columns == null) {
					throw new DomainException(
							"The columns list cannot be null.");
				}
				
				JSONObject result = new JSONObject();
				
				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.ACCELEROMETER_DATA) ||
					columns.contains(AccelDataColumnKey.X)) {
					
					result.put(AccelDataColumnKey.X.toString(false), x);
				}

				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.ACCELEROMETER_DATA) ||
					columns.contains(AccelDataColumnKey.Y)) {
					
					result.put(AccelDataColumnKey.Y.toString(false), y);
				}

				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.ACCELEROMETER_DATA) ||
					columns.contains(AccelDataColumnKey.Z)) {
					
					result.put(AccelDataColumnKey.Z.toString(false), z);
				}
				
				return result;
			}
			
			/**
			 * Populates the appropriate indices in the 'result' with values 
			 * based on the 'columns'. For example, if the first two columns 
			 * were not AccelData columns, they would be skipped. If the third 
			 * column was the accelerometer data's x value, the third index in 
			 * the 'result' would be populated with this accelerometer data's x
			 * value, which is a collection of doubles.
			 * 
			 * @param columns All of the columns in the 'result' list to 
			 * 				  populate. This will skip all of those columns 
			 * 				  that are not AccelDataColumnKey columns.
			 * 
			 * @param result The result list whose values will be updated based
			 * 				 on the 'columns'.
			 * 
			 * @throws DomainException The columns or result list was null or 
			 * 						   had differing lengths.
			 */
			public final void toCsvRow(
					final List<ColumnKey> columns,
					final List<Object> result) 
					throws DomainException {
				
				if(columns == null) {
					throw new DomainException(
							"The list of columns cannot be null.");
				}
				else if(result == null) {
					throw new DomainException(
							"The list of results cannot be null.");
				}
				else if(columns.size() != result.size()) {
					throw new DomainException(
							"The columns list and the result list were different lengths.");
				}
				
				int index;
				
				if((index = columns.indexOf(AccelDataColumnKey.X)) != -1) {
					@SuppressWarnings("unchecked")
					Collection<Double> currList =
							(Collection<Double>) result.get(index);
					if(currList == null) {
						currList = new LinkedList<Double>();
						result.set(index, currList);
					}
					currList.add(x);
				}

				if((index = columns.indexOf(AccelDataColumnKey.Y)) != -1) {
					@SuppressWarnings("unchecked")
					Collection<Double> currList = 
							(Collection<Double>) result.get(index);
					if(currList == null) {
						currList = new LinkedList<Double>();
						result.set(index, currList);
					}
					currList.add(y);
				}

				if((index = columns.indexOf(AccelDataColumnKey.Z)) != -1) {
					@SuppressWarnings("unchecked")
					Collection<Double> currList = 
							(Collection<Double>) result.get(index);
					if(currList == null) {
						currList = new LinkedList<Double>();
						result.set(index, currList);
					}
					currList.add(z);
				}
			}
		}
		private final List<AccelData> accelData;
		
		/**
		 * This class is responsible for the WifiData in a sensor data upload.
		 * 
		 * @author John Jenkins
		 */
		public static final class WifiData {
			/**
			 * Column names for WiFi data information.
			 * 
			 * @author John Jenkins
			 */
			public static enum WifiDataColumnKey implements ColumnKey {
				/**
				 * The time this WiFi data was collected.
				 */
				TIME ("time", "t"),
				/**
				 * An ISO-8601 time stamp.
				 */
				TIMESTAMP ("timestamp", "ts"),
				/**
				 * The timezone of the device.
				 */
				TIMEZONE ("timezone", "tz"),
				/**
				 * The scan information.
				 */
				SCAN ("scan", "sc"),
				/**
				 * The SSID of a device read.
				 */
				SSID ("ssid", "ss"),
				/**
				 * The strength of the reading from the device.
				 */
				STRENGTH ("strength", "st");
				
				/**
				 * The string that may be optionally be placed before a key to 
				 * better namespace it.
				 */
				public static final String NAMESPACE = "wifi";
				
				/**
				 * A pre-built, unmodifiable list that contains all of the 
				 * LocationColumnKey keys.
				 */
				public static final List<ColumnKey> ALL_COLUMNS;
				static {
					List<ColumnKey> keys = new ArrayList<ColumnKey>();
					
					keys.add(TIME);
					keys.add(TIMESTAMP);
					keys.add(TIMEZONE);
					keys.add(SSID);
					keys.add(STRENGTH);
					
					ALL_COLUMNS = Collections.unmodifiableList(keys);
				}				
				
				private final String key;
				private final String abbreviatedKey;
				
				/**
				 * Creates an AccelDataColumnKey object with the human-readable
				 * and abbreviated versions of the key.
				 * 
				 * @param key The long, human-readable name for this key.
				 * 
				 * @param abbreviatedKey A short abbreviation for this key.
				 */
				private WifiDataColumnKey(
						final String key, 
						final String abbreviatedKey) {
					
					this.key = key;
					this.abbreviatedKey = abbreviatedKey;
				}
				
				/**
				 * Converts this key to a string with the {@link #NAMESPACE} 
				 * before it.
				 * 
				 * @return The {@link #NAMESPACE} and 
				 * 		   {@link ColumnKey#NAMESPACE_DIVIDOR} followed by this
				 * 		   key's value.
				 */
				@Override
				public String toString() {
					return NAMESPACE + NAMESPACE_DIVIDOR + key;
				}
				
				/**
				 * Returns this key as a human-readable or abbreviated string.
				 * 
				 * @param abbreviated Whether or not to return an abbreviated 
				 * 					  version of this key.
				 * 
				 * @return This key as a human-readable or abbreviated string.
				 */
				public String toString(final boolean abbreviated) {
					return (abbreviated) ? abbreviatedKey : key;
				}
				
				/**
				 * Converts a string, either the human-readable version or the 
				 * abbreviated version, to its WifiDataColumnKey object.
				 *  
				 * @param value The string value to convert.
				 * 
				 * @return The WifiDataColumnKey that represents this object.
				 * 
				 * @throws IllegalArgumentException The string could not be 
				 * 									converted into a 
				 * 									WifiDataColumnKey object.
				 */
				public static List<ColumnKey> valueOfString(
						final String value) {
					
					if(value == null) {
						throw new IllegalArgumentException(
								"The value is null.");
					}
					
					String sanitizedValue = value.trim().toLowerCase();
					if(NAMESPACE.equals(sanitizedValue)) {
						return ALL_COLUMNS;
					}
					
					if(sanitizedValue.startsWith(NAMESPACE + NAMESPACE_DIVIDOR)) {
						sanitizedValue = 
								value.substring(
										NAMESPACE.length() +
										NAMESPACE_DIVIDOR.length());
					}
					
					for(WifiDataColumnKey currKey : values()) {
						if(currKey.key.equals(sanitizedValue) ||
								currKey.abbreviatedKey.equals(sanitizedValue)) {
							
							if(SCAN.equals(currKey)) {
								List<ColumnKey> result = 
										new ArrayList<ColumnKey>(2);
								result.add(SSID);
								result.add(STRENGTH);
								return result;
							}
							else {
								List<ColumnKey> result = 
										new ArrayList<ColumnKey>(1);
								result.add(currKey);
								return result;
							}
						}
					}
					
					throw new IllegalArgumentException("Unknown column key.");
				}
				
				/**
				 * Checks if a collection of column keys contains any of the
				 * keys in this enum.
				 * 
				 * @param columns The collection of columns.
				 * 
				 * @return True if the collection contains any of these column
				 * 		   keys; false, otherwise.
				 */
				public static boolean containsWifiDataColumnKey(
						final Collection<ColumnKey> columns) {
					
					for(WifiDataColumnKey currKey : values()) {
						if(columns.contains(currKey)) {
							return true;
						}
					}
					
					return false;
				}
			}
			
			private final Long time;
			private final DateTimeZone timezone;
			private final Map<String, Double> scan;
			
			/**
			 * Creates a WifiData object from this generic record.
			 * 
			 * @param wifiDataRecord The generic record.
			 * 
			 * @throws DomainException The record was missing some data.
			 */
			private WifiData(
					final GenericRecord wifiDataRecord)
					throws DomainException {
				
				// Write the time.
				Object timeObject = wifiDataRecord.get("time");
				if(timeObject instanceof Number) {
					time = ((Number) timeObject).longValue();
				}
				else {
					throw new DomainException("The time is not a number.");
				}
				
				// Write the time zone.
				Object timezoneObject = wifiDataRecord.get("timezone");
				if(timezoneObject instanceof Utf8) {
					timezone = 
						DateTimeZone.forID(((Utf8) timezoneObject).toString()); 
				}
				else {
					throw new DomainException(
						"The time zone is not a string.");
				}
				
				// Write the scan.
				Object scanObject = wifiDataRecord.get("scan");
				if(scanObject instanceof GenericArray) {
					@SuppressWarnings("unchecked")
					GenericArray<GenericRecord> scanArray =
						(GenericArray<GenericRecord>) scanObject;
					scan = new HashMap<String, Double>(scanArray.size());
					
					for(GenericRecord scanRecord : scanArray) {
						String ssid;
						Object ssidObject = scanRecord.get("ssid");
						if(ssidObject instanceof Utf8) {
							ssid = ((Utf8) ssidObject).toString();
						}
						else {
							throw new DomainException(
								"The SSID is not a string.");
						}
						
						double strength;
						Object strengthObject = 
							scanRecord.get("strength");
						if(strengthObject instanceof Number) {
							strength = ((Number) strengthObject).doubleValue();
						}
						else {
							throw new DomainException(
								"The strength is not a number.");
						}
						
						if(scan.put(ssid, strength) != null) {
							throw new DomainException(
								"Duplicate SSIDs found in the same scan.");
						}
					}
				}
				else {
					throw new DomainException("The scan is not an array.");
				}
			}
			
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
					final JSONObject wifiData, 
					final Mode mode) 
					throws DomainException {
				
				// Get the time and timezone.
				Long tTime = null;
				boolean timeFound = true;
				try {
					tTime = wifiData.getLong(
							WifiDataColumnKey.TIME.toString(false));
				}
				catch(JSONException noLongTime) {
					try {
						tTime = wifiData.getLong(
								WifiDataColumnKey.TIME.toString(true));
					}
					catch(JSONException noTime) {
						timeFound = false;
						String timestamp = null;
						try {
							timestamp = 
								wifiData.getString(
									WifiDataColumnKey.TIMESTAMP.toString(
										false));
						}
						catch(JSONException noLongTimestamp) {	
							try {
								timestamp = 
									wifiData.getString(
										WifiDataColumnKey.TIMESTAMP.toString(
											true));
							}
							catch(JSONException noShortTimestamp) {
								if(Mode.ERROR.equals(mode)) {
									tTime = null;
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
								tTime = null;
							}
							else {
								throw new DomainException(
										ErrorCode.SERVER_INVALID_TIMESTAMP,
										"The timestamp could not be decoded: " +
											timestamp);
							}
						}
						else {
							tTime = date.getTime();
						}
					}
				}
				time = tTime;
				
				DateTimeZone tTimezone;
				try {
					tTimezone =
							TimeUtils.getDateTimeZoneFromString(
								wifiData.getString(
									WifiDataColumnKey.TIMEZONE.toString(
										false)));
				}
				catch(JSONException noLongTimezone) {
					try {
						tTimezone =
								TimeUtils.getDateTimeZoneFromString(
									wifiData.getString(
										WifiDataColumnKey.TIMEZONE.toString(
											true)));
					}
					catch(JSONException noShortTimezone) {
						if(Mode.ERROR.equals(mode)) {
							tTimezone = null;
						}
						else if(! timeFound) {
							tTimezone = DateTimeZone.getDefault();
						}
						else {
							throw new DomainException(
									ErrorCode.SERVER_INVALID_TIMEZONE, 
									"The timezone is missing: " +
										WifiDataColumnKey.TIMEZONE.toString(
											false), 
									noShortTimezone);
						}
					}
				}
				catch(IllegalArgumentException e) {
					throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMEZONE,
						"The time zone is unknown.",
						e);
				}
				timezone = tTimezone;
				
				// Get the scan.
				JSONArray scan;
				try {
					scan = wifiData.getJSONArray(
							WifiDataColumnKey.SCAN.toString(false));
				}
				catch(JSONException e) {
					try {
						scan = 
							wifiData.getJSONArray(
								WifiDataColumnKey.SCAN.toString(true));
					}
					catch(JSONException notShort) {
						if(Mode.ERROR.equals(mode)) {
							scan = null;
						}
						else {
							throw new DomainException(
									ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
									"The scan is missing.", 
									notShort);
						}
					}
				}
				
				// Validate the scan value.
				if(scan == null) {
					this.scan = null;
				}
				else {
					// Create the local scan map.
					this.scan = new HashMap<String, Double>();
					//List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
					
					// For each of the entries in the array, parse out the
					// necessary information.
					int numScans = scan.length();
					for(int i = 0; i < numScans; i++) {
						try {
							JSONObject jsonObject = scan.getJSONObject(i);
							
							// Get the SSID.
							String ssid;
							try {
								ssid = jsonObject.getString(
										WifiDataColumnKey.SSID.toString(
												false));
							}
							catch(JSONException notLong) {
								try {
									ssid = jsonObject.getString(
											WifiDataColumnKey.SSID.toString(
													true));
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
												WifiDataColumnKey.STRENGTH.toString(
														false));
							}
							catch(JSONException notLong) {
								try {
									strength = 
											jsonObject.getDouble(
													WifiDataColumnKey.STRENGTH.toString(
															true));
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
							//accessPoints.add(new AccessPoint(ssid, strength));
							this.scan.put(ssid, strength);
						}
						catch(JSONException e) {
							throw new DomainException(
									"The object changed while we were reading it.", 
									e);
						}
					}
					//this.wifiScan = new WifiScan(tTime, accessPoints);
				}
			}
			
			/**
			 * Creates a WiFiData object that contains a timestamp of when the
			 * record was made and the map of WiFi device IDs to their signal
			 * strength.
			 * 
			 * @param time The time in milliseconds since the epoch when this
			 * 			   occurred.
			 *  
			 * @param timestamp The date and time that this record was made.
			 * 
			 * @param scan A map of WiFi device IDs to their signal strength.
			 * 
			 * @throws DomainException Thrown if the timestamp or scans map are
			 * 						   null.
			 */
			public WifiData(
					final long time,
					final DateTimeZone timezone,
					final Map<String, Double> scan)
					throws DomainException {
				
				if(timezone == null) {
					throw new DomainException(
							"The timezone cannot be null.");
				}
				else if(scan == null) {
					throw new DomainException(
							"The WiFi scan cannot be null.");
				}
				
				this.time = time;
				this.timezone = timezone;
				this.scan = new HashMap<String, Double>(scan);
			}
			
			/**
			 * Creates a JSONObject that represents the WiFi scan data with 
			 * only the information whose column value is present.
			 * 
			 * @param columns A collection of columns dictating which variables
			 * 				  should be included in the resulting object. If 
			 * 				  this is empty, an empty JSONObject will be 
			 * 				  returned. This cannot be null and if all columns 
			 * 				  are desired, a quick fix is to use
			 * 				  {@link WifiDataColumnKey#ALL_COLUMNS}.
			 * 
			 * @return Returns a JSONObject that represents this object with 
			 * 		   only the requested columns.
			 * 
			 * @throws JSONException There was an error building the 
			 * 						 JSONObject.
			 * 
			 * @throws DomainException The columns collection was null.
			 */
			public final JSONObject toJson(
					final boolean abbreviated,
					final Collection<ColumnKey> columns) 
					throws JSONException, DomainException {
				
				if(columns == null) {
					throw new DomainException(
							"The columns list cannot be null.");
				}

				JSONObject result = new JSONObject();
				
				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.TIME)) {
					
					result.put(
							WifiDataColumnKey.TIME.toString(abbreviated), 
							time);
				}
					
				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.TIMESTAMP)) {
					
					DateTime dateTime = new DateTime(time, timezone);
					DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
					builder.appendPattern(DATE_TIME_FORMAT);
					
					result.put(
							WifiDataColumnKey.TIMESTAMP.toString(
									abbreviated), 
							builder.toFormatter().print(dateTime));
				}
					
				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.TIMEZONE)) {
					
					result.put(
							WifiDataColumnKey.TIMEZONE.toString(
									abbreviated),
							timezone.getID());
				}
				
				if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.SCAN) ||
						(columns.contains(WifiDataColumnKey.SSID) &&
						 columns.contains(WifiDataColumnKey.STRENGTH))) {
					
					if(scan != null) {
						JSONArray scans = new JSONArray();
						for(String ssid : scan.keySet()) {
							JSONObject currScan = new JSONObject();
							
							currScan.put(
									WifiDataColumnKey.SSID.toString(
											abbreviated),
									ssid);
							currScan.put(
									WifiDataColumnKey.STRENGTH.toString(
											abbreviated),
									scan.get(ssid));
							
							scans.put(currScan);
						}
						
						result.put(
								WifiDataColumnKey.SCAN.toString(
										abbreviated), 
								scans);
					}
				}
				else if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.SSID)) {
					
					if(scan != null) {
						JSONArray scans = new JSONArray();
						for(String ssid : scan.keySet()) {
							JSONObject currScan = new JSONObject();
							
							currScan.put(
									WifiDataColumnKey.STRENGTH.toString(
											abbreviated),
									scan.get(ssid));
							
							scans.put(currScan);
						}
						
						result.put(
								WifiDataColumnKey.SCAN.toString(
										abbreviated), 
								scans);
					}
				}
				else if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
					columns.contains(SensorDataColumnKey.WIFI_DATA) ||
					columns.contains(WifiDataColumnKey.STRENGTH)) {
					
					if(scan != null) {
						JSONArray scans = new JSONArray();
						for(String ssid : scan.keySet()) {
							JSONObject currScan = new JSONObject();
							
							currScan.put(
									WifiDataColumnKey.SSID.toString(
											abbreviated),
									ssid);
							
							scans.put(currScan);
						}
						
						result.put(
								WifiDataColumnKey.SCAN.toString(
										abbreviated), 
								scans);
					}
				}

				return result;
			}
			
			/**
			 * Populates the appropriate indices in the 'result' with values 
			 * based on the 'columns'. For example, if the first two columns 
			 * were not WifiData columns, they would be skipped. If the third 
			 * column was the WiFi data's time, the third index in the 'result' 
			 * would be populated with this WiFi data's time value.
			 * 
			 * @param columns All of the columns in the 'result' list to 
			 * 				  populate. This will skip all of those columns 
			 * 				  that are not WifiDataColumnKey columns.
			 * 
			 * @param result The result list whose values will be updated based
			 * 				 on the 'columns'.
			 * 
			 * @throws DomainException The columns or result list was null or 
			 * 						   had differing lengths.
			 */
			public final void toCsvRow(
					final List<ColumnKey> columns,
					final List<Object> result) 
					throws DomainException {
				
				if(columns == null) {
					throw new DomainException(
							"The list of columns cannot be null.");
				}
				else if(result == null) {
					throw new DomainException(
							"The list of results cannot be null.");
				}
				else if(columns.size() != result.size()) {
					throw new DomainException(
							"The columns list and the result list were different lengths.");
				}
				
				int index;
				
				if((index = columns.indexOf(WifiDataColumnKey.TIME)) != -1) {
					result.set(index, time);
				}
					
				if((index = columns.indexOf(WifiDataColumnKey.TIMESTAMP)) != -1) {
					DateTime dateTime = new DateTime(time, timezone);
					DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
					builder.appendPattern(DATE_TIME_FORMAT);
					
					result.set(index, builder.toFormatter().print(dateTime));
				}
					
				if((index = columns.indexOf(WifiDataColumnKey.TIMEZONE)) != -1) {
					result.set(index, timezone.getID());
				}
				
				// If SCAN is present, we ignore it as it should have been, for
				// CSV output at least, broken down into SSID and STRENGTH.

				if(scan != null) {
					int ssidIndex = columns.indexOf(WifiDataColumnKey.SSID);
					Collection<String> ssids = 
							new ArrayList<String>(scan.size());
					
					int strengthIndex = 
							columns.indexOf(WifiDataColumnKey.STRENGTH);
					Collection<Double> strengths =
							new ArrayList<Double>(scan.size());
					
					for(String ssid : scan.keySet()) {
						if(ssidIndex != -1) {
							ssids.add(ssid);
						}
						
						if(strengthIndex != -1) {
							strengths.add(scan.get(ssid));
						}
					}
					
					if(ssidIndex != -1) {
						result.set(ssidIndex, ssids);
					}
					
					if(strengthIndex != -1) {
						result.set(strengthIndex, strengths);
					}
				}
			}
		}
		private final WifiData wifiData;
		
		/**
		 * Creates a SensorData object.
		 * 
		 * @param mode The mode generated from this sensor data.
		 * 
		 * @param speed The speed or null if the sensor data is 'ERROR' mode.
		 * 
		 * @param accelData The accelerometer data or null if the sensor data 
		 * 					is 'ERROR' mode.
		 * 
		 * @param wifiData The WiFi data or null if the sensor data is 'ERROR'
		 * 				   mode.
		 * 
		 * @throws DomainException A field was null and the mode was not 
		 * 						   'ERROR'.
		 */
		public SensorData(
				final Mode mode,
				final Double speed,
				final List<AccelData> accelData,
				final WifiData wifiData) 
				throws DomainException {
			
			if(mode == null) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_MODE, 
					"The mode is null.");
			}
			if((speed == null) && (! Mode.ERROR.equals(mode))) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_SPEED,
					"The speed is null and the mode is not 'ERROR'.");
			}
			if((accelData == null) && (! Mode.ERROR.equals(mode))) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA,
					"The accelerometer data is null and the mode is not 'ERROR'.");
			}
			if((wifiData == null) && (! Mode.ERROR.equals(mode))) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_WIFI_DATA,
					"The WiFi data is null and the mode is not 'ERROR'.");
			}
			
			this.mode = mode;
			this.speed = speed;
			this.accelData = accelData;
			this.wifiData = wifiData;
		}
		
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
				modeString = 
						sensorData.getString(
								MobilityColumnKey.MODE.toString(false));
			}
			catch(JSONException notLong) {
				try {
					modeString = 
							sensorData.getString(
									MobilityColumnKey.MODE.toString(true));
				}
				catch(JSONException notShort) {
					throw new DomainException(
							ErrorCode.MOBILITY_INVALID_MODE, 
							"The mode is missing in the sensor data: " + 
									MobilityColumnKey.MODE.toString(false), 
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
				tSpeed = 
						sensorData.getDouble(
								SensorDataColumnKey.SPEED.toString(false));
			}
			catch(JSONException notLong) {
				try {
					tSpeed = 
							sensorData.getDouble(
									SensorDataColumnKey.SPEED.toString(true));
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tSpeed = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_SPEED,
								"The speed is missing or invalid: " +
									SensorDataColumnKey.SPEED.toString(false), 
								notShort);
					}
				}
			}
			if(tSpeed.isInfinite() || tSpeed.isNaN()) {
				tSpeed = -1.0;
			}
			speed = tSpeed;

			// Get the accelerometer data.
			List<AccelData> tAccelData = null;
			JSONArray accelDataJson = null;
			try {
				accelDataJson = 
						sensorData.getJSONArray(
								SensorDataColumnKey.ACCELEROMETER_DATA.toString(
										false));
			}
			catch(JSONException notLong) {
				try {
					accelDataJson = 
							sensorData.getJSONArray(
									SensorDataColumnKey.ACCELEROMETER_DATA.toString(
											true));
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tAccelData = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_ACCELEROMETER_DATA, 
								"The accelerometer data is missing or invalid: " +
									SensorDataColumnKey.ACCELEROMETER_DATA.toString(
										false), 
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
						tAccelData.add(
								new AccelData(
										accelDataJson.getJSONObject(i), 
										mode));
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
				wifiDataJson = 
						sensorData.getJSONObject(
								SensorDataColumnKey.WIFI_DATA.toString(
										false));
			}
			catch(JSONException notLong) {
				try {
					wifiDataJson = 
							sensorData.getJSONObject(
									SensorDataColumnKey.WIFI_DATA.toString(
											true));
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tWifiData = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_WIFI_DATA, 
								"The WiFi data is missing or invalid: " +
									SensorDataColumnKey.WIFI_DATA.toString(
										false), 
								notShort);
					}
				}
			}
			
			// If the WiFi data was found and contained at least one key, 
			// create a WifiData object. The one key refers to where Mobility
			// heard nothing from WifiGpsLocationService.
			if((wifiDataJson != null) && (wifiDataJson.length() > 0)) {
				tWifiData = new WifiData(wifiDataJson, mode);
			}
			wifiData = tWifiData;
		}
		
		/**
		 * Creates a new SensorData object from a data record.
		 * 
		 * @param mode The sensor data's mode.
		 * 
		 * @param dataRecord The data record.
		 * 
		 * @throws DomainException A parameter was null or the data record was
		 * 						   malformed.
		 */
		private SensorData(
				final Mode mode,
				final GenericRecord dataRecord)
				throws DomainException {
			
			if(mode == null) {
				throw new DomainException("The mode is null.");
			}
			if(dataRecord == null) {
				throw new DomainException("The data record is null.");
			}
			
			this.mode = mode;
			
			Object speedObject = 
				dataRecord.get(SensorDataColumnKey.SPEED.toString(false));
			if(speedObject instanceof Number) {
				speed = ((Number) speedObject).doubleValue();
			}
			else {
				throw new DomainException("The speed is not a number.");
			}
			
			Object accelDataObject = 
				dataRecord.get(
					SensorDataColumnKey.ACCELEROMETER_DATA.toString(false));
			if(accelDataObject instanceof GenericArray) {
				@SuppressWarnings("unchecked")
				GenericArray<GenericRecord> accelDataArray = 
					(GenericArray<GenericRecord>) accelDataObject;
				accelData = new ArrayList<AccelData>(accelDataArray.size());

				for(GenericRecord accelRecord : accelDataArray) {
					accelData.add(new AccelData(accelRecord));
				}
			}
			else {
				throw new DomainException(
					"The accelerometer data is not an array.");
			}
			
			Object wifiDataObject = dataRecord.get("wifi_data");
			if(wifiDataObject instanceof GenericRecord) {
				wifiData = new WifiData((GenericRecord) wifiDataObject);
			}
			else {
				throw new DomainException("The WiFi data is not an object.");
			}
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
		 * Creates a JSONObject that represents the sensor data with only the 
		 * information whose column value is present or any of the columns that
		 * belong to objects owned by this sensor data.
		 * 
		 * @param columns A collection of columns dictating which variables
		 * 				  should be included in the resulting object. If 
		 * 				  this is empty, a JSONObject with the mode only will  
		 * 				  be returned. This cannot be null and if all columns 
		 * 				  are desired, a quick fix is to use
		 * 				  {@link SensorDataColumnKey#ALL_COLUMNS}.
		 * 
		 * @return Returns a JSONObject that represents this object with 
		 * 		   only the requested columns.
		 * 
		 * @throws JSONException There was an error building the 
		 * 						 JSONObject.
		 * 
		 * @throws DomainException The columns collection was null.
		 */
		public final JSONObject toJson(
				final boolean abbreviated,
				final Collection<ColumnKey> columns) 
				throws JSONException, DomainException {
			
			if(columns == null) {
				throw new DomainException("The list of columns cannot be null.");
			}
			
			JSONObject result = new JSONObject();
			
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				columns.contains(SensorDataColumnKey.MODE)) {
				result.put(
						MobilityColumnKey.MODE.toString(abbreviated),
						mode.name().toLowerCase());
			}
			
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				columns.contains(SensorDataColumnKey.SPEED)) {
				
				if(speed == null) {
					// Don't put it in the JSON.
				}
				else if(speed.isInfinite()) {
					if(Double.POSITIVE_INFINITY == speed.doubleValue()) {
						result.put(
								SensorDataColumnKey.SPEED.toString(
										abbreviated),
								"Infinity");
					}
					else {
						result.put(
								SensorDataColumnKey.SPEED.toString(
										abbreviated),
								"-Infinity");
					}
				}
				else if(speed.isNaN()) {
					result.put(
							SensorDataColumnKey.SPEED.toString(abbreviated),
							"NaN");
				}
				else {
					result.put(
							SensorDataColumnKey.SPEED.toString(abbreviated), 
							speed);
				}
			}
			
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				columns.contains(SensorDataColumnKey.ACCELEROMETER_DATA) ||
				AccelDataColumnKey.containsAccelDataColumnKey(columns)) {

				if(accelData == null) {
					// Don't put it in the JSON.
				}
				else {
					JSONArray accelArray = new JSONArray();
					for(AccelData accelRecord : accelData) {
						accelArray.put(accelRecord.toJson(columns));
					}
					result.put(
							SensorDataColumnKey.ACCELEROMETER_DATA.toString(
									abbreviated),
							accelArray);
				}
			}
			
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				columns.contains(SensorDataColumnKey.WIFI_DATA) ||
				WifiDataColumnKey.containsWifiDataColumnKey(columns)) {
				
				if(wifiData == null) {
					result.put(
							SensorDataColumnKey.WIFI_DATA.toString(
									abbreviated),
							new JSONObject());
				}
				else {
					result.put(
							SensorDataColumnKey.WIFI_DATA.toString(
									abbreviated),
							wifiData.toJson(abbreviated, columns));
				}
			}
			
			return result;
		}
		
		/**
		 * Populates the appropriate indices in the 'result' with values based 
		 * on the 'columns'. For example, if the first two columns were not 
		 * sensor data columns, they would be skipped. If the third column was 
		 * the sensor data's speed, the third index in the 'result' would be 
		 * populated with this sensor data's speed value.
		 * 
		 * @param columns All of the columns in the 'result' list to populate. 
		 * 				  This will skip all of those columns that are not 
		 * 				  SensorDataColumnKey columns or columns belonging to
		 * 				  the inner classes of SensorData.
		 * 
		 * @param result The result list whose values will be updated based on
		 * 				 the 'columns'.
		 * 
		 * @throws DomainException The columns or result list was null or had 
		 * 						   differing lengths.
		 */
		public final void toCsvRow(
				final List<ColumnKey> columns,
				final List<Object> result) 
				throws DomainException {
			
			if(columns == null) {
				throw new DomainException(
						"The list of columns cannot be null.");
			}
			else if(result == null) {
				throw new DomainException(
						"The list of results cannot be null.");
			}
			else if(columns.size() != result.size()) {
				throw new DomainException(
						"The columns list and the result list were different lengths.");
			}
			
			int index;
			
			if((index = columns.indexOf(SensorDataColumnKey.SPEED)) != -1) {
				result.set(index, speed);
			}
			
			if(accelData != null) {
				for(AccelData currAccelData : accelData) {
					currAccelData.toCsvRow(columns, result);
				}
			}
			
			if(wifiData != null) {
				wifiData.toCsvRow(columns, result);
			}
		}
	}
	private final SensorData sensorData;
	
	/**
	 * This is the classification information computed by the server's 
	 * classifier.
	 * 
	 * @author John Jenkins
	 */
	public static final class ClassifierData {
		/**
		 * Column names for accelerometer data information.
		 * 
		 * @author John Jenkins
		 */
		public static enum ClassifierDataColumnKey implements ColumnKey {
			/**
			 * The FFT generated by the classifier.
			 */
			FFT ("fft", "fft"),
			/**
			 * The variance generated by the classifier.
			 */
			VARIANCE ("variance", "v"),
			/**
			 * The N95 variance generated by the classifier.
			 *
			N95_VARIANCE ("n95variance", "n95v"),*/
			/**
			 * The average generated by the classifier.
			 */
			AVERAGE ("average", "a"),
			/**
			 * The mode generated by the classifier.
			 */
			MODE ("mode", "m");
			
			/**
			 * The string that may be optionally be placed before a key to 
			 * better "namespace" it.
			 */
			public static final String NAMESPACE = "classification";
			
			/**
			 * A pre-built, unmodifiable list that contains all of the 
			 * LocationColumnKey keys.
			 */
			public static final List<ColumnKey> ALL_COLUMNS;
			static {
				List<ColumnKey> keys = new LinkedList<ColumnKey>();
				
				keys.add(FFT);
				keys.add(VARIANCE);
				keys.add(AVERAGE);
				keys.add(MODE);
				
				ALL_COLUMNS = Collections.unmodifiableList(keys);
			}
			
			private final String key;
			private final String abbreviatedKey;
			
			/**
			 * Creates an ClassifierDataColumnKey object with the 
			 * human-readable and abbreviated versions of the key.
			 * 
			 * @param key The long, human-readable name for this key.
			 * 
			 * @param abbreviatedKey A short abbreviation for this key.
			 */
			private ClassifierDataColumnKey(
					final String key, 
					final String abbreviatedKey) {
				
				this.key = key;
				this.abbreviatedKey = abbreviatedKey;
			}
			
			/**
			 * Converts this key to a string with the {@link #NAMESPACE} 
			 * before it.
			 * 
			 * @return The {@link #NAMESPACE} and 
			 * 		   {@link ColumnKey#NAMESPACE_DIVIDOR} followed by this
			 * 		   key's value.
			 */
			@Override
			public String toString() {
				return NAMESPACE + NAMESPACE_DIVIDOR + key;
			}
			
			/**
			 * Returns this key as a human-readable or abbreviated string.
			 * 
			 * @param abbreviated Whether or not to return an abbreviated 
			 * 					  version of this key.
			 * 
			 * @return This key as a human-readable or abbreviated string.
			 */
			public String toString(final boolean abbreviated) {
				return (abbreviated) ? abbreviatedKey : key;
			}
			
			/**
			 * Converts a string, either the human-readable version or the 
			 * abbreviated version, to its ClassifierDataColumnKey object.
			 *  
			 * @param value The string value to convert.
			 * 
			 * @return The AccelDataColumnKey that represents this object.
			 * 
			 * @throws IllegalArgumentException The string could not be 
			 * 									converted into a 
			 * 									ClassifierDataColumnKey object.
			 */
			public static List<ColumnKey> valueOfString(
					final String value) {
				
				if(value == null) {
					throw new IllegalArgumentException(
							"The value is null.");
				}
				
				String sanitizedValue = value.trim().toLowerCase();
				if(NAMESPACE.equals(sanitizedValue)) {
					return ALL_COLUMNS;
				}
				
				if(sanitizedValue.startsWith(NAMESPACE + NAMESPACE_DIVIDOR)) {
					sanitizedValue = 
							value.substring(
									NAMESPACE.length() +
									NAMESPACE_DIVIDOR.length());
				}
				
				for(ClassifierDataColumnKey currKey : values()) {
					if(currKey.key.equals(sanitizedValue) ||
							currKey.abbreviatedKey.equals(sanitizedValue)) {
						
						List<ColumnKey> result = 
								new ArrayList<ColumnKey>(1);
						result.add(currKey);
						return result;
					}
				}
				
				throw new IllegalArgumentException("Unknown column key.");
			}
			
			/**
			 * Checks if a collection of column keys contains any of the
			 * keys in this enum.
			 * 
			 * @param columns The collection of columns.
			 * 
			 * @return True if the collection contains any of these column
			 * 		   keys; false, otherwise.
			 */
			public static boolean containsClassifierDataColumnKey(
					final Collection<ColumnKey> columns) {
				
				for(ClassifierDataColumnKey currKey : values()) {
					if(columns.contains(currKey)) {
						return true;
					}
				}
				
				return false;
			}
		}
		
		private final List<Double> fft;
		private final Double variance;
		
		// This is no longer being collected, but it is being left here as a
		// reminder in case it is added again.
		//private final List<Double> n95Fft;
		//private final Double n95Variance;
		
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
				tMode = 
					Mode.valueOf(
						classifierData.getString(
							ClassifierDataColumnKey.MODE.toString(false))
							.toUpperCase());
			}
			catch(JSONException notRegular) {
				try {
					tMode = 
						Mode.valueOf(
							classifierData.getString(
								ClassifierDataColumnKey.MODE.toString(true))
								.toUpperCase());
				}
				catch(JSONException notShort) {
					if(Mode.ERROR.equals(mode)) {
						tMode = null;
					}
					else {
						throw new DomainException(
								ErrorCode.MOBILITY_INVALID_MODE, 
								"The mode is missing.", 
								notShort);
					}
				}
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_MODE, 
						"The mode is unknown.", 
						e);
			}
			this.mode = tMode;
			
			JSONArray fftArray = null;
			List<Double> tFft = null;
			try {
				fftArray = 
						classifierData.getJSONArray(
							ClassifierDataColumnKey.FFT.toString(false));
			}
			catch(JSONException notRegular) {
				try {
					fftArray = 
							classifierData.getJSONArray(
								ClassifierDataColumnKey.FFT.toString(true));
				}
				catch(JSONException notShort) {
					// If it is missing we don't care. It may be that only the 
					// mode could be calculated.
				}
			}
			if(fftArray != null) {
				int numEntries = fftArray.length();
				tFft = new ArrayList<Double>(numEntries);
				for(int i = 0; i < numEntries; i++) {
					try {
						tFft.add(fftArray.getDouble(i));
					}
					catch(JSONException e) {
						if(Mode.ERROR.equals(mode)) {
							tMode = null;
						}
						else {
							throw new DomainException(
									"The FFT contains an invalid value: " + 
										fftArray.toString(), 
									e);
						}
					}
				}
			}
			fft = tFft;
			
			Double tVariance = null;
			try {
				tVariance = 
						classifierData.getDouble(
							ClassifierDataColumnKey.VARIANCE.toString(false));
			}
			catch(JSONException notRegular) {
				try {
					tVariance = 
							classifierData.getDouble(
								ClassifierDataColumnKey.VARIANCE.toString(
									true));
				}
				catch(JSONException notShort) {
					// If it is missing we don't care. It may be that only the 
					// mode could be calculated.
				}
			}
			variance = tVariance;
			
			Double tAverage = null;
			try {
				tAverage = 
						classifierData.getDouble(
							ClassifierDataColumnKey.AVERAGE.toString(false));
			}
			catch(JSONException e) {
				try {
					tAverage = 
							classifierData.getDouble(
								ClassifierDataColumnKey.AVERAGE.toString(
									true));
				}
				catch(JSONException notShort) {
					// If it is missing we don't care. It may be that only the mode
					// could be calculated.
				}
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
				final Double average, 
				final Mode mode) 
				throws DomainException{
			
			if(mode == null) {
				throw new DomainException("The mode cannot be null.");
			}
			
			this.fft = fft;
			this.variance = variance;
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
		 * @param abbreviated Whether or not the key should return their 
		 * 					  abbreviated versions or their complete versions.
		 * 
		 * @param columns The columns that should be added to the resulting
		 * 				  JSON.
		 * 
		 * @return A JSONObject representing this object.
		 * 
		 * @throws JSONException There was an error building the JSONObject.
		 */
		public final JSONObject toJson(
				final boolean abbreviated,
				final Collection<ColumnKey> columns) 
				throws JSONException, DomainException {
				
			if(columns == null) {
				throw new DomainException(
						"The columns list cannot be null.");
			}
			
			JSONObject result = new JSONObject();
			
			if((columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				columns.contains(ClassifierDataColumnKey.MODE)) &&
				(mode != null)) {
				
				result.put(
					ClassifierDataColumnKey.MODE.toString(abbreviated), 
					mode.name().toLowerCase());
			}
			
			if(columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				columns.contains(ClassifierDataColumnKey.FFT)) {
				
				result.put(
					ClassifierDataColumnKey.FFT.toString(abbreviated), 
					fft);
			}
			
			if(columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				columns.contains(ClassifierDataColumnKey.VARIANCE)) {

				result.put(
					ClassifierDataColumnKey.VARIANCE.toString(abbreviated), 
					variance);
			}
			
			if(columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				columns.contains(ClassifierDataColumnKey.AVERAGE)) {

				result.put(
					ClassifierDataColumnKey.AVERAGE.toString(abbreviated), 
					average);
			}
			
			return result;
		}
		
		/**
		 * Populates the appropriate indices in the 'result' with values based 
		 * on the 'columns'. For example, if the first two columns were not 
		 * ClassifierDataColumnKey columns, they would be skipped. If the third 
		 * column was the classifier data's mode value, the third index in the 
		 * 'result' would be populated with this classifier data's mode value.
		 * 
		 * @param columns All of the columns in the 'result' list to populate.
		 * 				  This will skip all of those columns that are not 
		 * 				  ClassifierDataColumnKey columns.
		 * 
		 * @param result The result list whose values will be updated based on
		 * 				 the 'columns'.
		 * 
		 * @throws DomainException The columns or result list was null or had 
		 * 						   differing lengths.
		 */
		public final void toCsvRow(
				final List<ColumnKey> columns,
				final List<Object> result) 
				throws DomainException {
			
			if(columns == null) {
				throw new DomainException(
						"The list of columns cannot be null.");
			}
			else if(result == null) {
				throw new DomainException(
						"The list of results cannot be null.");
			}
			else if(columns.size() != result.size()) {
				throw new DomainException(
						"The columns list and the result list were different lengths.");
			}
			
			int index;
			
			if((index = columns.indexOf(ClassifierDataColumnKey.MODE)) != -1) {
				result.set(index, mode.name().toLowerCase());
			}
			
			if((index = columns.indexOf(ClassifierDataColumnKey.FFT)) != -1) {
				result.set(index, fft);
			}
			
			if((index = columns.indexOf(ClassifierDataColumnKey.VARIANCE)) != -1) {
				result.set(index, variance);
			}
			
			if((index = columns.indexOf(ClassifierDataColumnKey.AVERAGE)) != -1) {
				result.set(index, average);
			}
		}
	}
	private ClassifierData classifierData;
	
	/**
	 * Creates a MobilityPoint object from a data stream.
	 * 
	 * @param dataStream The data stream of data.
	 * 
	 * @param subType The sub-type of the record.
	 * 
	 * @param privacyState The privacy state of the record.
	 * 
	 * @throws DomainException The point was invalid.
	 */
	public MobilityPoint(
			final DataStream dataStream,
			final SubType subType,
			final PrivacyState privacyState)
			throws DomainException {
		
		if(dataStream == null) {
			throw new DomainException("The data stream is null.");
		}
		if(subType == null) {
			throw new DomainException("The sub-type is null.");
		}
		if(privacyState == null) {
			throw new DomainException("The privacy state is null.");
		}

		MetaData metaData = dataStream.getMetaData();
		if(metaData == null) {
			throw new DomainException("The meta-data is missing.");
		}
		
		String idString = metaData.getId();
		if(idString == null) {
			throw new DomainException("The ID is missing.");
		}
		id = UUID.fromString(idString);
		
		DateTime timestamp = metaData.getTimestamp();
		if(timestamp == null) {
			throw new DomainException("The timestamp is missing.");
		}
		
		time = timestamp.getMillis();
		timezone = timestamp.getZone();

		location = metaData.getLocation();
		if(location == null) {
			locationStatus = LocationStatus.UNAVAILABLE;
		}
		else {
			locationStatus = LocationStatus.VALID;
		}
		
		this.privacyState = privacyState;
		this.subType = subType;
		
		GenericContainer data = dataStream.getData();
		if(data instanceof GenericRecord) {
			GenericRecord dataRecord = (GenericRecord) data;
			
			Object modeObject = 
				dataRecord.get(MobilityColumnKey.MODE.toString(false));
			if(modeObject instanceof Utf8) {
				mode = 
					Mode.valueOf(((Utf8) modeObject).toString().toUpperCase());
			}
			else {
				throw new DomainException("The mode is not a string.");
			}
			
			if(SubType.MODE_ONLY.equals(subType)) {
				sensorData = null;
			}
			else if(SubType.SENSOR_DATA.equals(subType)) {
				sensorData = new SensorData(mode, dataRecord);
			}
			else {
				throw new DomainException(
					"A sub-type was added, but not fully implemented.");
			}
		}
		else {
			throw new DomainException("The record is malformed.");
		}
		
		classifierData = null;
	}
	
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
		
		String idString;
		try {
			idString = 
					mobilityPoint.getString(
							MobilityColumnKey.ID.toString(false));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.MOBILITY_INVALID_ID, 
					"The Mobility point's ID is missing: " +
							MobilityColumnKey.ID.toString(false), 
					e);
		}
		try {
			id = UUID.fromString(idString);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.MOBILITY_INVALID_ID, 
					"The Mobility point's ID is not a valid UUID: " + idString, 
					e);
		}
		
		// Get the time.
		long tTime;
		try {
			tTime = mobilityPoint.getLong(
							MobilityColumnKey.TIME.toString(false));
		}
		catch(JSONException outerException) {
			try {
				tTime = mobilityPoint.getLong(
								MobilityColumnKey.TIME.toString(true));
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIME, 
						"The time is missing: " + 
								MobilityColumnKey.TIME.toString(false), 
						innerException);
			}
		}
		time = tTime;
		
		// Get the timezone.
		DateTimeZone tTimezone;
		try {
			tTimezone = 
					TimeUtils.getDateTimeZoneFromString(
							mobilityPoint.getString(
									MobilityColumnKey.TIMEZONE.toString(
											false)));
		}
		catch(JSONException outerException) {
			try {
				tTimezone = 
						TimeUtils.getDateTimeZoneFromString(
								mobilityPoint.getString(
										MobilityColumnKey.TIMEZONE.toString(
												true)));
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMEZONE, 
						"The timezone is missing: " + 
								MobilityColumnKey.TIMEZONE.toString(
										false), 
						innerException);
			}
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
				ErrorCode.SERVER_INVALID_TIMEZONE,
				"The time zone is unknown.",
				e);
		}
		timezone = tTimezone;
		
		// Get the location status.
		String locationStatusString;
		try {
			locationStatusString = 
					mobilityPoint.getString(
							LocationColumnKey.STATUS.toString(false)
					).toUpperCase();
		}
		catch(JSONException outerException) {
			try {
				locationStatusString =
						mobilityPoint.getString(
								LocationColumnKey.STATUS.toString(true)
						).toUpperCase();
			}
			catch(JSONException innerException) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
						"The location status is missing: " +
								LocationColumnKey.STATUS.toString(false), 
						innerException);
			}
		}
		try {
			locationStatus = LocationStatus.valueOf(locationStatusString);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"The location status is unknown: " + locationStatusString, 
					e);
		}
		
		// Get the location.
		Location tLocation;
		try {
			tLocation = 
					new Location(
							mobilityPoint.getJSONObject(
									MobilityColumnKey.LOCATION.toString(
											false)),
							timezone);
		}
		catch(JSONException outerException) {
			try {
				tLocation = 
						new Location(
								mobilityPoint.getJSONObject(
										MobilityColumnKey.LOCATION.toString(
												true)),
								timezone);
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
							"The location is missing: " +
									MobilityColumnKey.LOCATION.toString(
											false), 
							innerException);
				}
			}
		}
		location = tLocation;
		
		// Get the subtype.
		String subTypeString;
		try {
			subTypeString = 
					mobilityPoint.getString(
							MobilityColumnKey.SUB_TYPE.toString(false)
					).toUpperCase();
		}
		catch(JSONException notLong) {
			try {
				subTypeString = 
						mobilityPoint
							.getString(
									MobilityColumnKey.SUB_TYPE.toString(true)
							).toUpperCase();
			}
			catch(JSONException notShort) {
				throw new DomainException(
						ErrorCode.MOBILITY_INVALID_SUBTYPE, 
						"The subtype is missing: " +
								MobilityColumnKey.SUB_TYPE.toString(false), 
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
				modeString = 
						mobilityPoint.getString(
								MobilityColumnKey.MODE.toString(false));
			}
			catch(JSONException outerException) {
				try {
					modeString = 
							mobilityPoint.getString(
									MobilityColumnKey.MODE.toString(true));
				}
				catch(JSONException innerException) {
					throw new DomainException(
							ErrorCode.MOBILITY_INVALID_MODE, 
							"The subtype is '" + 
								SubType.MODE_ONLY.toString().toLowerCase() + 
								"', but the required key is missing: " +
								MobilityColumnKey.MODE.toString(false), 
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
				sensorData = 
						new SensorData(
								mobilityPoint.getJSONObject(
										MobilityColumnKey.SENSOR_DATA.toString(
												true)));
				mode = sensorData.mode;
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_JSON, 
						"The subtype is '" + 
							SubType.SENSOR_DATA.toString().toLowerCase() + 
							"', but the required key is missing: " + 
							MobilityColumnKey.SENSOR_DATA.toString(
									true),
						e);
			}
			break;
			
		default:
			// If we accepted the subtype then we must know what it is, but we
			// may not need to look for any further data based on that subtype.
			mode = null;
			sensorData = null;
		}
		
		// Attempt to retrieve the classifier data.
		ClassifierData tClassifierData = null;
		try {
			tClassifierData = 
					new ClassifierData(
						mode,
						mobilityPoint.getJSONObject(
							MobilityColumnKey.CLASSIFIER_DATA.toString(
								false)));
		}
		catch(JSONException notRegular) {
			try {
				tClassifierData = 
						new ClassifierData(
							mode,
							mobilityPoint.getJSONObject(
								MobilityColumnKey.CLASSIFIER_DATA.toString(
									true)));
			}
			catch(JSONException notShort) {
				// The classifier data is optional, so it may not exist.
			}
		}
		classifierData = tClassifierData;
		
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
			final DateTimeZone timezone,
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
			this.location = new Location(location, timezone);
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
			final DateTimeZone timezone,
			final LocationStatus locationStatus, 
			final Location location, 
			final Mode mode, 
			final SensorData sensorData,
			final ClassifierData classifierData) 
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
			this.classifierData = classifierData;
		}
		else {
			subType = SubType.SENSOR_DATA;
			
			this.sensorData = sensorData;
			this.classifierData = classifierData;
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
	public final DateTimeZone getTimezone() {
		return timezone;
	}
	
	/**
	 * Constructs a new DateTime with the time and time zone of this point.
	 * 
	 * @return The DateTime of this point.
	 */
	public final DateTime getDate() {
		return new DateTime(time, timezone);
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
			result.add(
					new Sample(
						dataPoint.getX(), 
						dataPoint.getY(), 
						dataPoint.getZ()));
		}
		return result;
	}

	/**
	 * Returns the WiFi scan from this data point if applicable. It is 
	 * applicable only if this point is {@link SubType#SENSOR_DATA} and the 
	 * mode is not {@link Mode#ERROR}.
	 * 
	 * @return The WifiScan from this data point.
	 * 
	 * @throws DomainException This call is not valid for this point.
	 */
	public final WifiScan getWifiScan() throws DomainException {
		if(! SubType.SENSOR_DATA.equals(subType)) {
			throw new DomainException(
					"There is no WiFi scan for Mobility points that are not of the subtype " + 
						SubType.SENSOR_DATA.toString());
		}
		if(Mode.ERROR.equals(mode)) {
			throw new DomainException(
					"There is now WiFi scan for Mobility points that are of the mode " + 
						Mode.ERROR.toString());
		}
		if(sensorData.wifiData == null) {
			throw new DomainException(
					"There was no WiFi data generated for this point.");
		}

		Map<String, Double> scan = sensorData.wifiData.scan;
		List<AccessPoint> accessPoints = 
				new ArrayList<AccessPoint>(scan.size());
		
		for(String ssid : scan.keySet()) {
			accessPoints.add(new AccessPoint(ssid, scan.get(ssid)));
		}
		
		return new WifiScan(sensorData.wifiData.time, accessPoints);
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
			final Double average, 
			final Mode mode) 
			throws DomainException {
			
		classifierData = new ClassifierData(fft, variance, average, mode);
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MobilityPoint point) {
		// Null items will always be pushed to the end of the list. 
		if(point == null) {
			return -1;
		}

		if(time > point.time) {
			return 1;
		}
		else if(time < point.time) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Validates a string that represents some key related to Mobility, this
	 * could be a Mobility key, a Location key, or any of Mobility inner class
	 * keys, and converts it to the appropriate key.
	 * 
	 * @param key The key string to be validated.
	 * 
	 * @return A ColumnKey representing the appropriate key.
	 * 
	 * @throws DomainException The key was null or could not be decoded.
	 */
	public static List<ColumnKey> validateKeyString(
			final String key) 
			throws DomainException {
		
		if(key == null) {
			throw new DomainException("The key cannot be null.");
		}
		
		try {
			if(key.startsWith(MobilityColumnKey.NAMESPACE)) {
				return MobilityColumnKey.valueOfString(key);
			}
			else if(key.startsWith(LocationColumnKey.NAMESPACE)) {
				return LocationColumnKey.valueOfString(key);
			}
			else if(key.startsWith(SensorDataColumnKey.NAMESPACE)) {
				return SensorDataColumnKey.valueOfString(key);
			}
			else if(key.startsWith(AccelDataColumnKey.NAMESPACE)) {
				return AccelDataColumnKey.valueOfString(key);
			}
			else if(key.startsWith(WifiDataColumnKey.NAMESPACE)) {
				return WifiDataColumnKey.valueOfString(key);
			}
			else if(key.startsWith(ClassifierDataColumnKey.NAMESPACE)) {
				return ClassifierDataColumnKey.valueOfString(key);
			}
			else {
				throw new DomainException("The key is unknown: " + key);
			}
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The key is unknown: " + key, e);
		}
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
			final Collection<ColumnKey> columns)
			throws JSONException, DomainException {
		
		if(columns == null) {
			throw new DomainException("The list of columns cannot be null.");
		}
		
		JSONObject result = new JSONObject();
		
		if(columns.contains(MobilityColumnKey.ID)) {
			result.put(MobilityColumnKey.ID.toString(abbreviated), id);
		}
		
		if(columns.contains(MobilityColumnKey.MODE)) {
			result.put(
					MobilityColumnKey.MODE.toString(abbreviated), 
					mode.toString().toLowerCase());
		}
	
		if(columns.contains(MobilityColumnKey.TIME)) {
			result.put(MobilityColumnKey.TIME.toString(abbreviated), time);
		}
	
		if(columns.contains(MobilityColumnKey.TIMESTAMP)) {
			DateTime dateTime = new DateTime(time, timezone);
			DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
			builder.appendPattern(DATE_TIME_FORMAT);

			result.put(
					MobilityColumnKey.TIMESTAMP.toString(abbreviated),
					builder.toFormatter().print(dateTime));
		}

		if(columns.contains(MobilityColumnKey.TIMEZONE)) {
			result.put(
					MobilityColumnKey.TIMEZONE.toString(abbreviated), 
					timezone.getID());
		}

		if(columns.contains(MobilityColumnKey.SUB_TYPE)) {
			result.put(
					MobilityColumnKey.SUB_TYPE.toString(abbreviated), 
					subType.toString().toLowerCase());
		}
		
		if(columns.contains(LocationColumnKey.STATUS)) {
			result.put(
					LocationColumnKey.STATUS.toString(abbreviated), 
					locationStatus.toString().toLowerCase());
		}

		if(columns.contains(MobilityColumnKey.LOCATION) ||
				LocationColumnKey.containsLocationColumnKey(columns)) {
			if(location != null) {
				result.put(
						MobilityColumnKey.LOCATION.toString(abbreviated), 
						location.toJson(abbreviated, columns));
			}
		}

		if(SubType.SENSOR_DATA.equals(subType)) {
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				SensorDataColumnKey.containsSensorDataColumnKey(columns)) {
			
				result.put(
						MobilityColumnKey.SENSOR_DATA.toString(abbreviated), 
						sensorData.toJson(abbreviated, columns));
			}
		
			if((columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				ClassifierDataColumnKey.containsClassifierDataColumnKey(columns)) &&
				(classifierData != null)){
				
				result.put(
					MobilityColumnKey.CLASSIFIER_DATA.toString(abbreviated), 
					classifierData.toJson(abbreviated, columns));
			}
		}
		
		return result;
	}
	
	/**
	 * Populates the indices in 'result' with a value referenced by 'columns'.
	 * For example, if the third value in 'columns' is Mobility's time column
	 * key, the third value in the result list will be set to this Mobility
	 * point's time. This means that the result list must already be populated
	 * with exactly as many values as exist in 'columns'.
	 *
	 * @param columns The columns to populate in the result list.
	 * 
	 * @param result The result list whose values will be overridden by their
	 * 				 values in this Mobility point in their respective  
	 * 				 indices.
	 * 
	 * @throws DomainException The columns or result list was null or weren't
	 * 						   the same length.
	 */
	public final void toCsvRow(
			final List<ColumnKey> columns, 
			final List<Object> result)
			throws DomainException {
		
		if(columns == null) {
			throw new DomainException(
					"The list of columns cannot be null.");
		}
		else if(result == null) {
			throw new DomainException(
					"The list of results cannot be null.");
		}
		else if(columns.size() != result.size()) {
			throw new DomainException(
					"The columns list and the result list were different lengths.");
		}
		
		int index;
		
		if((index = columns.indexOf(MobilityColumnKey.ID)) != -1) {
			result.set(index, id);
		}
		
		if((index = columns.indexOf(MobilityColumnKey.MODE)) != -1) {
			result.set(index, mode.toString().toLowerCase());
		}
		
		if((index = columns.indexOf(MobilityColumnKey.TIME)) != -1) {
			result.set(index, time);
		}
		
		if((index = columns.indexOf(MobilityColumnKey.TIMESTAMP)) != -1) {
			DateTime dateTime = new DateTime(time, timezone);
			DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
			builder.appendPattern(DATE_TIME_FORMAT);
			
			result.set(index, builder.toFormatter().print(dateTime));
		}
		
		if((index = columns.indexOf(MobilityColumnKey.TIMEZONE)) != -1) {
			result.set(index, timezone.getID());
		}
		
		if((index = columns.indexOf(MobilityColumnKey.SUB_TYPE)) != -1) {
			result.set(index, subType.toString().toLowerCase());
		}
		
		if((index = columns.indexOf(LocationColumnKey.STATUS)) != -1) {
			result.set(index, locationStatus.toString().toLowerCase());
		}
		
		if(columns.contains(MobilityColumnKey.LOCATION) ||
				LocationColumnKey.containsLocationColumnKey(columns)) {
			
			if(location != null) {
				location.toCsvRow(columns, result);
			}
		}
		
		if(SubType.SENSOR_DATA.equals(subType)) {
			if(columns.contains(MobilityColumnKey.SENSOR_DATA) ||
				SensorDataColumnKey.containsSensorDataColumnKey(columns)) {
			
				sensorData.toCsvRow(columns, result);
			}
			
			if(columns.contains(MobilityColumnKey.CLASSIFIER_DATA) ||
				ClassifierDataColumnKey.containsClassifierDataColumnKey(columns)) {
				
				classifierData.toCsvRow(columns, result);
			}
		}
	}
}