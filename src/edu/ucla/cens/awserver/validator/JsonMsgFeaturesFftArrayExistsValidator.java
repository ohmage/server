package edu.ucla.cens.awserver.validator;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

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
	public boolean validate(AwRequest request, JSONObject jsonObject) {		
		JSONObject object = JsonUtils.getObjectFromJson(jsonObject, "features");
		JSONArray array = JsonUtils.getJsonArrayFromJson(object, _key);
		
		if(null == array) {
			getAnnotator().annotate(request, _key + " array from features object in message is null or invalid");
			return false;
		}
		
		return true;
	}
}
