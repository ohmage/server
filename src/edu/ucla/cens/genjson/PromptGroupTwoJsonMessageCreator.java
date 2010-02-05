package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * Simulator of phone/sensor messages that correspond to the prompt type and the prompt group id 2 ("emotional state" group). 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * and the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts">Prompt Spec</a> on the wiki for
 * details.
 * 
 * @author selsky
 */
public class PromptGroupTwoJsonMessageCreator implements JsonMessageCreator {

//	# Emotional State group
//	{
//	    "date":"2009-11-03 10:18:33",
//	    "time":1257272467077,
//	    "timezone":"EST",
//	    "location": {
//	        "latitude":38.8977,
//	        "longitude":-77.0366
//	    },
//	    "version_id":1,
//	    "group_id":2,
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
//	         "response":0},
//	        {"prompt_id":14,
//	         "response":0}
//	     ]
//	}
	
	/**
	 * Returns a JSONArray with numberOfEntries elements that are all of the prompt group id 2 type.
	 */
	public JSONArray createMessage(int numberOfEntries) {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		int versionId = 1;
		int groupId = 2;
		List<String> tags = new ArrayList<String>();
	
		for(int i = 0; i < numberOfEntries; i++) {
			try { Thread.sleep(100); } catch (InterruptedException ie) { } // ensure variable dates
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
			
			for(int j = 1; j < 15; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomPositiveIntModulus(7));
				responses.add(p);
			}
			
			map.put("responses", responses);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
