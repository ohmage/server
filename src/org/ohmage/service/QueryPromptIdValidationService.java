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
package org.ohmage.service;

import java.util.List;

import org.ohmage.domain.Configuration;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointQueryAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validator for a sanity check of the "i" parameter in a data point API query.
 * 
 * @author selsky
 */
public class QueryPromptIdValidationService extends AbstractAnnotatingService {
	// private static Logger _logger = Logger.getLogger(QueryPromptIdValidationService.class);
	
	public QueryPromptIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks the data point ids from the awRequest to make sure the client has not requested a promptId with the displayType
	 * of metadata (which violates the our API constraints because metadata can only be "attached to" non-metadata displayTypes).
	 * Also checks that the data point ids exist for the Configuration represented by the campaign name and version in the query.
	 * If this method annotates the request (annotator.annotate()), it means there is a logical error in the client attempting to
	 * run queries.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		DataPointQueryAwRequest req = (DataPointQueryAwRequest) awRequest;
		
		String[] promptIds = req.getDataPointIds(); 
		Configuration config = req.getConfiguration();
		
		for(String promptId : promptIds) {
			List<String> configMetadataPromptIds = config.getMetadataPromptIds(promptId);
			
			if(configMetadataPromptIds.contains(promptId)) {
				getAnnotator().annotate(req, "promptId " +  promptId + " is a metadata promptId and disallowed");
				return;
			}	
		}
	}
}
