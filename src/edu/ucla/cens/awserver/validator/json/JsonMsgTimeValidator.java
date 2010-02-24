package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the time element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgTimeValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "time";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgTimeValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the value returned from the AwRequest for the key "time" exists and is > 0.
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		Long epoch = JsonUtils.getLongFromJsonObject(jsonObject, _key);
		
		if(null == epoch) {
			getAnnotator().annotate(awRequest, "time in message is null");
			return false;
		}
		
		if(epoch.longValue() < 0) { // before 1/1/1970
			getAnnotator().annotate(awRequest, "epoch time < 0");
			return false;
		}
		
		return true;
	}
}
