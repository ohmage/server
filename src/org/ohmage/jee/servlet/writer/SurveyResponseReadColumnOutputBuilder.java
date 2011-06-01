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
package org.ohmage.jee.servlet.writer;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.ohmage.domain.PromptContext;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * @author selsky
 */
public interface SurveyResponseReadColumnOutputBuilder {
	
	String createMultiResultOutput(int totalNumberOfResults, SurveyResponseReadAwRequest req, 
			                       Map<String, PromptContext> promptContextMap, Map<String, List<Object>> columnMap)
								   throws JSONException;
	
	String createZeroResultOutput(SurveyResponseReadAwRequest req, Map<String, List<Object>> columnMap) throws JSONException;

}
