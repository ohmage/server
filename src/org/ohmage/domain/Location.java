package org.ohmage.domain;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * This class contains all of the information associated with a location
 * record.
 * 
 * @author John Jenkins
 */
public class Location {
	private static final String JSON_KEY_LATITUDE = "latitude";
	private static final String JSON_KEY_LATITUDE_SHORT = "la";
	private static final String JSON_KEY_LONGITUDE = "longitude";
	private static final String JSON_KEY_LONGITUDE_SHORT = "lo";
	private static final String JSON_KEY_ACCURACY = "accuracy";
	private static final String JSON_KEY_ACCURACY_SHORT = "ac";
	private static final String JSON_KEY_PROVIDER = "provider";
	private static final String JSON_KEY_PROVIDER_SHORT = "pr";
	private static final String JSON_KEY_TIMESTAMP = "timestamp";
	private static final String JSON_KEY_TIMESTAMP_SHORT = "ts";
	
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
	 * @throws LocationException Thrown if the location data is null, isn't
	 * 							 a valid JSONObject, doesn't contain all of
	 * 							 the required information, or any of the 
	 * 							 information is invalid for its type.
	 */
	public Location(JSONObject locationData) throws ErrorCodeException {
		double tLatitude;
		try {
			tLatitude = locationData.getDouble(JSON_KEY_LATITUDE);
		}
		catch(JSONException noRegular) {
			try {
				tLatitude = locationData.getDouble(JSON_KEY_LATITUDE_SHORT);
			}
			catch(JSONException noShort) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The latitude is missing or invalid.", noShort);
			}
		}
		latitude = tLatitude;
		
		double tLongitude;
		try {
			tLongitude = locationData.getDouble(JSON_KEY_LONGITUDE);
		}
		catch(JSONException noRegular) {
			try {
				tLongitude = locationData.getDouble(JSON_KEY_LONGITUDE_SHORT);
			}
			catch(JSONException noShort) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The longitude is missing or invalid.", noShort);
			}
		}
		longitude = tLongitude;

		double tAccuracy;
		try {
			tAccuracy = locationData.getDouble(JSON_KEY_ACCURACY);
		}
		catch(JSONException noRegular) {
			try {
				tAccuracy = locationData.getDouble(JSON_KEY_ACCURACY_SHORT);
			}
			catch(JSONException noShort) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The accuracy is missing or invalid.", noShort);
			}
		}
		accuracy = tAccuracy;
		
		String tProvider;
		try {
			tProvider = locationData.getString(JSON_KEY_PROVIDER);
		}
		catch(JSONException noRegular) {
			try {
				tProvider = locationData.getString(JSON_KEY_PROVIDER_SHORT);
			}
			catch(JSONException noShort) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The provider is missing.", noShort);
			}
		}
		provider = tProvider;
		
		Date tTimestamp;
		try {
			tTimestamp = StringUtils.decodeDateTime(locationData.getString(JSON_KEY_TIMESTAMP));
		}
		catch(JSONException noRegular) {
			try {
				tTimestamp = StringUtils.decodeDateTime(locationData.getString(JSON_KEY_TIMESTAMP_SHORT));
			}
			catch(JSONException noShort) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is missing.", noShort);
			}
		}
		if(tTimestamp == null) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is invalid.");
		}
		timestamp = tTimestamp;
	}
	
	/**
	 * Creates a new Location object.
	 * 
	 * @param latitude The latitude of the device.
	 * 
	 * @param longitude The longitude of the device.
	 * 
	 * @param accuracy The accuracy of the reading.
	 * 
	 * @param provider A string representing who the provider was.
	 * 
	 * @param timestamp A timestamp of when this reading was made.
	 * 
	 * @throws IllegalArgumentException Thrown if the provider or date are
	 * 									null.
	 */
	public Location(final double latitude, final double longitude, 
			final double accuracy, final String provider, 
			final Date timestamp) {
		
		if(provider == null) {
			throw new IllegalArgumentException("The provider cannot be null.");
		}
		else if(timestamp == null) {
			throw new IllegalArgumentException("The timestamp cannot be null.");
		}
		
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.provider = provider;
		this.timestamp = timestamp;
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
	
	/**
	 * Creates a JSONObject that represents the information in this object.
	 * 
	 * @param abbreviated Whether or not the keys should use their 
	 * 					  abbreviated version.
	 * 
	 * @return Returns a JSONObject that represents this object or null if
	 * 		   there is an error building the JSONObject.
	 */
	public final JSONObject toJson(final boolean abbreviated) {
		try {
			JSONObject result = new JSONObject();
			
			result.put(((abbreviated) ? JSON_KEY_LATITUDE_SHORT : JSON_KEY_LATITUDE), latitude);
			result.put(((abbreviated) ? JSON_KEY_LONGITUDE_SHORT : JSON_KEY_LONGITUDE), longitude);
			result.put(((abbreviated) ? JSON_KEY_ACCURACY_SHORT : JSON_KEY_ACCURACY), accuracy);
			result.put(((abbreviated) ? JSON_KEY_PROVIDER_SHORT : JSON_KEY_PROVIDER), provider);
			result.put(((abbreviated) ? JSON_KEY_TIMESTAMP_SHORT : JSON_KEY_TIMESTAMP), TimeUtils.getIso8601DateTimeString(timestamp));
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}

	/**
	 * Generates a hash code for this location.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accuracy == null) ? 0 : accuracy.hashCode());
		result = prime * result
				+ ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result
				+ ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result
				+ ((provider == null) ? 0 : provider.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/**
	 * Compares another object to see if it is logically the same as this one.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (accuracy == null) {
			if (other.accuracy != null)
				return false;
		} else if (!accuracy.equals(other.accuracy))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
}