package edu.ucla.cens.awserver.validator.json;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for doubles present in the fft array in mobility mode_features messages.
 * 
 * @author selsky
 */
public class JsonMsgFeaturesFftArrayValuesValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "fft";
		
	public JsonMsgFeaturesFftArrayValuesValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * Validates that the fft array contains ten doubles. Assumes the features object and fft array exist.
	 * 
	 * @return true if the array contains ten valid doubles
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "features");
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(object, _key);
		
		int length = array.length();
		
		if(length != 10) {
			getAnnotator().annotate(awRequest, "fft array contains an invalid number of entries: " + length);
			return false;	
		}
		
		for(int i = 0; i < 10; i++) {
			
			Double value = JsonUtils.getDoubleFromJsonArray(array, i);
			
			if(null == value) {
				
				getAnnotator().annotate(awRequest, "fft array contains an invalid double at index " + i);
				return false;
			}
		}
		
		return true;
	}
}
