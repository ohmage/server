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
package org.ohmage.domain.survey.read;

/**
 * @author selsky
 */
public class PromptTypeUtils {
	
	private PromptTypeUtils() { }
	
	public static boolean isSingleChoiceType(String type) {
		return "single_choice".equals(type);
	}
	
	public static boolean isMultiChoiceType(String type) {
		return "multi_choice".equals(type);
	}
	
	public static boolean isRemoteActivityType(String type) {
		return "remote_activity".equals(type);
	}
	
	public static boolean isJsonArray(String type) {
		return "multi_choice".equals(type);
	}
	
	public static boolean isJsonObject(String type) {
		return "multi_choice_custom".equals(type)
		    || "single_choice_custom".equals(type);
	}
	
	public static boolean isNumberPromptType(String type) {
		return "hours_before_now".equals(type)
		    || "number".equals(type);
	}
	
}
