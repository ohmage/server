package edu.ucla.cens.awserver.validator.survey;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AbstractAnnotatingValidator;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for checking the campaign name and version from a survey upload request against campaign name-versions from a local
 * cache. 
 *
 * @author selsky
 */
public class CampaignUrnValidator extends AbstractAnnotatingValidator {
	private CacheService _cacheService;
	
	public CampaignUrnValidator(AwRequestAnnotator annotator, CacheService cacheService) {
		super(annotator);
		if(null == cacheService) {
			throw new IllegalArgumentException("a CacheService is required");
		}
		_cacheService = cacheService;
	}
		
	@Override
	public boolean validate(AwRequest awRequest) {
		if(! _cacheService.containsKey(awRequest.getCampaignUrn())) {
			getAnnotator().annotate(awRequest, "campaign URN does not exist: " + awRequest.getCampaignUrn()); 
			return false;
		}		
		return true;
	}
}
