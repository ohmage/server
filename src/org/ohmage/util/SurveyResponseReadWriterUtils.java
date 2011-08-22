/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.ohmage.util;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.configuration.PromptProperty;
import org.ohmage.domain.survey.read.SurveyResponseReadIndexedResult;

/**
 * A collection of static methods used by the output builders for /app/survey_response/read.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadWriterUtils {
	
	/**
	 * You cannot instantiate me.
	 */
	private SurveyResponseReadWriterUtils() { }
	
	public static JSONObject shortLaunchContext(String launchContext) throws JSONException {
		JSONObject lc = new JSONObject(launchContext);
		JSONObject shortLc = new JSONObject();
		
		String launchTime = JsonUtils.getStringFromJsonObject(lc, "launch_time");
		if(null != launchTime) {
			shortLc.put("launch_time", launchTime);
		}
		
		JSONArray activeTriggers = JsonUtils.getJsonArrayFromJsonObject(lc, "active_triggers");
		if(null != activeTriggers) {
			JSONArray shortArray = new JSONArray();
			for(int i = 0; i < activeTriggers.length(); i++) {
				JSONObject shortArrayEntry = new JSONObject();
				JSONObject longArrayEntry = JsonUtils.getJsonObjectFromJsonArray(activeTriggers, i);
				if(null != longArrayEntry) {
					String triggerType = JsonUtils.getStringFromJsonObject(longArrayEntry, "trigger_type");
					if(null != triggerType) {
						shortArrayEntry.put("trigger_type", triggerType);
					}
					JSONObject runtimeDescription = JsonUtils.getJsonObjectFromJsonObject(longArrayEntry, "runtime_description");
					if(null != runtimeDescription) {
						String triggerTime = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timestamp");
						if(null != triggerTime) {
							shortArrayEntry.put("trigger_timestamp", triggerTime);
						}
						String triggerTimezone = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timezone");
						if(null != triggerTimezone) {
							shortArrayEntry.put("trigger_timezone", triggerTimezone);
						}
					}
				}
				shortArray.put(shortArrayEntry);
			}
		}
		return shortLc;
	}
	
	public static String generateUtcTimestamp(SurveyResponseReadIndexedResult result) {
		return DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone());
	}	
	
	public static Object choiceGlossaryToJson(Map<String, PromptProperty> ppMap) throws JSONException {
		JSONObject main = new JSONObject();
		Iterator<String> it = ppMap.keySet().iterator();
		while(it.hasNext()) {
			PromptProperty pp = ppMap.get(it.next());
			JSONObject item = new JSONObject();
			item.put("value", pp.getValue());
			item.put("label", pp.getLabel());
			main.put(pp.getKey(), item);
		}
		return main;
	}
}
