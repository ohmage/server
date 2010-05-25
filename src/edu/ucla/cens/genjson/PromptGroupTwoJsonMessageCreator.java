package edu.ucla.cens.genjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * Simulator of phone/sensor messages that correspond to the prompt type and the prompt group id 2 ("emotional state" group). 
 * 
 * See the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">JSON Protocol documentation</a>
 * and the <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts">Prompt Spec</a> on the wiki for
 * details.
 * 
 * @author selsky
 */
public class PromptGroupTwoJsonMessageCreator implements JsonMessageCreator {
	
	/**
	 * Returns a JSONArray with numberOfEntries elements that are all of the prompt group id 2 type.
	 */
	public JSONArray createMessage(int numberOfDays) {
		JSONArray jsonArray = new JSONArray();
		String tz = ValueCreator.tz(); // use the same tz for all messages in the returned array (the most likely use case)
		int versionId = 1;
		int groupId = 2;
		List<String> tags = new ArrayList<String>();
	
		// Create data starting numberOfDays - 1 days ago
		for(int i = numberOfDays - 1 ; i >= 0; i--) {
			// Two responses for this group a day
			for (int j = 0; j < 2; j++) {
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
				
				// p0 is simply a "parent" question
				
				for(int k = 1; k < 11; k++) {
					Map<String, Object> p = new HashMap<String, Object>();
					p.put("prompt_id", k);
					p.put("response", ValueCreator.randomPositiveIntModulus(4));
					responses.add(p);
				}
				
				map.put("responses", responses);
				
				jsonArray.put(map);
			}
		}
		
		return jsonArray;
	}

}
