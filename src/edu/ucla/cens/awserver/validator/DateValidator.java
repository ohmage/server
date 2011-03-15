package edu.ucla.cens.awserver.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.ReflectionUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for AwRequest date attributes.
 * 
 * @author selsky
 * @see java.text.SimpleDateFormat
 */
public class DateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DateValidator.class);
	private Method _dateAccessorMethod;
	private String _dateFormat;
	private String _errorMessage;
	
	/**
	 * @throws IllegalArgumentException if the provided key is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is not a valid SimpleDateFormat
	 * @throws IllegalArgumentException if the key does not represent an AwRequest attribute
	 * @see ReflectionUtils#getAccessorMethod(Class, String)
	 */
	public DateValidator(AwRequestAnnotator annotator, String key, String format) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(format)) {
			throw new IllegalArgumentException("a format is required");
		}
		
		_dateAccessorMethod = ReflectionUtils.getAccessorMethod(AwRequest.class, key);
		
		// Create a new SimpleDateFormat to just throw it away
		// The reason this is done is because SimpleDateFormat is not synchronized 
		new SimpleDateFormat(format);
		_dateFormat = format;
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
