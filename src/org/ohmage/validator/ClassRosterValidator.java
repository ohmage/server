package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Validates that the roster is well-formed.
 * 
 * @author John Jenkins
 */
public class ClassRosterValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(ClassRosterValidator.class);
	
	private final boolean _required;
	
	/**
	 * Builds this validator.
	 * 
	 * @param annotator The validator with which to respond should the roster
	 * 					be invalid.
	 * 
	 * @param required Whether or not this check is required.
	 */
	public ClassRosterValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}

	/**
	 * Validates that the roster is valid in that each line contains exactly
	 * three comma-separated items and that the third is a valid class role.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		// Get the roster value from the request.
		String roster;
		try {
			roster = (String) awRequest.getToProcessValue(InputKeys.ROSTER);
		}
		catch(IllegalArgumentException outerException) {
			try {
				roster = (String) awRequest.getToValidateValue(InputKeys.ROSTER);
				
				if(roster == null) {
					if(_required) {
						throw new ValidatorException("Missing required key: " + InputKeys.ROSTER);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required key: " + InputKeys.ROSTER);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating that the class roster is a valid class roster and that all of the class roles are valid class roles.");
		
		// If it is an empty string meaning that there was nothing in the 
		// roster, then that is acceptable.
		if(! "".equals(roster)) {
			String[] rosterLines = roster.split("\n");
			
			// For each line in the roster...
			for(int i = 0; i < rosterLines.length; i++) {
				String[] rosterLineValues = rosterLines[i].split(InputKeys.LIST_ITEM_SEPARATOR);
				
				// Ensure that there are three entries.
				if(rosterLineValues.length != 3) {
					getAnnotator().annotate(awRequest, "The following line was malformed: " + rosterLines[i]);
					awRequest.setFailedRequest(true);
					return false;
				}
				
				// Ensure that the third entry is a valid class role.
				String classRole = rosterLineValues[2];
				if((! ClassRoleCache.ROLE_PRIVILEGED.equals(classRole)) && (! ClassRoleCache.ROLE_RESTRICTED.equals(classRole))) {
					getAnnotator().annotate(awRequest, "Invalid class role found: " + classRole);
					awRequest.setFailedRequest(true);
					return false;
				}
			}
		}
		
		awRequest.addToProcess(InputKeys.ROSTER, roster, true);
		return true;
	}
}