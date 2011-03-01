package edu.ucla.cens.genjson;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Simulator of phone/sensor messages that correspond to the mobility type and mode_only subtype.
 * 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * on the wiki for details.
 * 
 * @author selsky
 */
public class MobilityModeOnlyJsonMessageCreator implements JsonMessageCreator {

// Message example	
//	{
//	    "date":"2009-11-03 10:18:33",
//	    "time":1257272467077,
//	    "timezone":"EST",
//	    "subtype":"mode_only",
//      "location_status":"valid",	
//	    "location": {
//	        "latitude":38.8977,
//	        "longitude":-77.0366,
//          "accuracy":0.9283213,
//          "provider":"GPS",
//          "timestamp":"2009-11-03 10:18:33"
//	    },
//	    "mode":"still" 
//	}
	
	/**
	 * Returns a JSONArray containing <code>numberOfEntries</code> JSONObjects where each JSONObject is a mobility mode_only
	 * entry. 
	 */
	public JSONArray createMessage(int numberOfEntries) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		String subtype = "mode_only";
		
		for(int i = 0; i < numberOfEntries; i++) {
			try { Thread.sleep(100); } catch (InterruptedException ie) { } // ensure variable dates
			String date = ValueCreator.date();
			double latitude = ValueCreator.latitude();
			double longitude = ValueCreator.longitude();
			String mode = ValueCreator.mode();
			long epoch = ValueCreator.epoch();
			String locationStatus = ValueCreator.randomLocationStatus();
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("date", date);
			map.put("time", epoch);
			map.put("timezone", tz);
			map.put("subtype", subtype);
			map.put("mode", mode);
			map.put("location_status", locationStatus);
			
			if(! "unavailable".equals(locationStatus)) {
				Map<String, Object> location = new HashMap<String, Object>();
				location.put("latitude", latitude);
				location.put("longitude", longitude);
				location.put("accuracy", ValueCreator.randomPositiveFloat());
				location.put("provider", ValueCreator.randomProvider());
				location.put("timestamp", ValueCreator.date());
				map.put("location", location);
			}
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
