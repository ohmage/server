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
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.util.TimeUtils;

/**
 * This class contains all of the information associated with a location
 * record.
 * 
 * @author John Jenkins
 */
public class Location {
	/**
	 * Column names for location information.
	 * 
	 * @author John Jenkins
	 */
	public static enum LocationColumnKey implements ColumnKey {
		/**
		 * The time at which the location information was acquired.
		 */
		TIME ("time", "t"),
		/**
		 * The time zone of the device.
		 */
		TIMEZONE ("timezone", "tz"),
		/**
		 * The latitude of the device.
		 */
		LATITUDE ("latitude", "la"),
		/**
		 * The longitude of the device.
		 */
		LONGITUDE ("longitude", "lo"),
		/**
		 * The accuracy of this reading, i.e. it is accurate within this many
		 * units.
		 */
		ACCURACY ("accuracy", "ac"),
		/**
		 * The entity that gathered the information.
		 */
		PROVIDER ("provider", "pr"),
		/**
		 * The status of the location.
		 */
		STATUS ("location_status", "ls");
		
		/**
		 * The string that may be optionally be placed before a key to better
		 * namespace it.
		 */
		public static final String NAMESPACE = "location";
		
		/**
		 * A pre-built, unmodifiable list that contains all of the 
		 * LocationColumnKey keys.
		 */
		public static final List<ColumnKey> ALL_COLUMNS;
		static {
			// We must do this statically as opposed to calling 'value()' to
			// guarantee the ordering of the columns.
			List<ColumnKey> keys = new LinkedList<ColumnKey>();

			keys.add(STATUS);
			keys.add(TIME);
			keys.add(TIMEZONE);
			keys.add(LATITUDE);
			keys.add(LONGITUDE);
			keys.add(ACCURACY);
			keys.add(PROVIDER);
			
			ALL_COLUMNS = Collections.unmodifiableList(keys);
		}
		
		private final String key;
		private final String abbreviatedKey;
		
		/**
		 * Creates a LocationColumnKey object with the human-readable and
		 * abbreviated versions of the key.
		 * 
		 * @param key The long, human-readable name for this key.
		 * 
		 * @param abbreviatedKey A short abbreviation for this key.
		 */
		private LocationColumnKey(
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
		 * abbreviated version, to its LocationColumnKey object.
		 *  
		 * @param value The string value to convert.
		 * 
		 * @return The LocationColumnKey that represents this object.
		 * 
		 * @throws IllegalArgumentException The string could not be converted 
		 * 									into a LocationColumnKey object.
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
			
			for(LocationColumnKey currKey : values()) {
				if(currKey.key.equals(sanitizedValue) ||
						currKey.abbreviatedKey.equals(sanitizedValue)) {
					
					List<ColumnKey> result = new ArrayList<ColumnKey>(1);
					result.add(currKey);
					return result;
				}
			}
			
			throw new IllegalArgumentException("Unknown column key.");
		}
		
		/**
		 * Checks if a collection of column keys contains any of the keys in 
		 * this enum.
		 * 
		 * @param columns The collection of columns.
		 * 
		 * @return True if the collection contains any of these column keys; 
		 * 		   false, otherwise.
		 */
		public static boolean containsLocationColumnKey(
				final Collection<ColumnKey> columns) {
			
			for(LocationColumnKey currKey : values()) {
				if(columns.contains(currKey)) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	private final double latitude;
	private final double longitude;
	private final double accuracy;
	private final String provider;
	private final long time;
	private final DateTimeZone timeZone;
	
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
		Long tTime;
		try {
			tTime = 
					locationData.getLong(
							LocationColumnKey.TIME.toString(false));
		}
		catch(JSONException noRegular) {
			try {
				tTime = 
						locationData.getLong(
								LocationColumnKey.TIME.toString(true));
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMESTAMP, 
						"The timestamp is missing.", 
						noShort);
			}
		}
		time = tTime;
		
		DateTimeZone tTimeZone;
		try {
			tTimeZone = 
					TimeUtils.getDateTimeZoneFromString(
						locationData.getString(
							LocationColumnKey.TIMEZONE.toString(
								false)));
		}
		catch(JSONException noRegular) {
			try {
				tTimeZone = 
						TimeUtils.getDateTimeZoneFromString(
							locationData.getString(
								LocationColumnKey.TIMEZONE.toString(
									true)));
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_TIMEZONE, 
						"The time zone is missing.", 
						noShort);
			}
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_TIMEZONE,
					"The time zone is unknown.",
					e);
		}
		timeZone = tTimeZone;
		
		double tLatitude;
		try {
			tLatitude = 
					locationData.getDouble(
							LocationColumnKey.LATITUDE.toString(false));
		}
		catch(JSONException noRegular) {
			try {
				tLatitude = 
						locationData.getDouble(
								LocationColumnKey.LATITUDE.toString(true));
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The latitude is missing or invalid: " + 
							LocationColumnKey.LATITUDE.toString(false), 
						noShort);
			}
		}
		latitude = tLatitude;
		
		double tLongitude;
		try {
			tLongitude = 
					locationData.getDouble(
							LocationColumnKey.LONGITUDE.toString(false));
		}
		catch(JSONException noRegular) {
			try {
				tLongitude = 
						locationData.getDouble(
								LocationColumnKey.LONGITUDE.toString(true));
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
			tAccuracy = 
					locationData.getDouble(
							LocationColumnKey.ACCURACY.toString(false));
		}
		catch(JSONException noRegular) {
			try {
				tAccuracy = 
						locationData.getDouble(
								LocationColumnKey.ACCURACY.toString(true));
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
			tProvider = 
					locationData.getString(
							LocationColumnKey.PROVIDER.toString(false));
		}
		catch(JSONException noRegular) {
			try {
				tProvider = 
						locationData.getString(
								LocationColumnKey.PROVIDER.toString(true));
			}
			catch(JSONException noShort) {
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The provider is missing.", 
						noShort);
			}
		}
		provider = tProvider;
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
			final DateTimeZone timeZone) 
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
	public final DateTimeZone getTimeZone() {
		return timeZone;
	}
	
	/**
	 * Creates a JSONObject that represents the information in this object with
	 * only the information whose column value is present.
	 * 
	 * @param abbreviated Whether or not the keys should use their 
	 * 					  abbreviated version.
	 * 
	 * @param columns A collection of columns dictating which variables should
	 * 				  be included in the resulting object. If this is empty, an
	 * 				  empty JSONObject will be returned. This cannot be null 
	 * 				  and if all columns are desired, a quick fix is to use
	 * 				  {@link LocationColumnKey#ALL_COLUMNS}.
	 * 
	 * @return Returns a JSONObject that represents this object with only the
	 * 		   requested columns.
	 * 
	 * @throws JSONException There was an error building the JSONObject.
	 * 
	 * @throws DomainException The columns collection was null or a column key
	 * 						   was a LocationColumnKey but was not known to 
	 * 						   this function.
	 */
	public final JSONObject toJson(
			final boolean abbreviated,
			final Collection<ColumnKey> columns) 
			throws JSONException, DomainException {
		
		if(columns == null) {
			throw new DomainException("The list of columns cannot be null.");
		}
		
		JSONObject result = new JSONObject();
		
		if(columns.contains(LocationColumnKey.TIME)) {
			result.put(
					LocationColumnKey.TIME.toString(abbreviated), 
					time);
		}
			
		if(columns.contains(LocationColumnKey.TIMEZONE)) {
			result.put(
					LocationColumnKey.TIMEZONE.toString(abbreviated), 
					timeZone.getID());
		}
			
		if(columns.contains(LocationColumnKey.LATITUDE)) {
			result.put(
					LocationColumnKey.LATITUDE.toString(abbreviated),  
					latitude);
		}
			
		if(columns.contains(LocationColumnKey.LONGITUDE)) {
			result.put(
					LocationColumnKey.LONGITUDE.toString(abbreviated), 
					longitude);
		}
			
		if(columns.contains(LocationColumnKey.ACCURACY)) {
			result.put(
					LocationColumnKey.ACCURACY.toString(abbreviated), 
					accuracy);
		}
			
		if(columns.contains(LocationColumnKey.PROVIDER)) {
			result.put(
					LocationColumnKey.PROVIDER.toString(abbreviated), 
					provider);
		}
		
		return result;
	}
	
	/**
	 * Populates the appropriate indices in the 'result' with values based on
	 * the 'columns'. For example, if the first two columns were not Location
	 * columns, they would be skipped. If the third column was the location's 
	 * latitude, the third index in the 'result' would be populated with this
	 * location's latitude value.
	 * 
	 * @param columns All of the columns in the 'result' list to populate. This
	 * 				  will skip all of those columns that are not 
	 * 				  LocationColumnKey columns.
	 * 
	 * @param result The result list whose values will be updated based on the
	 * 				 'columns'.
	 * 
	 * @throws DomainException The columns or result list was null or had 
	 * 						   differing lengths, or a column was a 
	 * 						   LocationColumnKey column but was unknown to this
	 * 						   function.
	 */
	public final void toCsvRow(
			final List<ColumnKey> columns,
			final List<Object> result) 
			throws DomainException {
		
		if(columns == null) {
			throw new DomainException("The list of columns cannot be null.");
		}
		else if(result == null) {
			throw new DomainException("The list of results cannot be null.");
		}
		else if(columns.size() != result.size()) {
			throw new DomainException(
					"The columns list and the result list were different lengths.");
		}
		
		int index;
		
		if((index = columns.indexOf(LocationColumnKey.TIME)) != -1) {
			result.set(index, time);
		}
		
		if((index = columns.indexOf(LocationColumnKey.TIMEZONE)) != -1) {
			result.set(index, timeZone.getID());
		}
		
		if((index = columns.indexOf(LocationColumnKey.LATITUDE)) != -1) {
			result.set(index, latitude);
		}
		
		if((index = columns.indexOf(LocationColumnKey.LONGITUDE)) != -1) {
			result.set(index, longitude);
		}
		
		if((index = columns.indexOf(LocationColumnKey.ACCURACY)) != -1) {
			result.set(index, accuracy);
		}
		
		if((index = columns.indexOf(LocationColumnKey.PROVIDER)) != -1) {
			result.set(index, provider);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(accuracy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Location)) {
			return false;
		}
		Location other = (Location) obj;
		if (Double.doubleToLongBits(accuracy) != Double
				.doubleToLongBits(other.accuracy)) {
			return false;
		}
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude)) {
			return false;
		}
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude)) {
			return false;
		}
		if (provider == null) {
			if (other.provider != null) {
				return false;
			}
		} else if (!provider.equals(other.provider)) {
			return false;
		}
		if (time != other.time) {
			return false;
		}
		return true;
	}
}