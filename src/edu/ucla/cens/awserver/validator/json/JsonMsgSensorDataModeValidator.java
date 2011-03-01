package edu.ucla.cens.awserver.validator.json;

import java.util.List;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the mode element from a sensor_data mobility message.
 * 
 * @author selsky
 */
public class JsonMsgSensorDataModeValidator extends JsonMsgMobilityModeValidator {
	
	public JsonMsgSensorDataModeValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedValues) {
		super(awRequestAnnotator, allowedValues);	
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "data");
		String mode = JsonUtils.getStringFromJsonObject(object, _key); 
		
		if(StringUtils.isEmptyOrWhitespaceOnly(mode)) {
			getAnnotator().annotate(awRequest, "missing mode");
			return false;
		}
		
		if(! _allowedValues.contains(mode)) {
			getAnnotator().annotate(awRequest, "invalid mode: " + mode);
			return false;
		}
		
		return true;
	}
}
