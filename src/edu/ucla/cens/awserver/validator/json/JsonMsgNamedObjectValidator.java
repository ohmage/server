package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for named JSON Objects in AW JSON messages.
 * 
 * @author selsky
 */
public class JsonMsgNamedObjectValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key;
		
	/**
     * @throws IllegalArgumentException if the provded String key is empty, null, or all whitespace
	 */
	public JsonMsgNamedObjectValidator(AwRequestAnnotator awRequestAnnotator, String key) {
		super(awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-null, non-empty, non-all-whitespace key is required");
		}
		_key = key;
	}
	
	/**
	 * @return true if the value returned from the AwRequest for the key set on construction returns a value that is a JSON Object
	 * @return false otherwise
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {		 
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, _key);
		
		if(null == object) {
			getAnnotator().annotate(request, _key + " object in message is null");
			return false;
		}
		
		return true;
	}
}
