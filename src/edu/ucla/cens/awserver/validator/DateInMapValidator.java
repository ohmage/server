package edu.ucla.cens.awserver.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.ReflectionUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Poorly named class that uses the toValidate Map in the AwRequest to retrieve dates to validate.
 * 
 * @author selsky
 * @see java.text.SimpleDateFormat
 * @see DateValidator
 */
public class DateInMapValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DateInMapValidator.class);
	private String _key;
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
	public DateInMapValidator(AwRequestAnnotator annotator, String key, String format, boolean required) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(format)) {
			throw new IllegalArgumentException("a format is required");
		}
		
		// Create a new SimpleDateFormat to just throw it away
		// The reason this is done is because SimpleDateFormat is not synchronized 
		new SimpleDateFormat(format);
		_dateFormat = format;
		_key = key;
		_required = required;
	}
	
	/**
	 * @return true if the date is valid for the format
	 * @false otherwise
	 */
	public boolean validate(AwRequest awRequest) {
		if(null == awRequest.getToValidate()) {
			throw new IllegalArgumentException("missing required toValidate Map in AwRequest");
		}
		
		String value = (String) awRequest.getToValidate().get(_key);
		
		if(_required) {
			if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
				getAnnotator().annotate(awRequest, _errorMessage);
				return false;
			}	
		}
		
		if(null != value) { // validate the content
			
			try {
				
				SimpleDateFormat formatter = new SimpleDateFormat(_dateFormat);
				formatter.setLenient(false);
				formatter.parse(value);
				
			} catch (ParseException pe) {
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("invalid date: " + value);
				}
				
				getAnnotator().annotate(awRequest, _errorMessage);
				return false;
			}
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
