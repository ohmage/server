/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.ReflectionUtils;
import org.ohmage.util.StringUtils;


/**
 * Reflection-based Validator for AwRequest date attributes.
 * 
 * @author selsky
 * @see java.text.SimpleDateFormat
 */
public class DateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DateValidator.class);
	private Method _dateAccessorMethod;
	private String _dateFormat;
	private String _errorMessage;
	private boolean _required;
	
	/**
	 * @throws IllegalArgumentException if the provided key is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is not a valid SimpleDateFormat
	 * @throws IllegalArgumentException if the key does not represent an AwRequest attribute
	 * @see ReflectionUtils#getAccessorMethod(Class, String)
	 */
	public DateValidator(AwRequestAnnotator annotator, String key, String format, boolean required) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(format)) {
			throw new IllegalArgumentException("a format is required");
		}
		
		_dateAccessorMethod = ReflectionUtils.getAccessorMethod(AwRequest.class, key);
		
		// Create a new SimpleDateFormat to sanity check the format (pattern) and then just throw it away
		// The reason this is done is because SimpleDateFormat is not synchronized 
		new SimpleDateFormat(format);
		_dateFormat = format;
		
		_required = required;
	}
	
	/**
	 * Uses the key provided on construction to retrieve a date value from the AwRequest. Uses the format (as a SimpleDateFormat)
	 * to parse the date value.
	 * 
	 * @return true if the date is valid for the format
	 * @false otherwise
	 * @throws ValidatorException if the accessor method invoked on the AwRequest throws an Exception
	 * @throws ValidatorException if the accessor method enforces Java language access control and the underlying method is inaccessible 
	 */
	public boolean validate(AwRequest awRequest) {
		String stringDate = null;
		
		try {
			
			stringDate = (String) _dateAccessorMethod.invoke(awRequest);
			
			if(null == stringDate && ! _required) {
				return true;
			}
			
		} catch(InvocationTargetException ite) {
			
			throw new ValidatorException(ite);
			
		} catch(IllegalAccessException iae) {
			
			throw new ValidatorException(iae);
		}
		
		try {
			
			SimpleDateFormat formatter = new SimpleDateFormat(_dateFormat);
			formatter.setLenient(false);
			formatter.parse(stringDate);
			
		} catch (ParseException pe) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("invalid date: " + stringDate);
			}
			
			getAnnotator().annotate(awRequest, _errorMessage);
			return false;
		}
		
		return true;
	}
	
	/**
	 * @throws IllegalArgumentException if the provided error message is null, empty, or all whitespace
	 */
	public void setErrorMessage(String errorMessage) {
		if(StringUtils.isEmptyOrWhitespaceOnly(errorMessage)) {
			throw new IllegalArgumentException("an errorMessage is required");
		}
		_errorMessage = errorMessage;
	}
}
