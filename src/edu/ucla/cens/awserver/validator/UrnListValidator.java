package edu.ucla.cens.awserver.validator;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
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
		_logger.info("validating a URN list using property name " + _propertyName);
		
		Map<String, Object> toValidate = awRequest.getToValidate();
		
		if(null == toValidate) {
			throw new IllegalArgumentException("missing toValidate property in AwRequest");
		}
		
		if(_required) {
			if(null == toValidate.get(_propertyName)) {
				_logger.info("missing required property " + _propertyName);
				return false;
			}
		} 
		
		String urnList = (String) toValidate.get(_propertyName);
		
		if(null != urnList) { // validate the actual content
			
			String[] urns = urnList.split(",");
			
			for(String urn : urns) {
				if(! StringUtils.isValidUrn(urn)) {
					getAnnotator().annotate(awRequest, "found invalid urn: " + urn);
					return false;
				}
			}
		}
		
		return true;
	}	
}
