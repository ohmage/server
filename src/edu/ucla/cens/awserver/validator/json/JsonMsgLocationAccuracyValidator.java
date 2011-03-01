package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the location accuracy element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgLocationAccuracyValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "accuracy";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgLocationAccuracyValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location");
		String accuracy = JsonUtils.getStringFromJsonObject(object, _key); // annoyingly, the JSON lib does not have a getFloat(..)
		
		if(StringUtils.isEmptyOrWhitespaceOnly(accuracy)) {
			getAnnotator().annotate(awRequest, "missing accuracy");
			return false;
		}
		
		try {
		
			Float.parseFloat(accuracy);
			
		} catch (NumberFormatException nfe) {
			
			getAnnotator().annotate(awRequest, "unparseable float. " + nfe.getMessage() + " value: " + accuracy);
			return false;
			
		}
		
		return true;
		
	}
}
