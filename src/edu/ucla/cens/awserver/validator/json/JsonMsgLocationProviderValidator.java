package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the location provider element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgLocationProviderValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "provider";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgLocationProviderValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the value returned from the jsonObject for the key "provider" exists and is not empty or null
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location");
		String provider = JsonUtils.getStringFromJsonObject(object, _key);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(provider)) {
			getAnnotator().annotate(awRequest, "provider in message is null");
			return false;
		}
	
		return true;
	}
}
