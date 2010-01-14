package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Simulator of phone/sensor messages that correspond to the mobility type and mode_features subtype. 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * on the wiki for details.
 * 
 * @author selsky
 */
public class MobilityModeFeaturesJsonMessageCreator implements JsonMessageCreator {

// Example message
//	{
//        "date":"2009-11-03 10:18:33",
//        "time":1257272467077,
//        "timezone":"EST",
//        "subtype":"mode_features",
//        "location": {
//            "latitude":38.8977,
//            "longitude":-77.0366
//        },
//        "features":{
//            "mode":"still",
//            "speed":0.0,
//            "variance":0.0,
//            "average":0.0,
//            "fft":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]
//        }
//    },
	
	/**
	 * Creates numberOfEntries messages and places them into the returned JSONArray.
	 */
	public JSONArray createMessage(int numberOfEntries) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		String subtype = "mode_features";
	
		for(int i = 0; i < numberOfEntries; i++) {
			String date = ValueCreator.date();
			double latitude = ValueCreator.latitude();
			double longitude = ValueCreator.longitude();
			String mode = ValueCreator.mode();
			long epoch = ValueCreator.epoch();
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("date", date);
			map.put("time", epoch);
			map.put("timezone", tz);
			map.put("subtype", subtype);
			
			Map<String, Object> location = new HashMap<String, Object>();
			location.put("latitude", latitude);
			location.put("longitude", longitude);
			map.put("location", location);
			
			Map<String, Object> features = new HashMap<String, Object>();
			features.put("mode", mode);
			features.put("speed", ValueCreator.randomPositiveDouble());
			features.put("variance", ValueCreator.randomPositiveDouble());
			features.put("average", ValueCreator.randomPositiveDouble());
			
			List<Double> fft = new ArrayList<Double>();
			for(int j = 0; j < 10; j++) {
				fft.add(ValueCreator.randomPositiveDouble());
			}
			features.put("fft", fft);
			map.put("features", features);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
