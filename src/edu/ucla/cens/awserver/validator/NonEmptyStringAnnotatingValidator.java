package edu.ucla.cens.awserver.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.ReflectionUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for checking the existence of String values in an AwRequest.
 * 
 * @author selsky
 */
public class NonEmptyStringAnnotatingValidator extends AbstractAnnotatingValidator {
	private String _awRequestAttributeName;
	private Method _stringAccessorMethod;
	
	/**
	 * @throws IllegalArgumentException if the provided key is null, empty, or all whitespace
	 * @see ReflectionUtils#getAccessorMethod(Class, String)
	 */
	public NonEmptyStringAnnotatingValidator(AwRequestAnnotator annotator, String key) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-empty key is required");
		}
		_stringAccessorMethod = ReflectionUtils.getAccessorMethod(AwRequest.class, key);
	}
	
	/**
	 * @return true if the value found for awRequestAttributeName is non-null, non-empty, and non-all-whitespace
	 * @return false otherwise 
	 */
	public boolean validate(AwRequest awRequest) {
		try {
			
			if(StringUtils.isEmptyOrWhitespaceOnly((String) _stringAccessorMethod.invoke(awRequest))) {
				getAnnotator().annotate(awRequest, "missing value in AwRequest for " + _awRequestAttributeName);
				return false;
			}
		
			return true;
			
		} catch(InvocationTargetException ite) {
			
			throw new ValidatorException(ite);
			
		} catch(IllegalAccessException iae) {
			
			throw new ValidatorException(iae);
		}
	}
}
