package edu.ucla.cens.awserver.validator.json;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
 */
public class JsonMsgValueInListValidator extends AbstractAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgValueInListValidator.class);
	private String _key;
	private List<String> _allowedValues;
	 
	public JsonMsgValueInListValidator(AwRequestAnnotator annotator, String key, List<String> allowedValues) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		
		if(null == allowedValues || allowedValues.isEmpty()) {
			throw new IllegalArgumentException("a non-empty list of values is required");
		}
		
		_key = key;
		_allowedValues = allowedValues;
	}
	
	
	@Override
	public boolean validate(AwRequest awRequest, JSONObject object) {
		String value = JsonUtils.getStringFromJsonObject(object, _key);
		
		if(null == value) {
			_logger.warn("could not retrieve " + _key + " from JSON Object " + object);
			getAnnotator().annotate(awRequest, "could not retrieve " + _key + "from JSON object");
			return false;
		}
		
		
		if(! _allowedValues.contains(value)) {
			_logger.warn("invalid value in JSON Object for " + _key + ". Object: " + object);
			getAnnotator().annotate(awRequest, "invalid value in JSON Object for " + _key);
			return false;
		}
		
		return true;
	}
}
