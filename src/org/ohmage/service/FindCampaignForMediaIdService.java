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

import org.ohmage.dao.Dao;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MediaQueryAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validation service for checking whether the campaign URN provided in the request represents the campaign the media id belongs
 * to.
 * 
 * @author Joshua Selsky
 */
public class FindCampaignForMediaIdService extends AbstractDaoService {
	private AwRequestAnnotator _noMediaAnnotator;
	private AwRequestAnnotator _invalidCampaignAnnotator;
	
	public FindCampaignForMediaIdService(Dao dao, AwRequestAnnotator noMediaAnnotator, AwRequestAnnotator invalidCampaignAnnotator) {
		super(dao);
		
		if(null == noMediaAnnotator) {
			throw new IllegalArgumentException("noMediaAnnotator cannot be null");
		}
		if(null == invalidCampaignAnnotator) {
			throw new IllegalArgumentException("invalidCampaignAnnotator cannot be null");
		}
		
		_noMediaAnnotator = noMediaAnnotator;
		_invalidCampaignAnnotator = invalidCampaignAnnotator;
	}
	
	public void execute(AwRequest awRequest) {
		MediaQueryAwRequest req = (MediaQueryAwRequest) awRequest;
		getDao().execute(awRequest);
		List<?> results = awRequest.getResultList();
		if(0 == results.size()) {
			
			_noMediaAnnotator.annotate(awRequest, "no response found for media id or campaign URN doesn't exist");
			
		} else {
			
			if(results.size() > 1) { // logical error in the db on campaign URN uniqueness
				throw new ServiceException("found more than one campaign for URN " + req.getCampaignUrn());
			} 
			
			if(! req.getCampaignUrn().equals(((String) results.get(0)))) {
				_invalidCampaignAnnotator.annotate(awRequest, "invalid campaign urn in query");
			}
		}	
	}
}
