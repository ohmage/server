package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates a comma-delimited list of URNs.
 * 
 * @author selsky
 */
public class UrnListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(UrnListValidator.class);
	private String _propertyName;
	private boolean _required;
	
	public UrnListValidator(AwRequestAnnotator awRequestAnnotator, String propertyName, boolean required) {
		super(awRequestAnnotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(propertyName)) {
			throw new IllegalArgumentException("propertyName must not be null or empty");
		}
		
		_propertyName = propertyName;
		_required = required;
	}
	
	public boolean validate(AwRequest awRequest) {
		String urnList;
		try {
			urnList = (String) awRequest.getToProcessValue(_propertyName);
		}
		catch(IllegalArgumentException outerException) {
			try {
				urnList = (String) awRequest.getToValidateValue(_propertyName);
				
				if(urnList == null) {
					if(_required) {
						throw new IllegalArgumentException("The required URN list is missing: " + _propertyName);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new IllegalArgumentException("The required URN list is missing: " + _propertyName);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating a URN list using property name " + _propertyName);
		
		String[] urns = urnList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(String urn : urns) {
			if(! StringUtils.isValidUrn(urn)) {
				getAnnotator().annotate(awRequest, "found invalid urn: " + urn);
				return false;
			}
		}
		
		awRequest.addToProcess(_propertyName, urnList, true);
		return true;
	}	
}
