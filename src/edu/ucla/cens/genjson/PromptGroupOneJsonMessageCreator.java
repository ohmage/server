package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * Simulator of phone/sensor messages that correspond to the prompt type and the prompt group id 1 ("sleep" group). 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * and the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts">Prompt Spec</a> on the wiki for
 * details.
 * 
 * @author selsky
 */
public class PromptGroupOneJsonMessageCreator implements JsonMessageCreator {

//	# Sleep group
//	{
//	    "date":"2009-11-03 10:18:33",
//	    "time":1257272467077,
//	    "timezone":"EST",
//	    "location": {
//	        "latitude":38.8977,
//	        "longitude":-77.0366
//	    },
//	    "version_id":1,
//	    "group_id":1,
//	    "tags": [],
//	    "responses":[
//	        {"prompt_id":0,
//	         "response":"12:00"},
//	        {"prompt_id":1,
//	         "response":0},
//	        {"prompt_id":2,
//	         "response":"07:00"},
//	        {"prompt_id":3,
//	         "response":"0"},
//	        {"prompt_id":4,
//	         "response":"0"}
//	     ]
//	}
	
	/**
	 * Returns a JSONArray with numberOfEntries elements that are all of the prompt group id 1 type.
	 */
	public JSONArray createMessage(int numberOfEntries) {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		int versionId = 1;
		int groupId = 1;
		List<String> tags = new ArrayList<String>();
	
		for(int i = 0; i < numberOfEntries; i++) {
			try { Thread.sleep(100); } catch (InterruptedException ie) { } // ensure variable dates
			String date = ValueCreator.date(i,1);
			long epoch = ValueCreator.epoch(i,1);
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
			Map<String, Object> p0 = new HashMap<String, Object>();
			p0.put("prompt_id", 0);
			p0.put("response", ValueCreator.randomTime(23, 4));
			responses.add(p0);
			
			Map<String, Object> p1 = new HashMap<String, Object>();
			p1.put("prompt_id", 1);
			p1.put("response", ValueCreator.randomPositiveIntModulus(4));
			responses.add(p1);
			
			Map<String, Object> p2 = new HashMap<String, Object>();
			p2.put("prompt_id", 2);
			p2.put("response", ValueCreator.randomTime(7, 4));
			responses.add(p2);
			
			Map<String, Object> p3 = new HashMap<String, Object>();
			p3.put("prompt_id", 3);
			p3.put("response", ValueCreator.randomPositiveIntModulus(12));
			responses.add(p3);
			
			Map<String, Object> p4 = new HashMap<String, Object>();
			p4.put("prompt_id", 4);
			p4.put("response", ValueCreator.randomPositiveIntModulus(3));
			responses.add(p4);
			
			map.put("responses", responses);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
