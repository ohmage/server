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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.util.DateTimeUtils;

import org.apache.log4j.Logger;

/**
 * This class contains all of the information associated with a location
 * record.
 * 
 * @author John Jenkins
 */
public class Location {
	private static final Logger LOGGER = 
	Logger.getLogger(Location.class);

	Random rd = new Random();

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
	private final DateTime timestamp;
	/*
	private final long time;
	private final DateTimeZone timeZone;
	*/
	
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
			final DateTime timestamp,
			final double latitude, 
			final double longitude, 
			final double accuracy, 
			final String provider) 
			throws DomainException {
		
		if(timestamp == null) {
			throw new DomainException(
				"The timestamp is null.");
		}
		if(provider == null) {
			throw new DomainException(
				"The provider is null.");
		}

		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.provider = provider;
		this.timestamp = timestamp;
		/*
		this.time = timestamp.getMillis();
		this.timeZone = timestamp.getZone();
		*/
	}
	
	/**
	 * Creates a Location object from a Jackson JsonNode.
	 * 
	 * @param locationNode The location JsonNode.
	 * 
	 * @throws DomainException The node was invalid.
	 */
	public Location(
			final JsonNode locationNode)
			throws DomainException {
		
		if(locationNode == null) {
			throw new DomainException("The location node is null.");
		}
		
		List<DateTime> timestampRepresentations =
			new LinkedList<DateTime>();

		// Get the timestamp if the time and timezone fields were
		// specified.
		if(locationNode.has("time")) {
			JsonNode timeNode = locationNode.get("time");
			
			if(! timeNode.isNumber()) {
				throw new DomainException("The time isn't a number.");
			}
			long time = timeNode.getNumberValue().longValue();
			
			DateTimeZone timeZone = DateTimeZone.UTC;
			if(locationNode.has("timezone")) {
				JsonNode timeZoneNode =
					locationNode.get("timezone");
				
				if(! timeZoneNode.isTextual()) {
					throw new DomainException(
						"The time zone is not a string.");
				}
				
				try {
					timeZone = 
						DateTimeZone.forID(
							timeZoneNode.getTextValue());
				}
				catch(IllegalArgumentException e) {
					throw new DomainException(
						"The time zone is unknown.");
				}
			}
			
			timestampRepresentations.add(new DateTime(time, timeZone));
		}
		
		// Get the timestamp if the timestamp field was specified.
		if(locationNode.has("timestamp")) {
			JsonNode timestampNode =
				locationNode.get("timestamp");
			
			if(! timestampNode.isTextual()) {
				throw new DomainException(
					"The timestamp value was not a string.");
			}
			
			try {
				timestampRepresentations
					.add( 
						ISOW3CDateTimeFormat
							.any()
								.parseDateTime(
									timestampNode.getTextValue()));
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
					"The timestamp was not a valid ISO 8601 timestamp.",
					e);
			}
		}
		
		// Ensure that all representations of time are equal.
		if(timestampRepresentations.size() > 0) {
			// Create an iterator to cycle through the representations.
			Iterator<DateTime> timestampRepresentationsIter =
				timestampRepresentations.iterator();
			
			// The first timestamp will be set as the result. 
			DateTime timestamp = timestampRepresentationsIter.next();
			
			// Check against all subsequent timestamps to ensure that
			// they represent the same point in time.
			while(timestampRepresentationsIter.hasNext()) {
				if(timestamp.getMillis() != 
					timestampRepresentationsIter.next().getMillis()) {
					
					throw
						new DomainException(
							"Multiple representations of the timestamp were given, and they are not equal.");
				}
			}
			
			// If we checked out all of the timestamps and they are
			// equal, then save this timestamp.
			this.timestamp = timestamp;
		}
		else {
			throw new DomainException("The timestamp is missing.");
		}
		
		/*
		// Get the time.
		JsonNode timeNode = locationNode.get("time");
		if(timeNode == null) {
			throw new DomainException("The time is missing.");
		}
		else if(! timeNode.isNumber()) {
			throw new DomainException("The time is not a number.");
		}
		time = timeNode.getNumberValue().longValue();
		
		// Get the time zone.
		JsonNode timeZoneNode = locationNode.get("timezone");
		if(timeZoneNode == null) {
			throw new DomainException("The time zone is missing.");
		}
		else if(! timeZoneNode.isTextual()) {
			throw new DomainException("The time zone is not a string.");
		}
		try {
			timeZone = DateTimeZone.forID(timeZoneNode.getTextValue());
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The time zone is unknown.");
		}
		*/
		
		// Get the latitude.
		JsonNode latitudeNode = locationNode.get("latitude");
		if(latitudeNode == null) {
			throw new DomainException("The latitude is missing.");
		}
		else if(! latitudeNode.isNumber()) {
			throw new DomainException("The latitude is not a number.");
		}
		latitude = latitudeNode.getNumberValue().doubleValue();
		
		// Get the longitude.
		JsonNode longitudeNode = locationNode.get("longitude");
		if(longitudeNode == null) {
			throw new DomainException("The longitude is missing.");
		}
		else if(! longitudeNode.isNumber()) {
			throw new DomainException("The longitude is not a number.");
		}
		longitude = longitudeNode.getNumberValue().doubleValue();
		
		// Get the accuracy.
		JsonNode accuracyNode = locationNode.get("accuracy");
		if(accuracyNode == null) {
			throw new DomainException("The accuracy is missing.");
		}
		else if(! accuracyNode.isNumber()) {
			throw new DomainException("The accuracy is not a number.");
		}
		accuracy = accuracyNode.getNumberValue().doubleValue();
		
		// Get the provider.
		JsonNode providerNode = locationNode.get("provider");
		if(providerNode == null) {
			throw new DomainException("The provider is missing.");
		}
		else if(! providerNode.isTextual()) {
			throw new DomainException("The provider is not a string.");
		}
		provider = providerNode.getTextValue();
	}
	
	/**
	 * Creates a new Location object.
	 * 
	 * @param locationData A JSONObject representing all of the data for a
	 * 					   Location object.
	 * 
	 * @param defaultTimeZone This is a patch for the issue when the time zone
	 * 						  is only the value "0".
	 * 
	 * @throws DomainException Thrown if the location data is null, isn't
	 * 						   a valid JSONObject, doesn't contain all of
	 * 						   the required information, or any of the 
	 * 						   information is invalid for its type.
	 */
	public Location(
			final JSONObject locationData,
			final DateTimeZone defaultTimeZone) 
			throws DomainException {
		
		// The two possible representations of a timestamp.
		DateTime fromTimeTimezone = null, fromTimestamp = null;
		
		// Get the time.
		Long time = null;
		try {
			if(locationData.has(LocationColumnKey.TIME.toString(false))) {
				time = 
					locationData.getLong(
							LocationColumnKey.TIME.toString(false));
			}
			else if(locationData.has(LocationColumnKey.TIME.toString(true))) {
				time = 
					locationData.getLong(
							LocationColumnKey.TIME.toString(true));
			}
		}
		catch(JSONException e) {
			throw new DomainException(
				ErrorCode.SERVER_INVALID_TIME,
				"The time isn't a long.",
				e);
		}
		
		// Get the timezone.
		DateTimeZone timeZone = null;
		try {
			if(locationData.has(LocationColumnKey.TIMEZONE.toString(false))) {
				timeZone = 
					DateTimeUtils.getDateTimeZoneFromString(
						locationData.getString(
							LocationColumnKey.TIMEZONE.toString(false)));
			}
			else if(locationData.has(LocationColumnKey.TIMEZONE.toString(true))) {
				timeZone =
					DateTimeUtils.getDateTimeZoneFromString(
						locationData.getString(
							LocationColumnKey.TIMEZONE.toString(true)));
			}
		}
		catch(JSONException e) {
			throw new DomainException(
				ErrorCode.SERVER_INVALID_TIMEZONE,
				"The timezone isn't a string.",
				e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_TIMEZONE,
					"The time zone is unknown.",
					e);
		}
		
		// Validate that either both were given or neither were given, and, if
		// both were given, create a DateTime for it.
		if((time == null) && (timeZone != null)) {
			throw
				new DomainException(
					ErrorCode.SERVER_INVALID_TIME,
					"A 'timezone' was given, but the 'time' was not.");
		}
		else if((time != null) && (timeZone == null)) {
			throw
				new DomainException(
					ErrorCode.SERVER_INVALID_TIMEZONE,
					"A 'time' was given, but the 'timezone' was not.");
		}
		else if((time != null) && (timeZone != null)) {
			fromTimeTimezone = new DateTime(time, timeZone);
		}
		
		// Get the timestamp.
		if(locationData.has("timestamp")) {
			try {
				fromTimestamp =
					ISOW3CDateTimeFormat
						.any()
							.parseDateTime(
								locationData.getString("timestamp"));
			}
			catch(JSONException e) {
				throw new DomainException(
					ErrorCode.SERVER_INVALID_TIMESTAMP,
					"The timestamp isn't a string.",
					e);
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
					"The timestamp was not a valid ISO 8601 timestamp.",
					e);
			}
		}
		
		// Get the appropriate timestamp.
		// If none was given, thrown an exception.
		if((fromTimeTimezone == null) && (fromTimestamp == null)) {
			throw
				new DomainException(
					ErrorCode.SERVER_INVALID_TIMESTAMP,
					"The time information was missing.");
		}
		// If multiple were given, validate that they are they represent the
		// same time and timezone.
		else if((fromTimeTimezone != null) && (fromTimestamp != null)) {
			if(! fromTimeTimezone.equals(fromTimestamp)) {
				throw
					new DomainException(
						ErrorCode.SERVER_INVALID_TIMESTAMP,
						"Multiple, differing timestamps were given.");
			}
			else {
				timestamp = fromTimestamp;
			}
		}
		// If only one format was given, use that.
		else if(fromTimeTimezone != null) {
			timestamp = fromTimeTimezone;
		}
		else {
			timestamp = fromTimestamp;
		}
		
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
		return timestamp.getMillis();
	}
	
	/**
	 * Returns the time zone for when this information was gathered.
	 * 
	 * @return The time zone for when this information was gathered.
	 */
	public final DateTimeZone getTimeZone() {
		return timestamp.getZone();
	}
	
	/**
	 * Streams this object to the output.
	 * 
	 * @param generator The generator that will generate the result.
	 * 
	 * @param abbreviated Whether or not to stream the abbreviated keys.
	 * 
	 * @param columns The columns indicating which fields to include.
	 * 
	 * @throws JsonGenerationException There was a problem generating the JSON.
	 * 
	 * @throws IOException There was a problem writing to the stream.
	 * 
	 * @throws DomainException A required parameter was missing.
	 */
	public final void streamJson(
			final JsonGenerator generator,
			final boolean abbreviated,
			final Collection<ColumnKey> columns)
			throws JsonGenerationException, IOException, DomainException {
		
		if(generator == null) {
			throw new DomainException("The generator is null.");
		}
		else if(columns == null) {
			throw new DomainException("The list of columns cannot be null.");
		}
		
		if(columns.contains(LocationColumnKey.TIME)) {
			generator.writeNumberField(
				LocationColumnKey.TIME.toString(abbreviated), 
				getTime());
		}
			
		if(columns.contains(LocationColumnKey.TIMEZONE)) {
			generator.writeStringField(
				LocationColumnKey.TIMEZONE.toString(abbreviated), 
				getTimeZone().getID());
		}
			
		if(columns.contains(LocationColumnKey.LATITUDE)) {
			generator.writeNumberField(
				LocationColumnKey.LATITUDE.toString(abbreviated),  
				latitude);
		}
			
		if(columns.contains(LocationColumnKey.LONGITUDE)) {
			generator.writeNumberField(
				LocationColumnKey.LONGITUDE.toString(abbreviated), 
				longitude);
		}
			
		if(columns.contains(LocationColumnKey.ACCURACY)) {
			generator.writeNumberField(
				LocationColumnKey.ACCURACY.toString(abbreviated), 
				accuracy);
		}
			
		if(columns.contains(LocationColumnKey.PROVIDER)) {
			generator.writeStringField(
				LocationColumnKey.PROVIDER.toString(abbreviated), 
				provider);
		}
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
					getTime());
		}
			
		if(columns.contains(LocationColumnKey.TIMEZONE)) {
			result.put(
					LocationColumnKey.TIMEZONE.toString(abbreviated), 
					getTimeZone().getID());
		}
			
		if(columns.contains(LocationColumnKey.LATITUDE)) {
			double factor = rd.nextGaussian() * 0.001; 
			double latitudeLowPrecision = latitude + factor; 
			if(latitude>89 || latitude<-89){
				latitudeLowPrecision = latitude;
			}
			result.put(
					LocationColumnKey.LATITUDE.toString(abbreviated),  
					latitudeLowPrecision);
		}
			
		if(columns.contains(LocationColumnKey.LONGITUDE)) {
			double factor = rd.nextGaussian() * 0.001; 
			double longitudeLowPrecision = longitude + factor; 
			if(latitude>179 || latitude<-179){
				longitudeLowPrecision = longitude;
			}
			result.put(
					LocationColumnKey.LONGITUDE.toString(abbreviated), 
					longitudeLowPrecision);
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
			result.set(index, getTime());
		}
		
		if((index = columns.indexOf(LocationColumnKey.TIMEZONE)) != -1) {
			result.set(index, getTimeZone().getID());
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
		result =
			prime * result + ((provider == null) ? 0 : provider.hashCode());
		result =
			prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Location)) {
			return false;
		}
		Location other = (Location) obj;
		if(Double.doubleToLongBits(accuracy) != Double
			.doubleToLongBits(other.accuracy)) {
			return false;
		}
		if(Double.doubleToLongBits(latitude) != Double
			.doubleToLongBits(other.latitude)) {
			return false;
		}
		if(Double.doubleToLongBits(longitude) != Double
			.doubleToLongBits(other.longitude)) {
			return false;
		}
		if(provider == null) {
			if(other.provider != null) {
				return false;
			}
		}
		else if(!provider.equals(other.provider)) {
			return false;
		}
		if(timestamp == null) {
			if(other.timestamp != null) {
				return false;
			}
		}
		else if(!timestamp.equals(other.timestamp)) {
			return false;
		}
		return true;
	}
}
