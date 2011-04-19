package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Validates that the list of classes exist and is not empty, but it doesn't
 * do any actual validation as to if the classes are legitimate or not.
 * 
 * @author John Jenkins
 */
public class ClassListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(ClassListValidator.class);
	
	/**
	 * Creates a new validator for the list of classes.
	 * 
	 * @param annotator The annotator should something fail.
	 */
	public ClassListValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Validates the list of classes. At this point, it simply validates that
	 * the list is not empty, and that each item in the list is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating initial list of classes for new campaign.");
		
		String classes = (String) awRequest.getToValidate().get(CampaignCreationAwRequest.KEY_LIST_OF_CLASSES_AS_STRING);
		if((classes == null) || ("".equals(classes))) {
			awRequest.setFailedRequest(true);
			getAnnotator().annotate(awRequest, "Class list is empty.");
			return false;
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
			awRequest.addToProcess(CampaignCreationAwRequest.KEY_LIST_OF_CLASSES_AS_STRING, classes, true);
		}
		catch(IllegalArgumentException e) {
			throw new ValidatorException("Error while trying to add the list of classes to the toProcess map.", e);
		}
		
		return true;
	}

}
