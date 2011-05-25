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
		if(classes == null) {
			if(_required) {
				_logger.error("Missing required class list parameter. This should have been caught before this.");
				throw new ValidatorException("Missing required class list.");
			}
			else {
				return true;
			}
		}

		String[] classList = classes.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classList.length; i++) {
			if(! StringUtils.isValidUrn(classList[i])) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "Invalid URN in class list: " + classList[i]);
				return false;
			}
		}
		
		awRequest.addToProcess(InputKeys.CLASS_URN_LIST, classes, true);
		return true;
	}

}
