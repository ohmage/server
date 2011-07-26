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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.UrlPrivacyState;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MediaQueryAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service for finding an image filesystem URL given a unique id (a UUID).  
 * 
 * @author Joshua Selsky
 */
public class FindUrlForImageIdService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(FindUrlForImageIdService.class);
	private AwRequestAnnotator _noMediaAnnotator;
	private AwRequestAnnotator _severeAnnotator;
	
	/**
	 * @param dao the DAO to use when looking up the file URL
	 * @param noMediaAnnotator - the annotator to use when no media is found for the provided id
	 * @param severeAnnotator - the annoatator to use when IO or database exceptions occur
	 */
	public FindUrlForImageIdService(Dao dao, AwRequestAnnotator noMediaAnnotator, AwRequestAnnotator severeAnnotator) {
		super(dao);
		
		if(null == noMediaAnnotator) {
			throw new IllegalArgumentException("noMediaAnnotator cannot be null");
		}
		if(null == severeAnnotator) {
			throw new IllegalArgumentException("severeAnnotator cannot be null");
		}
		
		_noMediaAnnotator = noMediaAnnotator;
		_severeAnnotator = severeAnnotator;
	}
	
	/**
	 * Dispatches to the DAO provided on construction and expects that DAO to set a single-item result list containing a file URL
	 * for an image. If the size parameter is found in the AwRequest, a thumbnail version of the image URL is set in the request;
	 * otherwise the URL to the full-size image is set on the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Dispatching to a DAO to lookup a media id URL and then filtering by our ACL rules.");
		
		try {
		
			getDao().execute(awRequest);
		
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
		}
		
		List<?> results = awRequest.getResultList();
		
		if(0 == results.size()) {
			
			_noMediaAnnotator.annotate(awRequest, "No media url found for id");
			
		} else if (1 == results.size()){
			
			UrlPrivacyState urlPrivacyState = (UrlPrivacyState) results.get(0);
			
			// First check the privacy state before even dealing with the file
			
			User user = awRequest.getUser();
			String campaignUrn = awRequest.getCampaignUrn();
			
			if(! user.isSupervisorInCampaign(campaignUrn)) { // supervisors can read all data, all the time
				
				if(resultIsUnshared(urlPrivacyState) && ! urlPrivacyState.getUsername().equals((awRequest.getUser().getUserName()))) { 
					results.remove(0);
					return;
				}
			}
			
			// Now retrieve the file from the filesystem
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("Local media URL: " + urlPrivacyState.getUrl());
			}
			
			File f = null;
			String url = null;
			
			try {
				
				String size = null;
				Map<String, Object> toProcessMap = awRequest.getToProcess();
				
				if(toProcessMap.containsKey("size")) {
					size = (String) toProcessMap.get("size");
				}
				
				if(null == size) {
					f = new File(new URI(urlPrivacyState.getUrl()));
					url = urlPrivacyState.getUrl();
				}
				else if("small".equals(size)) {
					StringBuilder builder = new StringBuilder();
					url = urlPrivacyState.getUrl();
					int indexOfDot = url.lastIndexOf('.');
					builder.append(url.substring(0, indexOfDot)).append("-s").append(".jpg");			
					url = builder.toString();
					f = new File(new URI(url));
					if(_logger.isDebugEnabled()) {
						_logger.debug("Returning a thumbnail URL: " + url);
					}
				} 
				else {
					throw new IllegalStateException("invalid size parameter: " + size);
				}
				
			} catch(URISyntaxException use) { // bad! this means there are malformed file:/// URLs in the db
				                              // If HTTP URLs are added in the future, the URL class must be used instead of File
				
				throw new ServiceException(use);
			}
			
			
			if(! f.exists()) {
				// this can happen because the image upload and the survey upload are decoupled and the image
				// upload could simply fail because of client issues
				_logger.warn("Image file reference in url_based_resource table without a corresponding file on the filesystem");
				_noMediaAnnotator.annotate(awRequest, "No image file found for id (no file on filesystem)");
				
			} else {
				
				// hacky cast
				((MediaQueryAwRequest) awRequest).setMediaUrl(url);
			}
			
			
		} else { // bad! the media id is supposed to be a unique key
			
			_logger.error("More than one url found for image id " + ((MediaQueryAwRequest) awRequest).getMediaId());
			_severeAnnotator.annotate(awRequest, "More than one url found for image id " + ((MediaQueryAwRequest) awRequest).getMediaId());
			
		}
	}
	
	private boolean resultIsUnshared(UrlPrivacyState ups) {
		return ups.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_PRIVATE) 
			|| ups.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_INVISIBLE); 
	}
}
