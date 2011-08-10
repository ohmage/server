package org.ohmage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for individual Mobility data point.
 * 
 * @author John Jenkins
 */
public class MobilityInformation {
	private static final String JSON_KEY_DATE = "date";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION = "location";

	private static final String JSON_KEY_SUBTYPE = "subtype";
	
	// Mode-only
	private static final String JSON_KEY_MODE = "mode";
	
	// Sensor data
	private static final String JSON_KEY_DATA = "data";
	
	private static enum Mode { STILL, WALK, RUN, BIKE, DRIVE };
	
	private final Date date;
	private final long time;
	private final TimeZone timezone;
	
	private final String locationStatus;
	/**
	 * This class contains all of the information associated with a location
	 * record.
	 * 
	 * @author John Jenkins
	 */
	private final class Location {
		private static final String JSON_KEY_LATITUDE = "latitude";
		private static final String JSON_KEY_LONGITUDE = "longitude";
		private static final String JSON_KEY_ACCURACY = "accuracy";
		private static final String JSON_KEY_PROVIDER = "provider";
		private static final String JSON_KEY_TIMESTAMP = "timestamp";
		
		private final Double latitude;
		private final Double longitude;
		private final Double accuracy;
		private final String provider;
		private final Date timestamp;
		
		/**
		 * Creates a new Location object.
		 * 
		 * @param locationData A JSONObject representing all of the data for a
		 * 					   Location object.
		 * 
		 * @throws IllegalArgumentException Thrown if the location data is 
		 * 									null, isn't a valid JSONObject, 
		 * 									doesn't contain all of the required
		 * 									information, or any of the 
		 * 									information is invalid for its 
		 * 									type.
		 */
		private Location(JSONObject locationData) {
			if(locationData == null) {
				throw new IllegalArgumentException("The location data is missing.");
			}
			
			try {
				latitude = locationData.getDouble(JSON_KEY_LATITUDE);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The latitude is missing or invalid.");
			}
			
			try {
				longitude = locationData.getDouble(JSON_KEY_LONGITUDE);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The longitude is missing or invalid.");
			}

			try {
				accuracy = locationData.getDouble(JSON_KEY_ACCURACY);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The accuracy is missing or invalid.");
			}
			
			try {
				provider = locationData.getString(JSON_KEY_PROVIDER);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The provider is missing.");
			}
			
			try {
				timestamp = StringUtils.decodeDateTime(locationData.getString(JSON_KEY_TIMESTAMP));
				
				if(timestamp == null) {
					throw new IllegalArgumentException("The timestamp is invalid.");
				}
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The timestamp is missing.");
			}
		}

		/**
		 * Returns the latitude of this location.
		 * 
		 * @return The latitude of this location.
		 */
		public final double getLatitude() {
			return latitude;
		}

		/**
		 * Returns the longitude of this location.
		 * 
		 * @return The longitude of this location.
		 */
		public final double getLongitude() {
			return longitude;
		}

		/**
		 * Returns the accuracy of this location.
		 * 
		 * @return The accuracy of this location.
		 */
		public final double getAccuracy() {
			return accuracy;
		}

		/**
		 * Returns the provider of this location information.
		 * 
		 * @return The provider of this location information.
		 */
		public final String getProvider() {
			return provider;
		}

		/**
		 * Returns the timestamp for when this information was gathered.
		 * 
		 * @return The timestamp for when this information was gathered.
		 */
		public final Date getTimestamp() {
			return timestamp;
		}
	}
	private final Location location;

	private static enum SubType { MODE_ONLY, SENSOR_DATA };
	private final SubType subType;
	
	// Mode-only
	private final Mode mode;
	
	// Sensor data
	/**
	 * This class is responsible for managing the sensor data in a single
	 * Mobility entry.
	 * 
	 * @author John Jenkins
	 */
	private final class SensorData {
		private static final String JSON_KEY_MODE = "mode";
		private static final String JSON_KEY_SPEED = "speed";
		
		private static final String JSON_KEY_ACCEL_DATA = "accel_data";
		private static final String JSON_KEY_ACCEL_DATA_X = "x";
		private static final String JSON_KEY_ACCEL_DATA_Y = "y";
		private static final String JSON_KEY_ACCEL_DATA_Z = "z";
		
		private static final String JSON_KEY_WIFI_DATA = "wifi_data";
		private static final String JSON_KEY_WIFI_DATA_TIMESTAMP = "timestamp";
		private static final String JSON_KEY_WIFI_DATA_SCAN = "scan";
		
		private final Mode mode;
		private final Double speed;
		
		/**
		 * This class is responsible for managing tri-axle acceleration data
		 * points.
		 * 
		 * @author John Jenkins
		 */
		private final class AccelData {
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
			private AccelData(Double x, Double y, Double z) {
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
		}
		private final List<AccelData> accelData;
		
		/**
		 * This class is responsible for the WifiData in a sensor data upload.
		 * 
		 * @author John Jenkins
		 */
		private final class WifiData {
			private static final String JSON_KEY_SSID = "ssid";
			private static final String JSON_KEY_STRENGTH = "strength";
			
			private final Date timestamp;
			private final Map<String, Double> scan;
			
			/**
			 * Creates a WifiData point with a timestamp and all of the scan
			 * information.
			 * 
			 * @param timestamp A String representing the date and time that 
			 * 					this record was made.
			 * 
			 * @param scan The data collected from all of the WiFi points in 
			 * 			   range of the scan.
			 * 
			 * @throws IllegalArgumentException Thrown if either of the 
			 * 									parameters are null or invalid
			 * 									values.
			 */
			private WifiData(String timestamp, JSONArray scan) {
				// Validate the timestamp value.
				if(timestamp == null) {
					throw new IllegalArgumentException("The timestamp is missing.");
				}
				else if((this.timestamp = StringUtils.decodeDate(timestamp)) == null) {
					throw new IllegalArgumentException("The timestamp is invalid.");
				}
				
				// Validate the scan value.
				if(scan == null) {
					throw new IllegalArgumentException("The scan is missing.");
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
							catch(JSONException e) {
								throw new IllegalArgumentException("The SSID is missing.", e);
							}
							
							// Get the strength.
							Double strength;
							try {
								strength = jsonObject.getDouble(JSON_KEY_STRENGTH);
							}
							catch(JSONException e) {
								throw new IllegalArgumentException("The strength is missing or invalid.", e);
							}
							
							// Add them to the map.
							this.scan.put(ssid, strength);
						}
						catch(JSONException e) {
							throw new IllegalArgumentException("The scan is invalid.", e);
						}
					}
				}
			}

			/**
			 * Returns the timestamp representing when this scan was performed.
			 * 
			 * @return The timestamp representing when this scan was performed.
			 */
			public final Date getTimestamp() {
				return timestamp;
			}

			/**
			 * Returns an immutable copy of the scan.
			 * 
			 * @return An immutable copy of the scan.
			 */
			public final Map<String, Double> getScan() {
				return Collections.unmodifiableMap(scan);
			}
		}
		private final WifiData wifiData;
		
		/**
		 * Creates a new SensorData point from a JSONObject of sensor data.
		 * 
		 * @param sensorData The sensor data as a JSONObject.
		 * 
		 * @throws IllegalArgumentException Thrown if the sensor data is null,
		 * 									not valid sensor data, or missing
		 * 									some component.
		 */
		private SensorData(JSONObject sensorData) {
			if(sensorData == null) {
				throw new IllegalArgumentException("The sensor data is missing.");
			}
			
			// Get the mode.
			try {
				mode = Mode.valueOf(sensorData.getString(JSON_KEY_MODE).toUpperCase());
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The mode is missing.");
			}
			catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("The mode is not a known mode.", e);
			}
			
			// Get the speed.
			try {
				speed = sensorData.getDouble(JSON_KEY_SPEED);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The speed is missing or invalid.", e);
			}
			
			// Get the accelerometer data.
			try {
				JSONArray accelDataJson = sensorData.getJSONArray(JSON_KEY_ACCEL_DATA);
				int numAccelDataPoints = accelDataJson.length();
				
				// Create the resulting list and cycle through the 
				// JSONArray adding each of the entries.
				accelData = new ArrayList<AccelData>(numAccelDataPoints);
				for(int i = 0; i < numAccelDataPoints; i++) {
					try {
						JSONObject accelDataPointJson = accelDataJson.getJSONObject(i);
						
						// Get the x-acceleration.
						Double x;
						try {
							x = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_X);
						}
						catch(JSONException e) {
							throw new IllegalArgumentException("The 'x' point was missing or invalid.", e);
						}
						
						// Get the y-acceleration.
						Double y;
						try {
							y = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Y);
						}
						catch(JSONException e) {
							throw new IllegalArgumentException("The 'y' point was missing or invalid.", e);
						}
						
						// Get the z-acceleration.
						Double z;
						try {
							z = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Z);
						}
						catch(JSONException e) {
							throw new IllegalArgumentException("The 'z' point was missing or invalid.", e);
						}
						
						// Add a new point.
						accelData.add(new AccelData(x, y, z));
					}
					catch(JSONException e) {
						throw new IllegalArgumentException("An accelerometer data point is not a JSONObject.", e);
					}
				}
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The accelerometer data is missing or invalid.", e);
			}
			
			// Get the WiFi data.
			try {
				JSONObject wifiDataJson = sensorData.getJSONObject(JSON_KEY_WIFI_DATA);
				
				// Get the timestamp.
				String timestamp;
				try {
					timestamp = wifiDataJson.getString(JSON_KEY_WIFI_DATA_TIMESTAMP);
				}
				catch(JSONException e) {
					throw new IllegalArgumentException("The timestamp is missing.", e);
				}
				
				// Get the scan.
				JSONArray scan;
				try {
					scan = wifiDataJson.getJSONArray(JSON_KEY_WIFI_DATA_SCAN);
				}
				catch(JSONException e) {
					throw new IllegalArgumentException("The scan is missing.", e);
				}
				
				// Set the WifiData.
				wifiData = new WifiData(timestamp, scan);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The WiFi data is missing or invalid.", e);
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
		 * @return The record's WifiData.
		 */
		public final WifiData getWifiData() {
			return wifiData;
		}
	}
	private final SensorData sensorData; 
	
	/**
	 * Creates a MobilityInformation object that represents all of the 
	 * information in the parameterized Mobility data point.
	 * 
	 * @param mobilityPoint A JSONObject that contains all of the required
	 * 						information for a Mobility data point.
	 * 
	 * @throws IllegalArgumentException Thrown if Mobility point is null, 
	 * 									invalid, or contains insufficient 
	 * 									information to build this object.
	 */
	public MobilityInformation(JSONObject mobilityPoint) {
		// Get the date.
		try {
			date = StringUtils.decodeDateTime(mobilityPoint.getString(JSON_KEY_DATE));
			
			if(date == null) {
				throw new IllegalArgumentException("The date is invalid.");
			}
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The date is missing.", e);
		}
		
		// Get the time.
		try {
			time = mobilityPoint.getLong(JSON_KEY_TIME);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The time is missing.", e);
		}
		
		// Get the timezone.
		// FIXME: This doesn't validate the timezone and instead just defaults
		// to GMT.
		try {
			timezone = TimeZone.getTimeZone(mobilityPoint.getString(JSON_KEY_TIMEZONE));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The timezone is missing.", e);
		}
		
		// Get the location status.
		try {
			locationStatus = mobilityPoint.getString(JSON_KEY_LOCATION_STATUS);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The location status is missing.", e);
		}
		
		// Get the location.
		try {
			location = new Location(mobilityPoint.getJSONObject(JSON_KEY_LOCATION));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The location is missing.", e);
		}
		
		// Get the subtype.
		try {
			subType = SubType.valueOf(mobilityPoint.getString(JSON_KEY_SUBTYPE));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The subtype is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The subtype is an unknown type.", e);
		}
		
		// Based on the subtype, get the mode or sensor data.
		switch(subType) {
		case MODE_ONLY:
			try {
				mode = Mode.valueOf(mobilityPoint.getString(JSON_KEY_MODE));
				sensorData = null;
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The subtype is '" + SubType.MODE_ONLY.toString().toLowerCase() + 
						"', but the required key is missing: " + JSON_KEY_MODE, e);
			}
			catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("The mode is unknown.", e);
			}
			break;
			
		case SENSOR_DATA:
			try {
				mode = null;
				sensorData = new SensorData(mobilityPoint.getJSONObject(JSON_KEY_DATA));
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("The subtype is '" + SubType.SENSOR_DATA.toString().toLowerCase() +
						"', but the required key is missing: " + JSON_KEY_DATA, e);
			}
			break;
			
		default:
			// If we accepted the subtype then we must know what it is, but we
			// may not need to look for any further data based on that subtype.
			mode = null;
			sensorData = null;
		}
	}
}