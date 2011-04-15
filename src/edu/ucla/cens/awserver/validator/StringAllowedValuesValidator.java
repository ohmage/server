package edu.ucla.cens.awserver.validator;

import java.util.List;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class StringAllowedValuesValidator extends AbstractAnnotatingValidator {
	//private Logger _logger = Logger.getLogger(StringAllowedValuesValidator.class);
	private List<String> _allowedValues;
	private String _key;
	private boolean _required;
	private String _errorMessage;
	
	public StringAllowedValuesValidator(AwRequestAnnotator annotator, String key, List<String> allowedValues , boolean required) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		if(null == allowedValues || allowedValues.size() < 1) {
			throw new IllegalArgumentException("allowed values must not be null and must contain at least one entry");
		}
		_allowedValues = allowedValues;
		_key = key;
		_required = required;
	}
	
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
			if(! _allowedValues.contains(value)) {
				getAnnotator().annotate(awRequest, "found disallowed value for " + _key + ". value: " + value);
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
