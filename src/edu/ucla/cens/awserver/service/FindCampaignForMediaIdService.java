package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

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
