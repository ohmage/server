package edu.ucla.cens.awserver.validator;

import org.andwellness.utils.StringUtils;
import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;

public class GenericUrnValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(GenericUrnValidator.class);
	
	private String _key;
	private boolean _required;
	
	public GenericUrnValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or an empty string.");
		}
		
		_key = key;
		_required = required;
	}

	@Override
	public boolean validate(AwRequest awRequest) {
		// TODO Auto-generated method stub
		return false;
	}

}
