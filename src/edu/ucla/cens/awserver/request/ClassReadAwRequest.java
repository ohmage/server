package edu.ucla.cens.awserver.request;

import java.util.Map;

/**
 * The internal representation of a class read request.
 * 
 * @author John Jenkins
 */
public class ClassReadAwRequest extends ResultListAwRequest {
	/**
	 * Populates a class read request with the list of classes.
	 * 
	 * @param classList A comma-separated list of class URNs that
	 */
	public ClassReadAwRequest(String classList) {
		if(classList == null) {
			throw new IllegalArgumentException("Cannot have a null class list.");
		}
		
		Map<String, Object> toValidate = getToValidate();
		toValidate.put(InputKeys.CLASS_URN_LIST, classList);
		setToValidate(toValidate);
	}
}
