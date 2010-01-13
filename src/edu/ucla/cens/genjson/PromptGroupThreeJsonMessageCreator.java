package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

public class PromptGroupThreeJsonMessageCreator implements JsonMessageCreator {

//	# Diary group
//	{
//	    "date":"2009-11-03 10:18:33",
//	    "time":1257272467077,
//	    "timezone":"EST",
//	    "location": {
//	        "latitude":38.8977,
//	        "longitude":-77.0366
//	    },
//	    "version_id":1,
//	    "group_id":3,
//	    "tags": [],
//	    "responses":[
//	        {"prompt_id":1,
//	         "response":0},
//	        {"prompt_id":2,
//	         "response":0},
//	        {"prompt_id":3,
//	         "response":0},
//	        {"prompt_id":4,
//	         "response":0},
//	        {"prompt_id":5,
//	         "response":0},
//	        {"prompt_id":6,
//	         "response":0},
//	        {"prompt_id":7,
//	         "response":0},
//	        {"prompt_id":8,
//	         "response":0},
//	        {"prompt_id":9,
//	         "response":0},
//	        {"prompt_id":10,
//	         "response":0},
//	        {"prompt_id":11,
//	         "response":0},
//	        {"prompt_id":12,
//	         "response":0},
//	        {"prompt_id":13,
//	         "response":["t","t","t","t","t","t"]},
//	        {"prompt_id":14,
//	         "response":0},
//	        {"prompt_id":15,
//	         "response":0}
//	        {"prompt_id":16,
//	         "response":0},
//	        {"prompt_id":17,
//	         "response":0},
//	        {"prompt_id":18,
//	         "response":0},
//	        {"prompt_id":19,
//	         "response":0},
//	        {"prompt_id":20,
//	         "response":0},
//	        {"prompt_id":21,
//	         "response":0}
//	     ]
//	}
	
	public JSONArray createMessage(int numberOfEntries) {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		int versionId = 1;
		int groupId = 1;
		List<String> tags = new ArrayList<String>();
	
		for(int i = 0; i < numberOfEntries; i++) {
			String date = ValueCreator.date();
			long epoch = ValueCreator.epoch();
			double latitude = ValueCreator.latitude();
			double longitude = ValueCreator.longitude();
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("date", date);
			map.put("time", epoch);
			map.put("timezone", tz);
			map.put("version_id", versionId);
			map.put("group_id", groupId);
			map.put("tags", tags); // always empty for now
			
			Map<String, Object> location = new HashMap<String, Object>();
			location.put("latitude", latitude);
			location.put("longitude", longitude);
			map.put("location", location);
			
			
			List<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();
			
			// p0 is simply a "parent" question
			
			for(int j = 1; j < 4; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomBoolean() ? "1" : "0");
				responses.add(p);
			}
			
			for(int j = 4; j < 9; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomPositiveIntModulus(5));
				responses.add(p);
			}
			
			for(int j = 9; j < 12; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomBoolean() ? "1" : "0");
				responses.add(p);
			}
			
			Map<String, Object> p12 = new HashMap<String, Object>();
			p12.put("prompt_id", 12);
			p12.put("response", ValueCreator.randomPositiveIntModulus(7));
			responses.add(p12);
			
			List<String> booleans = new ArrayList<String>();
			for(int j = 0; j < 6; j++) {
				booleans.add(ValueCreator.randomBoolean() ? "t" : "f");
			}
			Map<String, Object> p13 = new HashMap<String, Object>();
			p13.put("prompt_id", 13);
			p13.put("response", booleans);
			responses.add(p13);
			
			for(int j = 14; j < 17; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomPositiveIntModulus(10));
				responses.add(p);
			}
			
			for(int j = 17; j < 19; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomBoolean() ? "1" : "0");
				responses.add(p);
			}
			
			Map<String, Object> p19 = new HashMap<String, Object>();
			p19.put("prompt_id", 19);
			p19.put("response", ValueCreator.randomPositiveIntModulus(7));
			responses.add(p19);
			
			Map<String, Object> p20 = new HashMap<String, Object>();
			p20.put("prompt_id", 20);
			p20.put("response", ValueCreator.randomBoolean() ? "1" : "0");
			responses.add(p20);
			
			Map<String, Object> p21 = new HashMap<String, Object>();
			p21.put("prompt_id", 21);
			p21.put("response", ValueCreator.randomPositiveIntModulus(7));
			responses.add(p21);
			
			map.put("responses", responses);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
