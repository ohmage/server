package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for doubles present in features objects present in mobility mode_features messages.
 * 
 * @author selsky
 */
public class JsonMsgFeaturesDoubleValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key;
		
	/**
     * @throws IllegalArgumentException if the provded String key is empty, null, or all whitespace
	 */
	public JsonMsgFeaturesDoubleValidator(AwRequestAnnotator awRequestAnnotator, String key) {
		super(awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-null, non-empty, non-all-whitespace key is required");
		}
		_key = key;
	}
	
	/**
	 * Validates a double from a JSON Object named "features". Assumes the features object exists.
	 * 
	 * @return true if the value returned from the AwRequest for the key set on construction returns a value that is present in 
	 * the features object and a valid double
	 * @return false otherwise
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {		
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "features");
		Double value = JsonUtils.getDoubleFromJsonObject(object, _key);
		
		if(null == value) {
			getAnnotator().annotate(request, _key + " double from features object in message is null or invalid");
			return false;
		}
		
		return true;
	}
}
