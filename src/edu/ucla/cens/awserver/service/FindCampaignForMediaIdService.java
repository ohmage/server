package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
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
			
			_noMediaAnnotator.annotate(awRequest, "no response found for media id");
			
		} else {
			
			for(int i = 0; i < results.size(); i++) {
				CampaignNameVersion cnv = (CampaignNameVersion) results.get(i);
				if(cnv.getCampaignName().equals(req.getCampaignName()) && cnv.getCampaignVersion().equals(req.getCampaignVersion())) {
					return;
				}
			}
			
			_invalidCampaignAnnotator.annotate(awRequest, "invalid campaign name-version in query");
		}	
	}
}
