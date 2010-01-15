package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for *internally set* AwRequest attributes. This validator should only be used to check required values that are 
 * internally set by the HTTP layer (servlets, filters, AwRequestCreators) where it is a logical error for the value to be missing. 
 * E.g., most controllers in AW require a subdomain attribute in the AwRequest, but the subdomain value is set by parsing the  
 * URL string, not by a user-driven ("external") action. Put another way, it is a logical error for the subdomain to be missing.
 * For validation of user-driven data, use a subclass of AbstractAnnotatingValidator.
 * 
 * @author selsky
 */
public class NonEmptyStringValidator implements Validator {
	private String _awRequestAttributeName;

	/**
	 * Creates an instance of this class where the payloadItemName parameter is used as a key to retrieve the value that will be 
	 * validated. The value will be retrieved from the payload Map in the AwRequest.
	 * 
	 * @throws IllegalArgumentException if the payloadItemName parameter is empty or null or all whitespace
	 */
	public NonEmptyStringValidator(String awRequestAttributeName) {
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequestAttributeName)) {
			throw new IllegalArgumentException("cannot create class: empty awRequestAttributeName parameter.");
		}
		
		_awRequestAttributeName = awRequestAttributeName;
	}
	
	/**
	 * Using the key passed in on construction, retrieves a value from the payload Map from the passed in AwRequest and checks
	 * whether it is empty.
     *
	 * @return true if the value is a non-empty, non-all-whitespace string
	 * @throws ValidatorException if the retrieved value is null, empty, or all whitespace 
	 */
	public boolean validate(AwRequest awRequest) {
		if(StringUtils.isEmptyOrWhitespaceOnly((String) awRequest.getAttribute(_awRequestAttributeName))) {
			return true;
		}
		throw new ValidatorException("missing " + _awRequestAttributeName + " from AwRequest - cannot continue processing");
	}
}
