package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * Simulator of phone/sensor messages that correspond to the prompt type and the prompt group id three ("diary" group). 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * and the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts">Prompt Spec</a> on the wiki for
 * details.
 * 
 * @author selsky
 */
public class PromptGroupThreeJsonMessageCreator implements JsonMessageCreator {

	/**
	 * Returns a JSONArray with numberOfEntries elements that are all of the prompt group id 3 type.
	 */
	public JSONArray createMessage(int numberOfDays) {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		int versionId = 1;
		int groupId = 3;
		List<String> tags = new ArrayList<String>();
	
		// Create data starting numberOfDays - 1 days ago
		for(int i = numberOfDays - 1 ; i >= 0; i--) {
			try { Thread.sleep(100); } catch (InterruptedException ie) { } // ensure variable dates
			String date = ValueCreator.date(i);
			long epoch = ValueCreator.epoch(i);
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
			p0.put("response", ValueCreator.randomPositiveIntModulus(3));
			responses.add(p0);
			
			// p1 is simply a "parent" question"
			
			for(int j = 2; j < 6; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomBoolean() ? 1 : 0);
				responses.add(p);
			}
						
			Map<String, Object> p6 = new HashMap<String, Object>();
			p6.put("prompt_id", 6);
			p6.put("response", ValueCreator.randomPositiveIntModulus(3));
			responses.add(p6);
			
			Map<String, Object> p7 = new HashMap<String, Object>();
			p7.put("prompt_id", 7);
			p7.put("response", ValueCreator.randomPositiveIntModulus(6));
			responses.add(p7);
			
			Map<String, Object> p8 = new HashMap<String, Object>();
			p8.put("prompt_id", 8);
			p8.put("response", ValueCreator.randomPositiveIntModulus(3));
			responses.add(p8);
			
			List<String> booleans = new ArrayList<String>();
			for(int j = 0; j < 6; j++) {
				booleans.add(ValueCreator.randomBoolean() ? "t" : "f");
			}
			Map<String, Object> p9 = new HashMap<String, Object>();
			p9.put("prompt_id", 9);
			p9.put("response", booleans);
			responses.add(p9);
			
			for(int j = 10; j < 12; j++) {
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("prompt_id", j);
				p.put("response", ValueCreator.randomPositiveIntModulus(10));
				responses.add(p);
			}
			
			Map<String, Object> p12 = new HashMap<String, Object>();
			p12.put("prompt_id", 12);
			p12.put("response", ValueCreator.randomBoolean() ? 1 : 0);
			responses.add(p12);
			
			map.put("responses", responses);
			
			jsonArray.put(map);
		}
		
		return jsonArray;
	}

}
