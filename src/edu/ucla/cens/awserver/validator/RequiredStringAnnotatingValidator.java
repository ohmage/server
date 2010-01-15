package edu.ucla.cens.awserver.validator;

import java.util.List;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for AwRequest attributes that are required to belong to a set of allowed values.
 * 
 * @author selsky
 */
public class RequiredStringAnnotatingValidator extends AbstractAnnotatingValidator {
	private String _awRequestAttributeName;
	private List<String> _allowedValues;
	
	/**
	 * @throws IllegalArgumentException if the awRequestAttributeName is null, empty, or all whitespace 
	 * @throws IllegalArgumentException if the allowedValues List is null or empty
	 */
	public RequiredStringAnnotatingValidator(AwRequestAnnotator annotator, String awRequestAttributeName, List<String> allowedValues) {
		super(annotator);
		if(null == awRequestAttributeName) {
			throw new IllegalArgumentException("missing awRequestAttributeName");
		}
		if(null == allowedValues || allowedValues.isEmpty()){
			throw new IllegalArgumentException("missing or empty allowedValues list");
		}
		_awRequestAttributeName = awRequestAttributeName;
		_allowedValues = allowedValues;
	}

	/**
	 * @return true if the attribute retrieved with awRequestAttributeName is equal to a value in the allowedValues list
	 * @return false otherwise 
	 */
	public boolean validate(AwRequest awRequest) {
		String attrValue = (String) awRequest.getAttribute(_awRequestAttributeName);
		if(! StringUtils.isEmptyOrWhitespaceOnly(attrValue)) {
		
			for(String allowedValue : _allowedValues) {
				if(attrValue.equals(allowedValue)) {
					return true;
				}
			}
		}
		
		getAnnotator().annotate(awRequest, "invalid value found for " + _awRequestAttributeName + ". value:  " + attrValue);
		return false;
	}
	
}
