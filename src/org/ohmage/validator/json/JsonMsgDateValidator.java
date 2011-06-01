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
package org.ohmage.validator.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates the date element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgDateValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "date";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgDateValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the value returned from the AwRequest for the key "date" exists and is of the form yyyy-MM-dd hh:mm:ss.
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		String date = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == date) {
			getAnnotator().annotate(awRequest, "date in message is null");
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setLenient(false); // enforce valid dates 
		
		try {
		
			sdf.parse(date);
			
		} catch (ParseException pe) {
			
			getAnnotator().annotate(awRequest, "unparseable date. " + pe.getMessage() + " date value: " + date);
			return false;
			
		}
		
		return true;
		
	}
}
