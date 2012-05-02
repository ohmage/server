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
package org.ohmage.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * This class is responsible for validating information pertaining to Mobility
 * data.
 * 
 * @author John Jenkins
 */
public final class MobilityValidators {
	private static final Logger LOGGER = Logger.getLogger(MobilityValidators.class);
	
	/**
	 * The multiplier used to convert the time given by the user into a valid
	 * milliseconds value.<br />
	 * <br />
	 * Current value: Minutes
	 */
	public static final long CHUNK_DURATION_MULTIPLIER = 1000 * 60; 
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private MobilityValidators() {}
	
	/**
	 * Validates Mobility data in the format of a JSONArray of JSONObjects 
	 * where each JSONObject is a Mobility data point. It returns the decoded
	 * Mobility points and adds the invalid Mobility points and their 
	 * respective ID (if retrievable) to the parameterized lists.
	 * 
	 * @param data The data to be validated. The expected value is a JSONArray
	 * 			   of JSONObjects where each JSONObject is a Mobility data
	 * 			   point.
	 * 
	 * @param invalidPointIds The IDs from their respective JSON objects where
	 * 						  the entire point contained some error(s).
	 * 
	 * @param invalidPoint The JSONObject for each point that contained 
	 * 					   some error. There may or may not be a 
	 * 					   corresponding ID in the 'invalidPointIds' list
	 * 					   depending on whether the ID was retrievable or
	 * 					   not.
	 * 
	 * @return Returns null if the data is null or whitespace only; otherwise,
	 * 		   a list of MobilityPoint objects is returned.
	 * 
	 * @throws ValidationException Thrown if the data is not null, not
	 * 							   whitespace only, and the data String cannot
	 * 							   be decoded into JSON.
	 */
	public static List<MobilityPoint> validateDataAsJsonArray(
			final String data, 
			Collection<JSONObject> invalidPoint) 
			throws ValidationException {
		
		LOGGER.info("Validating a JSONArray of data points.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(data)) {
			return null;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(data.trim());
			
			List<MobilityPoint> result = new LinkedList<MobilityPoint>();
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject mobilityPointJson = jsonArray.getJSONObject(i);
				
				try {
					result.add(
							new MobilityPoint(
									mobilityPointJson, 
									MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException invalidPointException) {
					LOGGER.warn("Invalid point.", invalidPointException);
					invalidPoint.add(mobilityPointJson);
				}
			}
			
			return result;
		}
		catch(JSONException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_JSON, 
					"The JSONArray containing the data is malformed.", 
					e);
		}
	}
	
	/**
	 * Validates that the date is valid and returns it or fails the request.
	 * 
	 * @param date The date value as a string.
	 * 
	 * @return The date decoded into a Date object or null if the string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the date string was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static DateTime validateDate(final String date) 
			throws ValidationException {
		
		LOGGER.info("Validating a date value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(date)) {
			return null;
		}
		
		try {
			return TimeUtils.getDateTimeFromString(date);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_DATE, 
					"The date value is unknown: " + date);
		}
	}
	
	/**
	 * Validates that the duration is a valid number and that it is not too 
	 * large. The largeness restriction is based on the multiplier used to
	 * convert this value to its internal milliseconds representation.
	 * 
	 * @param duration The duration to be validated.
	 * 
	 * @return The duration as a long value or null if the duration was null or
	 * 		   an empty string.
	 * 
	 * @throws ValidationException Thrown if there is an error.
	 */
	public static Long validateChunkDuration(final String duration)
			throws ValidationException {
		
		LOGGER.info("Validating the chunk duration.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(duration)) {
			return null;
		}
		
		try {
			Long longDuration = Long.decode(duration);
			
			if(longDuration > (Long.MAX_VALUE / CHUNK_DURATION_MULTIPLIER)) {
				throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_CHUNK_DURATION,
						"The chunk duration is too great. It can be at most " +
								(Long.MAX_VALUE / CHUNK_DURATION_MULTIPLIER));
			}
			
			return longDuration * CHUNK_DURATION_MULTIPLIER;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.MOBILITY_INVALID_CHUNK_DURATION,
					"The chunk duration is invalid: " + duration);
		}
	}
	
	/**
	 * Validates that the duration is a valid number.
	 * 
	 * @param duration The duration to be validated.
	 * 
	 * @return The duration as a long value or null if the duration was null or
	 * 		   an empty string.
	 * 
	 * @throws ValidationException Thrown if there is an error.
	 */
	public static Long validateAggregateDuration(final String duration)
			throws ValidationException {
		
		LOGGER.info("Validating the aggregate duration.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(duration)) {
			return null;
		}
		
		try {
			return Long.decode(duration);
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.MOBILITY_INVALID_AGGREGATE_DURATION,
					"The aggregate duration is invalid: " + duration);
		}
	}
	
	/**
	 * Validates that a value representing a boolean indicating if we should or
	 * should not return sensor data along with the other information 
	 * representing a Mobility point.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return The boolean representation of the value.
	 * 
	 * @throws ValidationException Thrown if the value could not be decoded as
	 * 							   a valid boolean.
	 */
	public static boolean validateIncludeSensorDataValue(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return false;
		}
		
		Boolean result = StringUtils.decodeBoolean(value);
		if(result == null) {
			throw new ValidationException(
					ErrorCode.MOBILITY_INVALID_INCLUDE_SENSOR_DATA_VALUE,
					"The \"include sensor data\" value was not a valid boolean: " +
							value);
		}
		
		return result;
	}
	
	/**
	 * Validates that a string representing column keys contains only valid 
	 * column keys and then converts them into their actual value.
	 * 
	 * @param value The string to be decoded.
	 * 
	 * @return A list of column keys.
	 * 
	 * @throws ValidationException
	 */
	public static List<ColumnKey> validateColumns(
			final String value,
			final boolean allowDuplicates)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return MobilityColumnKey.ALL_COLUMNS;
		}
		
		Map<ColumnKey, String> duplicates = new HashMap<ColumnKey, String>();
		List<ColumnKey> result = new LinkedList<ColumnKey>();
		String[] columns = value.trim().split(InputKeys.LIST_ITEM_SEPARATOR);
		for(String column : columns) {
			if(StringUtils.isEmptyOrWhitespaceOnly(column)) {
				continue;
			}
			
			try {
				List<ColumnKey> validatedColumns =
						MobilityPoint.validateKeyString(column.trim());
				
				if(! allowDuplicates) {
					String duplicate;
					for(ColumnKey currColumn : validatedColumns) {
						if((duplicate = duplicates.put(currColumn, column)) != null) {
							throw new ValidationException(
									ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
									"The same column '" +
										currColumn.toString() +
										"' is being requested by two column values: '" +
										duplicate + 
										"' and '" +
										column +
										"'");
						}
					}
				}
				
				result.addAll(validatedColumns);
			}
			catch(DomainException e) {
				throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
						"The column is unknown: " + column,
						e);
			}
		}
		
		return result;
	}
}
