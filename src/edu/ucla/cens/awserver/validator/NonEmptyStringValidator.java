package edu.ucla.cens.awserver.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.ReflectionUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for *internally set* AwRequest attributes. This validator should only be used to check required values that are 
 * internally set by the HTTP layer (servlets, filters, AwRequestCreators) where it is a logical error for the value to be missing. 
 * For validation of user-driven data, use a subclass of AbstractAnnotatingValidator.
 * 
 * @author selsky
 */
public class NonEmptyStringValidator implements Validator {
	private Method _stringAccessorMethod;
	private String _key;

	/**
	 * Creates an instance of this class where the payloadItemName parameter is used as a key to retrieve the value that will be 
	 * validated. The value will be retrieved from the payload Map in the AwRequest.
	 * 
	 * @throws IllegalArgumentException if the payloadItemName parameter is empty or null or all whitespace
	 * @see ReflectionUtils#getAccessorMethod(Class, String)
	 */
	public NonEmptyStringValidator(String key) {
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-empty key is required");
		}
		_key = key;
		_stringAccessorMethod = ReflectionUtils.getAccessorMethod(AwRequest.class, key);
	}
	
	/**
	 * Using the key passed in on construction, retrieves a value from the payload Map from the passed in AwRequest and checks
	 * whether it is empty.
     *
	 * @return true if the value is a non-null, non-empty, non-all-whitespace string
	 * @throws ValidatorException if the retrieved value is null, empty, or all whitespace 
	 */
	public boolean validate(AwRequest awRequest) {
		try {
			if(StringUtils.isEmptyOrWhitespaceOnly((String) _stringAccessorMethod.invoke(awRequest))) {
				throw new ValidatorException("missing " + _key + " from AwRequest - cannot continue processing");
			}
		
			return true;
		}
	    catch(InvocationTargetException ite) {
		
	    	throw new ValidatorException(ite);
		
		} catch(IllegalAccessException iae) {
			
			throw new ValidatorException(iae);
		}
	}
}
