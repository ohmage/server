package edu.ucla.cens.awserver.service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
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
		
		try {
		
			getDao().execute(awRequest);
		
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
		}
		
		List<?> results = awRequest.getResultList();
		
		if(0 == results.size()) {
			
			_noMediaAnnotator.annotate(awRequest, "no media url found for id");
			
		} else if (1 == results.size()){
			
			String url = (String) results.get(0);
			if(_logger.isDebugEnabled()) {
				_logger.info("local media URL: " + url);
			}
			
			File f = null;
			try {
				
				f = new File(new URI(url));
				
			} catch(URISyntaxException use) { // bad! this means there are malformed file:/// URLs in the db
				                              // If HTTP URLs are added in the future, the URL class must be used instead of File
				
				throw new ServiceException(use);
			}
			
			
			if(! f.exists()) {
				// this can happen because the image upload and the survey upload are decoupled and the image
				// upload could simply fail because of client issues
				_logger.warn("media file reference in url_based_resource table without a corresponding file on the filesystem");
				_noMediaAnnotator.annotate(awRequest, "no media file found for url");
			} else {
				((MediaQueryAwRequest) awRequest).setMediaUrl(url);
			}
			
			
		} else { // bad! the media id is supposed to be a unique key
			
			_logger.error("more than one url found for media id " + ((MediaQueryAwRequest) awRequest).getMediaId());
			_severeAnnotator.annotate(awRequest, "more than one url found for media id");
			
		}
	}
}
