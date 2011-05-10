package edu.ucla.cens.awserver.validator;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class OutputFormatValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(OutputFormatValidator.class);
	private List<String> _allowedValues;
	
	public OutputFormatValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedValues) {
		super(awRequestAnnotator);
		if(null == allowedValues || allowedValues.isEmpty()) {
			throw new IllegalArgumentException("a non-null, non-empty list is required");
		}
		_allowedValues = allowedValues;
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a SurveyResponseReadAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating output format");
		
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a SurveyResponseReadAwRequest: " + awRequest.getClass());
		}
		
		String outputFormat = ((SurveyResponseReadAwRequest) awRequest).getOutputFormat();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			getAnnotator().annotate(awRequest, "empty output format found");
			return false;
		}
			
		if(! _allowedValues.contains(outputFormat)) {
			getAnnotator().annotate(awRequest, "found an unknown output format: " + outputFormat);
			return false;
		}
		
		return true;
	}
}
