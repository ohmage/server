package edu.ucla.cens.awserver.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.ReflectionUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for AwRequest attributes that are required to belong to a set of allowed values.
 * 
 * @author selsky
 */
public class RequiredStringAnnotatingValidator extends AbstractAnnotatingValidator {
	private String _key;
	private Method _stringAccessorMethod;
	private List<String> _allowedValues;
	
	/**
	 * @throws IllegalArgumentException if the awRequestAttributeName is null, empty, or all whitespace 
	 * @throws IllegalArgumentException if the allowedValues List is null or empty
	 * @see ReflectionUtils#getAccessorMethod(Class, String)
	 */
	public RequiredStringAnnotatingValidator(AwRequestAnnotator annotator, String key, List<String> allowedValues) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-empty key is required");
		}
		if(null == allowedValues || allowedValues.isEmpty()){
			throw new IllegalArgumentException("missing or empty allowedValues list");
		}
		_stringAccessorMethod = ReflectionUtils.getAccessorMethod(AwRequest.class, key);
		_key = key;
		_allowedValues = allowedValues;
	}

	/**
	 * @return true if the attribute retrieved with awRequestAttributeName is equal to a value in the allowedValues list
	 * @return false otherwise 
	 */
	public boolean validate(AwRequest awRequest) {
		
		try {
			String attrValue = (String) _stringAccessorMethod.invoke(awRequest);
			
			if(! StringUtils.isEmptyOrWhitespaceOnly(attrValue)) {
			
				for(String allowedValue : _allowedValues) {
					if(attrValue.equals(allowedValue)) {
						return true;
					}
				}
			}
			
			getAnnotator().annotate(awRequest, "invalid value found for " + _key + ". value:  " + attrValue);
			return false;
		}
		catch(InvocationTargetException ite) {
			
	    	throw new ValidatorException(ite);
		
		} catch(IllegalAccessException iae) {
			
			throw new ValidatorException(iae);
		}
	}
}
