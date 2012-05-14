package org.ohmage.validator;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for all of the observer validators.
 *
 * @author John Jenkins
 */
public class ObserverValidators {
	/**
	 * Empty default constructor.
	 */
	private ObserverValidators() {}
	
	/**
	 * Validates that a observer definition is valid and creates a Observer from it.
	 * 
	 * @param definition The XML definition as a String.
	 * 
	 * @return The Observer represented by the definition or NULL if the
	 * 		   definition is null or only whitespace.
	 * 
	 * @throws ValidationException The observer definition was not valid.
	 */
	public static final Observer validateObserverDefinitionXml(
			final String definition)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(definition)) {
			return null;
		}
		
		try {
			return new Observer(definition);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_DEFINITION,
				e.getMessage(),
				e);
		}
	}
	
	/**
	 * Validates that an observer ID is valid.
	 * 
	 * @param value The observer ID.
	 * 
	 * @return The sanitized observer ID or null if the value was null or only
	 * 		   whitespace.
	 * 
	 * @throws ValidationException The observer ID is invalid.
	 */
	public static final String validateObserverId(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Observer.sanitizeId(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_ID,
				"The observer ID is invalid: " + value,
				e);
		}
	}
	
	/**
	 * Validates that an observer version is valid.
	 * 
	 * @param value The observer version as a string.
	 * 
	 * @return The observer version as a long or null if the value was null or
	 * 		   only whitespace.
	 * 
	 * @throws ValidationException The observer version is invalid.
	 */
	public static final Long validateObserverVersion(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Long.valueOf(value);
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_VERSION,
				"The value is not a valid number: " + value,
				e);
		}
	}
	
	/**
	 * Sanitizes the stream ID.
	 * 
	 * @param value The stream ID to be sanitized.
	 * 
	 * @return The sanitized stream ID or null if the stream ID is null or only
	 * 		   whitespace.
	 * 
	 * @throws ValidationException The stream ID is invalid.
	 */
	public static final String validateStreamId(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Observer.Stream.sanitizeId(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_STREAM_ID,
				"The stream ID is invalid: " + value,
				e);
		}
	}
	
	/**
	 * Validates that an stream version is valid.
	 * 
	 * @param value The stream version as a string.
	 * 
	 * @return The stream version as a long or null if the value was null or
	 * 		   only whitespace.
	 * 
	 * @throws ValidationException The stream version is invalid.
	 */
	public static final Long validateStreamVersion(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Long.valueOf(value);
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_STREAM_VERSION,
				"The value is not a valid number: " + value,
				e);
		}
	}
	
	/**
	 * Decodes the uploaded data as a JSON array of JSON objects.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A collection of JSONObjects or null if the value was null or 
	 * 		   only whitespace.
	 * 
	 * @throws ValidationException The data is invalid.
	 */
	public static final Collection<JSONObject> validateData(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		JSONArray array;
		try {
			array = new JSONArray(value);
		}
		catch(JSONException e) {
			throw new ValidationException(
				ErrorCode.OBSERVER_INVALID_STREAM_DATA,
				"The data stream was not a JSON array of JSON objects.");
		}
		
		int arrayLength = array.length();
		Collection<JSONObject> result = new ArrayList<JSONObject>(arrayLength);
		for(int i = 0; i < arrayLength; i++) {
			try {
				result.add(array.getJSONObject(i));
			}
			catch(JSONException e) {
				throw new ValidationException(
					ErrorCode.OBSERVER_INVALID_STREAM_DATA,
					"The element at index, " + i + ", was not a JSON object.");
			}
		}
		
		return result;
	}
}