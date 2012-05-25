package org.ohmage.validator;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

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
	public static final JsonParser validateData(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return (new MappingJsonFactory()).createJsonParser(value);
		}
		catch(IOException e) {
			throw new ValidationException("The data could not be read.", e);
		}
	}
	
	/**
	 * Validates that a date is a valid date with or without time.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return A DateTime representing the date and time or null if the value
	 * 		   was null or only whitespace.
	 * 
	 * @throws ValidationException The value is not null or whitespace and 
	 * 							   could not be decoded.
	 */
	public static final DateTime validateDate(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return TimeUtils.getDateTimeFromString(value);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_DATE,
				"The date was invalid: " + value,
				e);
		}
	}
	
	/**
	 * Converts a string of comma-separated columns into a set of strings and
	 * removes all empty strings.
	 * 
	 * @param value The comma-separated string.
	 * 
	 * @return A set of column strings.
	 * 
	 * @throws ValidationException A column value was invalid.
	 */
	public static final ColumnNode<String> validateColumnList(
			final String value)
			throws ValidationException {
		
		ColumnNode<String> result = new ColumnNode<String>();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return result;
		}
		
		String[] items = value.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int currColumn = 0; currColumn < items.length; currColumn++) {
			String column = items[currColumn];
			
			if(column == null) {
				continue;
			}
			
			String trimmedColumn = column.trim();
			if(trimmedColumn.length() == 0) {
				continue;
			}
			
			ColumnNode<String> currNode = result;
			String[] parts = trimmedColumn.split(":");
			for(int currPart = 0; currPart < parts.length; currPart++) {
				String part = parts[currPart];
				
				if(part == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_COLUMN_LIST,
						"Two ':'s were given in sequence: " + trimmedColumn);
				}
				
				String trimmedPart = part.trim();
				if(trimmedPart.length() == 0) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_COLUMN_LIST,
						"Two ':'s were given in sequence: " + trimmedColumn);
				}
				
				if(! currNode.hasChild(trimmedPart)) {
					currNode.addChild(trimmedPart);
				}
				
				currNode = currNode.getChild(trimmedPart);
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that the number to skip is positive or zero.
	 * 
	 * @param value The number to validate.
	 * 
	 * @return The number to skip with a default of 0.
	 * 
	 * @throws ValidationException The number was negative.
	 */
	public static final long validateNumToSkip(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return 0;
		}
		
		try {
			long result = Long.decode(value);
			
			if(result < 0) {
				throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
					"The number to skip must be positive: " + result);
			}
			
			return result;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
				"The number to skip is not a valid number: " + value,
				e);
		}
	}
	
	/**
	 * Validates that the number to return is positive or zero and less than or
	 * equal to the maximum allowed.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return The number to return.
	 * 
	 * @throws ValidationException The number was negative or greater than the
	 * 							   allowed maximum.
	 */
	public static final long validateNumToReturn(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return StreamReadRequest.MAX_NUMBER_TO_RETURN;
		}
		
		try {
			long result = Long.decode(value);

			if(result < 0) {
				throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
					"The number to return must be positive: " + result);
			}
			else if(result > StreamReadRequest.MAX_NUMBER_TO_RETURN) {
				throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
					"The number to return is greater than the allowed maximum (" +
						StreamReadRequest.MAX_NUMBER_TO_RETURN +
						"): " +
						result);
			}
			
			return result;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
				"The number to skip is not a valid number.",
				e);
		}
	}
}