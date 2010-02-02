package edu.ucla.cens.awserver.validator.prompt;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Some JSON restrictions for prompt responses are defined by maps. The phone/device must send a valid key based on the restriction
 * for each particular map type. All map keys for prompt responses are defined to be integers.
 * 
 * @author selsky
 */
public class MapValidator extends NullValidator {
	private List<Integer> _keyList;
	
	/**
	 * @throws IllegalArgumentException if the provided string is not parseable as JSON
	 */
	public MapValidator(String restriction) {
		JSONObject jsonObject = JsonUtils.getJsonObjectFromString(restriction);
		
		if(null == jsonObject) {
			throw new IllegalArgumentException("restriction is not a valid JSON object");
		}
		
		// All that is needed are the object "keys"
		
		String[] keys = JSONObject.getNames(jsonObject);
		
		if(null == keys) {
			throw new IllegalArgumentException("no keys found in JSONObject");
		}
		
		_keyList = new ArrayList<Integer>();
		
		for(String key : keys) {
			_keyList.add(Integer.parseInt(key));
		}
	}
	
	/**
	 * @return true if the response consists of a known integer key or is the string "null"
	 * @return false otherwise
	 */
	public boolean validate(String response) {
		if(super.validate(response)) {
			return true;
		}
		
		int val = 0;
		
		try {
			
			val = Integer.parseInt(response);
			
		} catch(NumberFormatException nfe) {
			
			return false;
		}
		
		return _keyList.contains(val);
		
	}

}
