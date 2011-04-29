package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates that the list of classes exist and is not empty, but it doesn't
 * do any actual validation as to if the classes are legitimate or not.
 * 
 * @author John Jenkins
 */
public class ClassListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(ClassListValidator.class);
	
	private boolean _required;
	
	/**
	 * Creates a new validator for the list of classes.
	 * 
	 * @param annotator The annotator should something fail.
	 */
	public ClassListValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}
	
	/**
	 * Validates the list of classes. At this point, it simply validates that
	 * the list is not empty, and that each item in the list is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating initial list of classes for new campaign.");
		
		String classes = (String) awRequest.getToValidate().get(InputKeys.CLASS_URN_LIST);
		if(StringUtils.isEmptyOrWhitespaceOnly(classes)) {
			if(_required) {
				_logger.error("Missing required class list parameter. This should have been caught before this.");
				throw new ValidatorException("Missing required class list.");
			}
			else {
				return true;
			}
		}
		
		try {
			String[] classList = classes.split(",");
			if(classList.length == 0) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "Class list is empty.");
				return false;
			}

			for(int i = 0; i < classList.length; i++) {
				if(! classList[i].startsWith("urn:")) {
					awRequest.setFailedRequest(true);
					getAnnotator().annotate(awRequest, "Invalid URN in class list: " + classList[i]);
					return false;
				}
			}
		}
		catch(NullPointerException e) {
			// This may be because the last character is a comma, but for now
			// we will lump this in with the rest and call it an error.
			awRequest.setFailedRequest(true);
			getAnnotator().annotate(awRequest, "Weird class list " + classes);
			return false;
		}
		
		try {
			awRequest.addToProcess(InputKeys.CLASS_URN_LIST, classes, true);
		}
		catch(IllegalArgumentException e) {
			throw new ValidatorException("Error while trying to add the list of classes to the toProcess map.", e);
		}
		
		return true;
	}

}
