package edu.ucla.cens.awserver.validator.json;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for doubles present in features objects present in mobility mode_features messages.
 * 
 * @author selsky
 */
public class JsonMsgFeaturesFftArrayExistsValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "fft";
		
	public JsonMsgFeaturesFftArrayExistsValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * Validates the existence of an array named "fft" from a JSON Object named "features". Assumes the features object exists.
	 * 
	 * @return true if the array contains ten valid doubles
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "features");
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(object, _key);
		
		if(null == array) {
			getAnnotator().annotate(awRequest, _key + " array from features object in message is null or invalid");
			return false;
		}
		
		return true;
	}
}
