package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
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
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location");
		
		// first, look for a double
		Double latlong = JsonUtils.getDoubleFromJsonObject(object, _key);
		
		if(null == latlong) { 
			
			getAnnotator().annotate(awRequest, _key + " in message is null");
			return false;
		
			
		} else {
			
			if("latitude".equals(_key)) {
				
				double latitude = latlong.doubleValue();
				if(latitude < -90d || latitude > 90d) {
					getAnnotator().annotate(awRequest, _key + " is invalid. value: " + latitude);
					return false;
				}
				
			} else if ("longitude".equals(_key)) {
				
				double longitude = latlong.doubleValue();
				if(longitude < -180d || longitude > 180d) {
					getAnnotator().annotate(awRequest, _key + " is invalid. value: " + longitude);
					return false;
				}
				
			}
		}
		
		return true;
	}
}
