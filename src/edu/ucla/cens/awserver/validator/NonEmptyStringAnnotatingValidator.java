package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for checking the existence of AwRequest attributes.
 * 
 * @author selsky
 */
public class NonEmptyStringAnnotatingValidator extends AbstractAnnotatingValidator {
	private String _awRequestAttributeName;
	
	/**
	 * @throws IllegalArgumentException if the provided awRequestAttributeName is null
	 */
	public NonEmptyStringAnnotatingValidator(AwRequestAnnotator annotator, String awRequestAttributeName) {
		super(annotator);
		if(null == awRequestAttributeName) {
			throw new IllegalArgumentException("a non-null awRequestAttributeName is required");
		}
		_awRequestAttributeName = awRequestAttributeName;
	}
	
	/**
	 * @return true if the value found for awRequestAttributeName is non-null, non-empty, and non-all-whitespace
	 * @return false otherwise 
	 */
	public boolean validate(AwRequest awRequest) {
		if(StringUtils.isEmptyOrWhitespaceOnly((String) awRequest.getAttribute(_awRequestAttributeName))) {
			getAnnotator().annotate(awRequest, "missing value in AwRequest for " + _awRequestAttributeName);
			return false;
		}
		return true;
	}

}
