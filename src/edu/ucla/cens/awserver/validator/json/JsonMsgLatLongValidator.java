package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the latitude and longitude elements from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgLatLongValidator extends AbstractAnnotatingJsonObjectValidator {
//	private static Logger logger = Logger.getLogger(JsonMsgLatLongValidator.class);
	private String _key;
		
	/**
     * @throws IllegalArgumentException if the key is not equals to latitude or longitude
	 */
	public JsonMsgLatLongValidator(AwRequestAnnotator awRequestAnnotator, String key) {
		super(awRequestAnnotator);
		if(null == key || ! ("latitude".equals(key) || "longitude".equals(key) )) {
			throw new IllegalArgumentException("invalid key: " + key); 
		}
		_key = key;
	}
	
	/**
	 * Checks the latitude or longitude value for the location object. Assumes the location object exists. 
	 * 
	 * @return true if the value returned from the AwRequest for the lat or long exists and is a valid double
	 * @return false otherwise
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {		 
		JSONObject object = JsonUtils.getObjectFromJson(jsonObject, "location");
		
		// first, look for a double
		Double latlong = JsonUtils.getDoubleFromJson(object, _key);
		
		if(null == latlong) { // ok, check for Double.NaN as a JSON string (an allowed special case for lat-long values only)
			
			String stringLatLong = JsonUtils.getStringFromJson(object, _key);
			
			if(null == stringLatLong || ! "Double.NaN".equals(stringLatLong)) {
				getAnnotator().annotate(request, _key + " in message is null or invalid. value: " + stringLatLong);
				return false;
			}
		}
		
		return true;
	}
}
