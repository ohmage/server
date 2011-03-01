package edu.ucla.cens.awserver.validator.json;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the accel_data element from a sensor_data mobility message.
 * 
 * @author selsky
 */
public class JsonMsgSensorDataAccelDataValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "accel_data";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSensorDataAccelDataValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "data");
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(object, _key);
		
		if(null == array || 0 == array.length()) {
			getAnnotator().annotate(awRequest, "invalid or non-existent accel_data array in sensor_data mobility message");
			return false;
		}
		
		return true;
	}
}
