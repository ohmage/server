package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the speed element from a sensor_data mobility message.
 * 
 * This class is nearly identical to JsonMsgLocationAccuracyValidator.
 * 
 * @author selsky
 */
public class JsonMsgSensorDataSpeedValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "speed";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSensorDataSpeedValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "data");
		String speed = JsonUtils.getStringFromJsonObject(object, _key); // annoyingly, the JSON lib does not have a getFloat(..)
		
		if(StringUtils.isEmptyOrWhitespaceOnly(speed)) {
			getAnnotator().annotate(awRequest, "missing speed");
			return false;
		}
		
		try {
		
			Float.parseFloat(speed);
			
		} catch (NumberFormatException nfe) {
			
			getAnnotator().annotate(awRequest, "unparseable float. " + nfe.getMessage() + " value: " + speed);
			return false;
			
		}
		
		return true;
	}
}
