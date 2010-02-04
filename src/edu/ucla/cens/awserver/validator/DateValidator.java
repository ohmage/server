package edu.ucla.cens.awserver.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Date validator for AwRequest attributes.
 * 
 * @author selsky
 * @see java.text.SimpleDateFormat
 */
public class DateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DateValidator.class);
	private String _key;
	private SimpleDateFormat _dateFormat;
	private String _errorMessage;
	
	/**
	 * @throws IllegalArgumentException if the provided key is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided format is not a valid SimpleDateFormat
	 */
	public DateValidator(AwRequestAnnotator annotator, String key, String format) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(format)) {
			throw new IllegalArgumentException("a format is required");
		}
		_key = key;
		_dateFormat = new SimpleDateFormat(format);
	}
	
	/**
	 * Uses the key provided on construction to retrieve a date value from the AwRequest. Uses the format (as a SimpleDateFormat)
	 * to parse the date value.
	 * 
	 * @return true if the date is valid for the format
	 * @false otherwise
	 */
	public boolean validate(AwRequest awRequest) {
		String stringDate = (String) awRequest.getAttribute(_key);
		try {
			
			_dateFormat.parse(stringDate);
			
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
