package edu.ucla.cens.awserver.validator.survey;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AbstractAnnotatingValidator;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for checking the campaign name and version from a survey upload request against campaign name-versions from a local
 * cache. 
 *
 * @author selsky
 */
public class CampaignNameVersionValidator extends AbstractAnnotatingValidator {
	private CacheService _cacheService;
	
	public CampaignNameVersionValidator(AwRequestAnnotator annotator, CacheService cacheService) {
		super(annotator);
		if(null == cacheService) {
			throw new IllegalArgumentException("a CacheService is required");
		}
		_cacheService = cacheService;
	}
		
	@Override
	public boolean validate(AwRequest awRequest) {
		String campaignName = awRequest.getCampaignName();
		String campaignVersion = awRequest.getCampaignVersion();
		
		CampaignNameVersion campaignNameVersion = new CampaignNameVersion(campaignName, campaignVersion);
		if(! _cacheService.containsKey(campaignNameVersion)) {
			getAnnotator().annotate(awRequest, "campaign name-version is invalid. name=" + campaignName + " version=" + campaignVersion); 
			return false;
		}
				
		return true;
	}
}
