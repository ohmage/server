package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author selsky
 */
public class MobilityModeExtendedJsonMessageCreator implements JsonMessageCreator {

// Example message
//	{
//        "date":"2009-11-03 10:18:33",
//        "time":1257272467077,
//        "timezone":"EST",
//        "subtype":"sensor_data",
//        "location": {
//            "latitude":38.8977,
//            "longitude":-77.0366,
//            "accuracy":0.9283213,
//            "provider":"GPS",
//            "timestamp":"2009-11-03 10:18:33"
//        },
//        "data":{
//   	      "mode":"still",
//            "speed":0.0,
//            "accel_data":[{"x":0.0, "y":0.0,"z",0.0}],
//            "wifi_data":[{"ssid":"blah","strength:100}]
//        }
//    },
	
	/**
	 * Creates numberOfEntries messages and places them into the returned JSONArray.
	 */
	public JSONArray createMessage(int numberOfEntries) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		String subtype = "sensor_data";
	
		for(int i = 0; i < numberOfEntries; i++) {
			try { Thread.sleep(100); } catch (InterruptedException ie) { } // ensure variable dates
			
			String date = ValueCreator.date();
			double latitude = ValueCreator.latitude();
			double longitude = ValueCreator.longitude();
			long epoch = ValueCreator.epoch();
			String locationStatus = ValueCreator.randomLocationStatus();
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("date", date);
			map.put("time", epoch);
			map.put("timezone", tz);
			map.put("subtype", subtype);
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
			
			Map<String, Object> sensorData = new HashMap<String, Object>();
			sensorData.put("speed", ValueCreator.randomPositiveDouble());
			
			List<Map<String,Object>> accelEntries = new ArrayList<Map<String,Object>>();
			int numberOfAccelEntries = ValueCreator.randomPositiveIntModulus(30) + 1;
			for(int j = 0; j < numberOfAccelEntries; j++) {
				Map<String, Object> points = new HashMap<String, Object>();				
				points.put("x", ValueCreator.randomDouble());
				points.put("y", ValueCreator.randomDouble());
				points.put("z", ValueCreator.randomDouble());
				accelEntries.add(points);
			}
			sensorData.put("accel_data", accelEntries);
			
			int numberOfWifiEntries = ValueCreator.randomPositiveIntModulus(20);
			List<Map<String,Object>> wifiScanEntries = new ArrayList<Map<String,Object>>();
			for(int j = 0; j < numberOfWifiEntries; j++) {
				Map<String, Object> entry = new HashMap<String, Object>();
				entry.put("ssid", ValueCreator.randomMacAddress());
				int s = ValueCreator.randomPositiveIntModulus(120);
				entry.put("strength", -s);
				wifiScanEntries.add(entry);
			}
			
			Map<String, Object> wifiData = new HashMap<String, Object>();
			wifiData.put("scan", wifiScanEntries);
			wifiData.put("timestamp", ValueCreator.date());
			sensorData.put("wifi_data", wifiData);
			
			sensorData.put("mode", ValueCreator.mode());
			
			map.put("data", sensorData);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
