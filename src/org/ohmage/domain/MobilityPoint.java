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
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityPoint.SensorData.AccelData;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

import edu.ucla.cens.mobilityclassifier.Sample;

/**
 * This class is responsible for individual Mobility data points.
 * 
 * @author John Jenkins
 */
public class MobilityPoint {
	private static final String JSON_KEY_DATE = "date";
	private static final String JSON_KEY_DATE_SHORT = "ts";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIME_SHORT = "t";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_TIMEZONE_SHORT = "tz";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION_STATUS_SHORT = "ls";
	private static final String JSON_KEY_LOCATION = "location";
	private static final String JSON_KEY_LOCATION_SHORT = "l";

	public static final String JSON_KEY_SUBTYPE = "subtype";
	
	// Mode-only
	private static final String JSON_KEY_MODE = "mode";
	private static final String JSON_KEY_MODE_SHORT = "m";
	
	// Sensor data
	private static final String JSON_KEY_DATA = "data";
	
	private final Date date;
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
	
	public static enum Mode { STILL, WALK, RUN, BIKE, DRIVE };
	
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
		public static final class AccelData {
			private final double x;
			private final double y;
			private final double z;
			
			/**
			 * Creates a tri-axle acceleration data point.
			 * 
			 * @param x The x-acceleration of the point.
			 * 
			 * @param y The y-acceleration of the point.
			 * 
			 * @param z The z-acceleration of the point.
			 */
			public AccelData(final double x, final double y, final double z) {
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
			 * @return Returns a JSONObject representing this object unless 
			 * 		   there is an error in which case null is returned.
			 */
			public final JSONObject toJson() {
				try {
					JSONObject result = new JSONObject();
					
					result.put(JSON_KEY_ACCEL_DATA_X, x);
					result.put(JSON_KEY_ACCEL_DATA_Y, y);
					result.put(JSON_KEY_ACCEL_DATA_Z, z);
					
					return result;
				}
				catch(JSONException e) {
					return null;
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
			 * @throws MobilityException Thrown if either of the parameters are
			 * 							 null or invalid values.
			 */
			private WifiData(String timestamp, JSONArray scan) throws ErrorCodeException {
				// Validate the timestamp value.
				if(timestamp == null) {
					throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is missing.");
				}
				else if((this.timestamp = StringUtils.decodeDate(timestamp)) == null) {
					throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is invalid.");
				}
				
				// Validate the scan value.
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
							throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_WIFI_DATA, "The SSID is missing.", e);
						}
						
						// Get the strength.
						Double strength;
						try {
							strength = jsonObject.getDouble(JSON_KEY_STRENGTH);
						}
						catch(JSONException e) {
							throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_WIFI_DATA, "The strength is missing or invalid.", e);
						}
						
						// Add them to the map.
						this.scan.put(ssid, strength);
					}
					catch(JSONException e) {
						throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_WIFI_DATA, "The scan is invalid.", e);
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
			 * @throws IllegalArgumentException Thrown if the timestamp or 
			 * 									scans map are null.
			 */
			public WifiData(final Date timestamp, 
					final Map<String, Double> scans) {
				
				if(timestamp == null) {
					throw new IllegalArgumentException("The timestamp cannot be null.");
				}
				else if(scans == null) {
					throw new IllegalArgumentException("The map of scans cannot be null.");
				}
				
				this.timestamp = timestamp;
				this.scan = scans;
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
			
			/**
			 * Creates a JSONObject that represents the information in this
			 * object.
			 * 
			 * @return Returns a JSONObject that represents this object or null
			 * 		   if there is an error.
			 */
			public final JSONObject toJson() {
				try {
					JSONObject result = new JSONObject();
					
					result.put(JSON_KEY_WIFI_DATA_TIMESTAMP, TimeUtils.getIso8601DateTimeString(timestamp));
					
					JSONArray scanJson = new JSONArray();
					for(String ssid : scan.keySet()) {
						JSONObject currScan = new JSONObject();
						currScan.put(JSON_KEY_SSID, ssid);
						currScan.put(JSON_KEY_STRENGTH, scan.get(ssid));
						
						scanJson.put(currScan);
					}
					result.put(JSON_KEY_WIFI_DATA_SCAN, scanJson);
					
					return result;
				}
				catch(JSONException e) {
					return null;
				}
			}
		}
		private final WifiData wifiData;
		
		/**
		 * Creates a new SensorData point from a JSONObject of sensor data.
		 * 
		 * @param sensorData The sensor data as a JSONObject.
		 * 
		 * @throws MobilityException Thrown if the sensor data is null, not 
		 * 							 valid sensor data, or missing some 
		 * 							 component.
		 */
		private SensorData(JSONObject sensorData) throws ErrorCodeException {
			// Get the mode.
			try {
				mode = Mode.valueOf(sensorData.getString(JSON_KEY_MODE).toUpperCase());
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_MODE, "The mode is missing.", e);
			}
			catch(IllegalArgumentException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_MODE, "The mode is not a known mode.", e);
			}
			
			// Get the speed.
			try {
				speed = sensorData.getDouble(JSON_KEY_SPEED);
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_SPEED, "The speed is missing or invalid.", e);
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
							throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_ACCELEROMETER_DATA, "The 'x' point was missing or invalid.", e);
						}
						
						// Get the y-acceleration.
						Double y;
						try {
							y = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Y);
						}
						catch(JSONException e) {
							throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_ACCELEROMETER_DATA, "The 'y' point was missing or invalid.", e);
						}
						
						// Get the z-acceleration.
						Double z;
						try {
							z = accelDataPointJson.getDouble(JSON_KEY_ACCEL_DATA_Z);
						}
						catch(JSONException e) {
							throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_ACCELEROMETER_DATA, "The 'z' point was missing or invalid.", e);
						}
						
						// Add a new point.
						accelData.add(new AccelData(x, y, z));
					}
					catch(JSONException e) {
						throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_ACCELEROMETER_DATA, "An accelerometer data point is not a JSONObject.", e);
					}
				}
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_ACCELEROMETER_DATA, "The accelerometer data is missing or invalid.", e);
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
					throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is missing.", e);
				}
				
				// Get the scan.
				JSONArray scan;
				try {
					scan = wifiDataJson.getJSONArray(JSON_KEY_WIFI_DATA_SCAN);
				}
				catch(JSONException e) {
					throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_WIFI_DATA, "The scan is missing.", e);
				}
				
				// Set the WifiData.
				wifiData = new WifiData(timestamp, scan);
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_WIFI_DATA, "The WiFi data is missing or invalid.", e);
			}
		}
		
		/**
		 * Creates a new SensorData object that represents the information 
		 * collected by Mobility when a Mobility point was generated.
		 * 
		 * @param mode The mode calculated based on this information.
		 * 
		 * @param speed The speed of the device when this point was made.
		 * 
		 * @param accelData The accelerometer information collected during this
		 * 					reading.
		 * 
		 * @param wifiData The WiFi information collected when this point was
		 * 				   made.
		 * 
		 * @throws IllegalArgumentException Thrown if the mode, accelerometer
		 * 									data, or WiFi data are null.
		 */
		public SensorData(final Mode mode, final double speed, 
				final List<AccelData> accelData, final WifiData wifiData) {
			
			if(mode == null) {
				throw new IllegalArgumentException("The mode cannot be null.");
			}
			else if(accelData == null) {
				throw new IllegalArgumentException("The accelerometer data cannot be null.");
			}
			else if(wifiData == null) {
				throw new IllegalArgumentException("The WiFi data cannot be null.");
			}

			this.mode = mode;
			this.speed = speed;
			this.accelData = accelData;
			this.wifiData = wifiData;
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
		
		/**
		 * Creates a JSONObject that represents the information in this object.
		 * 
		 * @return Returns a JSONObject that represents this object or null if
		 * 		   there is an error.
		 */
		public final JSONObject toJson() {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_MODE, mode.name().toLowerCase());
				result.put(JSON_KEY_SPEED, speed);
				result.put(JSON_KEY_WIFI_DATA, wifiData.toJson());
				
				JSONArray accelArray = new JSONArray();
				for(AccelData accelRecord : accelData) {
					accelArray.put(accelRecord.toJson());
				}
				result.put(JSON_KEY_ACCEL_DATA, accelArray);
				
				return result;
			}
			catch(JSONException e) {
				return null;
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
		 */
		private ClassifierData(Mode mode) {
			if(mode == null) {
				throw new IllegalArgumentException("The mode cannot be null.");
			}
			
			this.fft = null;
			this.variance = null;
			this.n95Variance = null;
			this.average = null;
			this.mode = mode;
		}
		
		/**
		 * Creates a ClassifierData object that contains all of the applicable
		 * keys from the JSONObject. The only required key is the mode.
		 * 
		 * @param classifierData The classifier data as a JSONObject.
		 * 
		 * @throws MobilityException Thrown if the mode is missing or unknown.
		 */
		private ClassifierData(JSONObject classifierData) throws ErrorCodeException{
			try {
				mode = Mode.valueOf(classifierData.getString(JSON_KEY_MODE).toUpperCase());
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_MODE, "The mode is missing.", e);
			}
			catch(IllegalArgumentException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_MODE, "The mode is unknown.", e);
			}
			
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
		 * @throws IllegalArgumentException Thrown if any of the values are 
		 * 									null.
		 */
		private ClassifierData(List<Double> fft, Double variance,
				Double n95Variance,
				Double average, Mode mode) {
			if(mode == null) {
				throw new IllegalArgumentException("The mode cannot be null.");
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
		 * @return A JSONObject representing this object or null if there is an
		 * 		   error.
		 */
		public final JSONObject toJson() {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_FFT, fft);
				result.put(JSON_KEY_VARIANCE, variance);
				
				result.put(JSON_KEY_N95_VARIANCE, n95Variance);
				
				result.put(JSON_KEY_AVERAGE, average);
				result.put(JSON_KEY_MODE, mode.name().toLowerCase());
				
				return result;
			}
			catch(JSONException e) {
				return null;
			}
		}
	}
	private ClassifierData classifierData;
	
	/**
	 * Creates a Mobility object that represents all of the 
	 * information in the parameterized Mobility data point.
	 * 
	 * @param mobilityPoint A JSONObject that contains all of the required
	 * 						information for a Mobility data point.
	 * 
	 * @throws MobilityException Thrown if Mobility point is null, invalid, or
	 * 							 contains insufficient information to build 
	 * 							 this object.
	 */
	public MobilityPoint(JSONObject mobilityPoint, PrivacyState privacyState) 
			throws ErrorCodeException {
		
		// Get the date.
		Date tDate;
		try {
			tDate = StringUtils.decodeDateTime(mobilityPoint.getString(JSON_KEY_DATE));
			
			if(tDate == null) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The date is invalid.");
			}
		}
		catch(JSONException outerException) {
			try {
				tDate = StringUtils.decodeDateTime(mobilityPoint.getString(JSON_KEY_DATE_SHORT));
			}
			catch(JSONException innerException) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The date is missing.", innerException);
			}
		}
		date = tDate;
		
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
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIME, "The time is missing.", innerException);
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
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMEZONE, "The timezone is missing.", innerException);
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
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, "The location status is missing.", innerException);
			}
		}
		catch(IllegalArgumentException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, "The location status is unknown.", e);
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
					throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The location is missing.", innerException);
				}
			}
		}
		location = tLocation;
		
		// Get the subtype.
		try {
			subType = SubType.valueOf(mobilityPoint.getString(JSON_KEY_SUBTYPE).toUpperCase());
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_SUBTYPE, "The subtype is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_SUBTYPE, "The subtype is an unknown type.", e);
		}
		
		// Based on the subtype, get the mode or sensor data.
		switch(subType) {
		case MODE_ONLY:
			Mode tMode;
			try {
				tMode = Mode.valueOf(mobilityPoint.getString(JSON_KEY_MODE).toUpperCase());
			}
			catch(JSONException outerException) {
				try {
					tMode = Mode.valueOf(mobilityPoint.getString(JSON_KEY_MODE_SHORT).toUpperCase());
				}
				catch(JSONException innerException) {
					throw new ErrorCodeException(
							ErrorCodes.MOBILITY_INVALID_MODE, 
							"The subtype is '" + 
								SubType.MODE_ONLY.toString().toLowerCase() + 
								"', but the required key is missing: " + 
								JSON_KEY_MODE, 
							innerException);
				}
			}
			catch(IllegalArgumentException e) {
				throw new ErrorCodeException(ErrorCodes.MOBILITY_INVALID_MODE, "The mode is unknown.", e);
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
				throw new ErrorCodeException(
						ErrorCodes.SERVER_INVALID_JSON, 
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
	 * @param date The date this Mobility point was created.
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
	 * @throws IllegalArgumentExcpetion Thrown if any of the required 
	 * 									parameters are missing or if any of the
	 * 									parameters are invalid.
	 */
	public MobilityPoint(Date date, Long time, TimeZone timezone,
			LocationStatus locationStatus, JSONObject location, 
			Mode mode, PrivacyState privacyState, 
			JSONObject sensorData, JSONObject features, String classifierVersion) throws ErrorCodeException {
		
		if(date == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
		else {
			this.date = date;
		}
		
		if(time == null) {
			throw new IllegalArgumentException("The time cannot be null.");
		}
		else {
			this.time = time;
		}
		
		if(timezone == null) {
			throw new IllegalArgumentException("The timezone cannot be null.");
		}
		else {
			this.timezone = timezone;
		}
		
		if(locationStatus == null) {
			throw new IllegalArgumentException("The location status cannot be null.");
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
			throw new IllegalArgumentException("The mode cannot be null.");
		}
		else {
			this.mode = mode;
		}
		
		if(privacyState == null) {
			throw new IllegalArgumentException("The privacy state cannot be null.");
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
			this.classifierData = new ClassifierData(features);
		}
	}
	
	/**
	 * Creates a new MobilityPoint object.
	 * 
	 * @param date The date and time that this reading was taken.
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
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing.
	 */
	public MobilityPoint(Date date, Long time, TimeZone timezone,
			LocationStatus locationStatus, Location location, 
			Mode mode, SensorData sensorData) {
		
		if(date == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
		else {
			this.date = date;
		}
		
		if(time == null) {
			throw new IllegalArgumentException("The time cannot be null.");
		}
		else {
			this.time = time;
		}
		
		if(timezone == null) {
			throw new IllegalArgumentException("The timezone cannot be null.");
		}
		else {
			this.timezone = timezone;
		}
		
		if(locationStatus == null) {
			throw new IllegalArgumentException("The location status cannot be null.");
		}
		else {
			this.locationStatus = locationStatus;
		}
		
		if((! LocationStatus.UNAVAILABLE.equals(locationStatus)) &&
				(location == null)) {
			throw new IllegalArgumentException(
					"The location cannot be null if the location status is not '" + 
					LocationStatus.UNAVAILABLE.toString().toLowerCase() + "'.");
		}
		else {
			this.location = location;
		}
		
		if(mode == null) {
			throw new IllegalArgumentException("The mode cannot be null.");
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
	 * Returns the date and time this Mobility point was created.
	 * 
	 * @return The date and time this Mobility point was created.
	 */
	public final Date getDate() {
		return date;
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
	 * Returns the list of Samples from this Mobility point if it is of type
	 * {@link SubType#SENSOR_DATA}; otherwise, null is returned.
	 * 
	 * @return A list of Samples from this Mobility point if it is of type
	 * 		   {@link SubType#SENSOR_DATA}, which may be empty but will never 
	 * 		   be null; otherwise, null is returned.
	 */
	public final List<Sample> getSamples() {
		if(! SubType.SENSOR_DATA.equals(subType)) {
			return null;
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
	 * @throws IllegalArgumentException Thrown if any of the values are 
	 * 									null.
	 */
	public final void setClassifierData(List<Double> fft, Double variance,
			Double n95Variance,
			Double average, Mode mode) {
			
		classifierData = new ClassifierData(fft, variance, n95Variance, average, mode);
	}
	
	/**
	 * Sets this Mobility point's classifier data from the server's classifier,
	 * but it only sets the mode.
	 * 
	 * @param mode The mode that the classifier generated.
	 */
	public final void setClassifierModeOnly(Mode mode) {
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
	 */
	public final JSONObject toJson(final boolean abbreviated, final boolean withData) {
		try {
			JSONObject result = new JSONObject();
			
			result.put(((abbreviated) ? JSON_KEY_DATE_SHORT : JSON_KEY_DATE), TimeUtils.getIso8601DateTimeString(date));
			result.put(((abbreviated) ? JSON_KEY_TIMEZONE_SHORT : JSON_KEY_TIMEZONE), timezone.getID());
			result.put(((abbreviated) ? JSON_KEY_TIME_SHORT : JSON_KEY_TIME), time);
			
			result.put(((abbreviated) ? JSON_KEY_LOCATION_STATUS_SHORT : JSON_KEY_LOCATION_STATUS), locationStatus.toString().toLowerCase());
			if(location != null) {
				result.put(((abbreviated) ? JSON_KEY_LOCATION_SHORT : JSON_KEY_LOCATION), location.toJson(abbreviated));
			}
			
			if(withData) {
				// Subtype
				result.put(JSON_KEY_SUBTYPE, subType.toString().toLowerCase());
				
				// If subtype is mode-only, then just the mode.
				if(SubType.MODE_ONLY.equals(subType)) {
					result.put(((abbreviated) ? JSON_KEY_MODE_SHORT : JSON_KEY_MODE), mode.toString().toLowerCase());
				}
				// If subtype is sensor-data, then a data object.
				else if(SubType.SENSOR_DATA.equals(subType)) {
					result.put(JSON_KEY_DATA, sensorData.toJson());
				}
			}
			else {
				result.put(((abbreviated) ? JSON_KEY_MODE_SHORT : JSON_KEY_MODE), mode.toString().toLowerCase());
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
}