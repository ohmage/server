package edu.ucla.cens.awserver.service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.SurveyResponsePrivacyStateCache;
import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.UrlPrivacyState;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * 
 * 
 * @author Joshua Selsky
 * @see SurveyResponsePrivacyFilterService
 */
public class FindUrlForMediaIdService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(FindUrlForMediaIdService.class);
	private AwRequestAnnotator _noMediaAnnotator;
	private AwRequestAnnotator _severeAnnotator;
	
	public FindUrlForMediaIdService(Dao dao, AwRequestAnnotator noMediaAnnotator, AwRequestAnnotator severeAnnotator) {
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
				
				if(user.isParticipantInCampaign(campaignUrn) 
					&& user.getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1) {	
					
					if(urlPrivacyState.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_INVISIBLE)) { 
						_logger.info("Removing an " + SurveyResponsePrivacyStateCache.PRIVACY_STATE_INVISIBLE + "image url from the query results");
						results.remove(0);
						return;
					}
					
				} else if (user.isOnlyAnalystOrAuthor(campaignUrn)){
					
					if(urlPrivacyState.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_PRIVATE)) {
						_logger.info("Removing an " + SurveyResponsePrivacyStateCache.PRIVACY_STATE_PRIVATE + "image url from the query results");
						results.remove(0);
						return;
					}
				}
			}
			
			// Now check out the file
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("Local media URL: " + urlPrivacyState.getUrl());
			}
			
			File f = null;
			
			try {
				
				f = new File(new URI(urlPrivacyState.getUrl()));
				
			} catch(URISyntaxException use) { // bad! this means there are malformed file:/// URLs in the db
				                              // If HTTP URLs are added in the future, the URL class must be used instead of File
				
				throw new ServiceException(use);
			}
			
			
			if(! f.exists()) {
				// this can happen because the image upload and the survey upload are decoupled and the image
				// upload could simply fail because of client issues
				_logger.warn("Media file reference in url_based_resource table without a corresponding file on the filesystem");
				_noMediaAnnotator.annotate(awRequest, "No media file found for id (no file on filesystem)");
				
			} else {
				
				((MediaQueryAwRequest) awRequest).setMediaUrl(urlPrivacyState.getUrl());
			}
			
			
		} else { // bad! the media id is supposed to be a unique key
			
			_logger.error("More than one url found for media id " + ((MediaQueryAwRequest) awRequest).getMediaId());
			_severeAnnotator.annotate(awRequest, "More than one url found for media id " + ((MediaQueryAwRequest) awRequest).getMediaId());
			
		}
	}
}
