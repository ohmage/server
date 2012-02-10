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

import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;

/**
 * This class contains all of the information associated with a location
 * record.
 * 
 * @author John Jenkins
 */
public class Location {
	public static final String JSON_KEY_LATITUDE = "latitude";
	private static final String JSON_KEY_LATITUDE_SHORT = "la";
	public static final String JSON_KEY_LONGITUDE = "longitude";
	private static final String JSON_KEY_LONGITUDE_SHORT = "lo";
	public static final String JSON_KEY_ACCURACY = "accuracy";
	private static final String JSON_KEY_ACCURACY_SHORT = "ac";
	public static final String JSON_KEY_PROVIDER = "provider";
	private static final String JSON_KEY_PROVIDER_SHORT = "pr";
	public static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIME_SHORT = "t";
	public static final String JSON_KEY_TIME_ZONE = "timezone";
	private static final String JSON_KEY_TIME_ZONE_SHORT = "tz";
	//public static final String JSON_KEY_OUTPUT_TIMESTAMP = "location_timestamp";
	
	private final Double latitude;
	private final Double longitude;
	private final Double accuracy;
	private final String provider;
	private final Long time;
	private final TimeZone timeZone;
	
	/**
	 * Creates a new Location object.
	 * 
	 * @param locationData A JSONObject representing all of the data for a
	 * 					   Location object.
	 * 
	 * @throws DomainException Thrown if the location data is null, isn't
	 * 						   a valid JSONObject, doesn't contain all of
	 * 						   the required information, or any of the 
	 * 						   information is invalid for its type.
	 */
	public Location(JSONObject locationData) throws DomainException {
		double tLatitude;
		try {
			tLatitude = locationData.getDouble(JSON_KEY_LATITUDE);
		}
		catch(JSONException noRegular) {
			try {
				tLatitude = locationData.getDouble(JSON_KEY_LATITUDE_SHORT);
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The latitude is missing or invalid.", 
						noShort);
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
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The longitude is missing or invalid.", 
						noShort);
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
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The accuracy is missing or invalid.", 
						noShort);
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
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The provider is missing.", 
						noShort);
			}
		}
		provider = tProvider;
		
		Long tTime;
		try {
			tTime = locationData.getLong(JSON_KEY_TIME);
		}
		catch(JSONException noRegular) {
			try {
				tTime = locationData.getLong(JSON_KEY_TIME_SHORT);
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMESTAMP, 
						"The timestamp is missing.", 
						noShort);
			}
		}
		time = tTime;
		
		TimeZone tTimeZone;
		try {
			tTimeZone = TimeZone.getTimeZone(locationData.getString(JSON_KEY_TIME_ZONE));
		}
		catch(JSONException noRegular) {
			try {
				tTimeZone = TimeZone.getTimeZone(locationData.getString(JSON_KEY_TIME_ZONE_SHORT));
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMEZONE, 
						"The time zone is missing.", 
						noShort);
			}
		}
		timeZone = tTimeZone;
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
	 * @throws DomainException Thrown if the provider or time zone are null.
	 */
	public Location(
			final double latitude, 
			final double longitude, 
			final double accuracy, 
			final String provider, 
			final long time, 
			final TimeZone timeZone) 
			throws DomainException {
		
		if(provider == null) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION, 
					"The provider cannot be null.");
		}
		else if(timeZone == null) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION,
					"The time zone cannot be null.");
		}
		
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.provider = provider;
		this.time = time;
		this.timeZone = timeZone;
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
	 * Returns the time for when this information was gathered.
	 * 
	 * @return The time for when this information was gathered.
	 */
	public final Long getTime() {
		return time;
	}
	
	/**
	 * Returns the time zone for when this information was gathered.
	 * 
	 * @return The time zone for when this information was gathered.
	 */
	public final TimeZone getTimeZone() {
		return timeZone;
	}
	
	/**
	 * Creates a JSONObject that represents the information in this object.
	 * 
	 * @param abbreviated Whether or not the keys should use their 
	 * 					  abbreviated version.
	 * 
	 * @return Returns a JSONObject that represents this object.
	 * 
	 * @throws JSONException There was an error building the JSONObject.
	 */
	public final JSONObject toJson(final boolean abbreviated) 
			throws JSONException {
		
		JSONObject result = new JSONObject();
		
		result.put(((abbreviated) ? JSON_KEY_LATITUDE_SHORT : JSON_KEY_LATITUDE), latitude);
		result.put(((abbreviated) ? JSON_KEY_LONGITUDE_SHORT : JSON_KEY_LONGITUDE), longitude);
		result.put(((abbreviated) ? JSON_KEY_ACCURACY_SHORT : JSON_KEY_ACCURACY), accuracy);
		result.put(((abbreviated) ? JSON_KEY_PROVIDER_SHORT : JSON_KEY_PROVIDER), provider);
		result.put(((abbreviated) ? JSON_KEY_TIME_SHORT : JSON_KEY_TIME), time);
		result.put(((abbreviated) ? JSON_KEY_TIME_ZONE_SHORT : JSON_KEY_TIME_ZONE), timeZone.getID());
		
		return result;
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
				+ ((time == null) ? 0 : time.hashCode());
		result = prime * result
				+ ((timeZone == null) ? 0 : timeZone.hashCode());
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
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (timeZone == null) {
			if (other.timeZone != null)
				return false;
		} else if (!timeZone.equals(other.timeZone))
			return false;
		return true;
	}
}
