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
package org.ohmage.domain.campaign.prompt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents prompts with numeric bounds. This includes but is not
 * limited to numeric prompts where the numeric response must be between two
 * numbers as well as text prompts where the number of characters that make up
 * the result must be between two numbers.
 * 
 * @author John Jenkins
 */
public abstract class BoundedPrompt extends Prompt {
	private static final String JSON_KEY_LOWER_BOUND = "min";
	private static final String JSON_KEY_UPPER_BOUND = "max";
	private static final String JSON_KEY_DEFAULT = "default";
	
	/**
	 * The campaign configuration property key for the lower bound.
	 */
	public static final String XML_KEY_MIN = "min";
	/**
	 * The campaign configuration property key for the upper bound.
	 */
	public static final String XML_KEY_MAX = "max";
	
	private final BigDecimal min;
	private final BigDecimal max;
	
	private final BigDecimal defaultValue;
	/**
	 * Whether or not the min, max, default and response must be a whole number.
	 */
	private final boolean wholeNumber;

	
	/**
	 * Creates a new bounded prompt.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param min The lower bound for a response to this prompt.
	 * 
	 * @param max The upper bound for a response to this prompt.
	 * 
	 * @param defaultValue The default value for this prompt. This is optional
	 * 					   and may be null if one doesn't exist.
	 * 
	 * @param type This prompt's 
	 * 			   {@link org.ohmage.domain.campaign.Prompt.Type}.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public BoundedPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final BigDecimal min, 
			final BigDecimal max, 
			final BigDecimal defaultValue,
			final Boolean wholeNumber,
			final Type type, 
			final int index)
			throws DomainException {
		
		super(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			type,
			index);
		
		
		if(wholeNumber == null) {
			this.wholeNumber = true; // Hack for HT to handle pre-2.16 campaigns with number prompts.
			                         // The previous spec only allowed whole numbers.
		}
		else {
			this.wholeNumber = wholeNumber;
		}

		// Validate the min value.
		if(this.wholeNumber && (! isWholeNumber(min))) {
			throw
				new DomainException("The min must be a whole number: " + min);
		}
		this.min = min;
		
		// Validate the max value.
		if(this.wholeNumber && (! isWholeNumber(max))) {
			throw
				new DomainException("The max must be a whole number: " + max);
		}
		this.max = max;
		
		// max has to be >= min
		if (this.max.compareTo(this.min) < 0) {
		    throw new DomainException("The max value (" +this.max + ") must be greater than or equal to the min value (" + this.min + ").");
		}
		
		// Validate the default value.
		if(defaultValue != null) {
			if(this.wholeNumber && (! isWholeNumber(defaultValue))) {
				throw
					new DomainException(
						"The default must be a whole number: " + defaultValue);
			}
			else if(defaultValue.compareTo(min) < 0) {
				throw
					new DomainException(
						"The default value is smaller than the minimum " +
							"value (" +
							min +
							"): " +
							defaultValue);
			}
			else if(defaultValue.compareTo(max) > 0) {
				throw 
					new DomainException(
						"The default value is greater than the maximum " +
							"value (" +
							max +
							"): " +
							defaultValue);
			}
		}
		this.defaultValue = defaultValue;
		
	}
	
	/**
	 * Returns the lower bound for a response to this prompt.
	 * 
	 * @return The lower bound for a response to this prompt.
	 */
	public BigDecimal getMin() {
		return min;
	}
	
	/**
	 * Returns the upper bound for a response to this prompt.
	 * 
	 * @return The upper bound for a response to this prompt.
	 */
	public BigDecimal getMax() {
		return max;
	}
	
	/**
	 * Returns the default value.
	 * 
	 * @return The default value. This may be null if none was given.
	 */
	public BigDecimal getDefault() {
		return defaultValue;
	}
	
	/**
	 * Returns the wholeNumber value.
	 * 
	 * @return The wholeNumber value. 
	 */
	public boolean getWholeNumber() {
		return wholeNumber;
	}
	
	/**
	 * Verifies that the value of the condition is not outside the bounds of
	 * the prompt.
	 * 
	 * @param pair The condition-value pair to validate.
	 * 
	 * @throws DomainException The value was not a number or was outside the
	 * 						   predefined bounds.
	 */
	@Override
	public void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		try {
			DecimalFormat decimalFormat = new DecimalFormat();
			decimalFormat.setParseBigDecimal(true);
			BigDecimal value =
				(BigDecimal) decimalFormat.parse(pair.getValue());
			
			// Verify that the default is not less than the minimum.
			if(value.compareTo(getMin()) < 0) {
				throw new DomainException(
						"The value of the condition is less than the " +
							"minimum allowed value (" + 
							getMin() +
							"): " +
							pair.getValue());
			}
			// Verify that the default is not greater than the maximum.
			else if(value.compareTo(getMax()) > 0) {
				throw new DomainException(
						"The value of the condition is greater than the " +
							"maximum allowed value (" + 
							getMax() +
							"): " +
							pair.getValue());
			}
			// Verify that the condition is a whole number if the response
			// must be a whole number
			else if(getWholeNumber() && ! isWholeNumber(value)) {
				throw
					new DomainException(
						"The value of the condition is a decimal, but the " +
							"flag indicatest that only whole numbers are " +
							"possible.");
			}
		}
		catch(ParseException e) {
			throw new DomainException(
					"The value of the condition is not a number: " + 
						pair.getValue(),
					e);
		}
	}

	/**
	 * Validates that a given value is valid and, if so, converts it into an 
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the  
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link AtomicInteger}</li>
	 * 				<li>{@link AtomicLong}</li>
	 * 				<li>{@link BigInteger}</li>
	 * 				<li>{@link Integer}</li>
	 * 				<li>{@link Long}</li>
	 * 				<li>{@link Short}</li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>A whole number.</li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return A {@link Number} object or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		BigDecimal result;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt, '" +
							getId() +
							"', was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a number, be sure it is a whole number, if required.
		else if(value instanceof Number) {
			result = new BigDecimal(value.toString());
			if(wholeNumber && (! isWholeNumber(result))) {
				throw
					new DomainException(
						"The value cannot be a decimal: " + value);
			}
		}
		// If it is a string, parse it to check if it's a NoResponse value and,
		// if not, parse it as a long. If that does not work either, throw an
		// exception.
		else if(value instanceof String) {
			String stringValue = (String) value;
			
			try {
				//throw new NoResponseException(NoResponse.valueOf(stringValue));
				return NoResponse.valueOf(stringValue);
			}
			catch(IllegalArgumentException iae) {
				// Parse it.
				try {
					DecimalFormat format = new DecimalFormat();
					format.setParseBigDecimal(true);
					result = (BigDecimal) format.parse(stringValue);
				}
				catch(ParseException e) {
					throw
						new DomainException(
							"The value could not be decoded as a number: " +
								stringValue,
							e);
				}
			}

			// Validate it.
			if(wholeNumber && (! isWholeNumber(result))) {
				throw
					new DomainException(
						"The value cannot be a decimal: " + value);
			}
		}
		// Finally, if its type is unknown, throw an exception.
		else {
			throw new DomainException(
					"The value is not decodable as a response value for " +
						"prompt '" +
						getId() +
						"'.");
		}
		
		// Now that we have a Number value, verify that it is within bounds.
		if(min.compareTo(result) > 0) {
			throw new DomainException(
					"The value is less than the lower bound (" +
						min + 
						") for the prompt, '" +
						getId() +
						"': " + 
						result);
		}
		else if(max.compareTo(result) < 0) {
			throw new DomainException(
					"The value is greater than the upper bound (" +
						max +
						") for the prompt, '" +
						getId() +
						"': " +
						result);
		}
		
		return result;
	}
	
	/**
	 * Creates a JSONObject that represents this bounded prompt.
	 * 
	 * @return A JSONObject that represents this bounded prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_LOWER_BOUND, min);
		result.put(JSON_KEY_UPPER_BOUND, max);
		result.put(JSON_KEY_DEFAULT, defaultValue);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.SurveyItem#toConcordia(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// The response is always an object.
		generator.writeStartObject();
		generator.writeStringField("type", "object");
		
		// The fields array.
		generator.writeArrayFieldStart("schema");
		
		// The first field in the object is the prompt's ID.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_PROMPT_ID);
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		// The second field in the object is the response's value.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_RESPONSE);
		generator.writeStringField("type", "number");
		generator.writeEndObject();
		
		// End the array of fields.
		generator.writeEndArray();
		
		// End the object.
		generator.writeEndObject();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result =
			prime *
				result +
				((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		result = prime * result + (wholeNumber ? 1231 : 1237);
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
		if(!super.equals(obj)) {
			return false;
		}
		if(!(obj instanceof BoundedPrompt)) {
			return false;
		}
		
		BoundedPrompt other = (BoundedPrompt) obj;
		if(defaultValue == null) {
			if(other.defaultValue != null) {
				return false;
			}
		}
		else if(!defaultValue.equals(other.defaultValue)) {
			return false;
		}
		
		if(max == null) {
			if(other.max != null) {
				return false;
			}
		}
		else if(!max.equals(other.max)) {
			return false;
		}
		
		if(min == null) {
			if(other.min != null) {
				return false;
			}
		}
		else if(!min.equals(other.min)) {
			return false;
		}

		if(wholeNumber != other.wholeNumber) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns whether or not a given value is a whole number.
	 * 
	 * @param value
	 *        The value to check.
	 * 
	 * @return True if the BigDecimal is a whole number; false, otherwise.
	 */
	protected boolean isWholeNumber(final BigDecimal value) {
		try {
			return value.setScale(0, RoundingMode.DOWN).compareTo(value) == 0;
		}
		catch(ArithmeticException e) {
			return false;
		}
	}
	
}