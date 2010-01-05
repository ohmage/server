package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates a string item from the payload Map from an AwRequest. 
 * 
 * @author selsky
 * @see AwRequest
 */
public class NonEmptyStringValidator implements Validator {
	private String _payloadItemName;

	/**
	 * Creates an instance of this class where the payloadItemName parameter is used as a key to retrieve the value that will be 
	 * validated. The value will be retrieved from the payload Map in the AwRequest.
	 * 
	 * @throws IllegalArgumentException if the payloadItemName parameter is empty or null or all whitespace
	 */
	public NonEmptyStringValidator(String payloadItemName) {
		if(StringUtils.isEmptyOrWhitespaceOnly(payloadItemName)) {
			throw new IllegalArgumentException("cannot create class: empty payloadItemName parameter.");
		}
		
		_payloadItemName = payloadItemName;
	}
	
	/**
	 * Using the key passed in on construction, retrieves a value from the payload Map from the passed in AwRequest and checks
	 * whether it is empty.
	 * @return false if the payload value is empty, null, or all whitespace
	 * @return true otherwise
	 */
	public boolean validate(AwRequest awRequest) {
		return ! (StringUtils.isEmptyOrWhitespaceOnly((String) awRequest.getPayload().get(_payloadItemName)));
	}
}
