package edu.ucla.cens.awserver.validator;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

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
	public boolean validate(AwRequest request, JSONObject jsonObject) {
		Long epoch = JsonUtils.getLongFromJson(jsonObject, _key);
		
		if(null == epoch) {
			getAnnotator().annotate(request, "time in message is null");
			return false;
		}
		
		if(epoch.longValue() < 0) { // before 1/1/1970
			getAnnotator().annotate(request, "epoch time < 0");
			return false;
		}
		
		return true;
	}
}
