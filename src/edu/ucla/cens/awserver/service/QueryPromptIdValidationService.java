package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.cache.ConfigurationCacheService;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for a sanity check of the "i" parameter in a data point API query.
 * 
 * @author selsky
 */
public class QueryPromptIdValidationService extends AbstractAnnotatingService {
	// private static Logger _logger = Logger.getLogger(QueryPromptIdValidationService.class);
	private ConfigurationCacheService _configurationCacheService;
	
	public QueryPromptIdValidationService(AwRequestAnnotator annotator, ConfigurationCacheService configurationCacheService) {
		super(annotator);
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a ConfigurationCacheService is required");
		}
		_configurationCacheService = configurationCacheService;
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
		String campaignName = req.getCampaignName();
		String campaignVersion = req.getCampaignVersion();
		
		Configuration config = (Configuration) _configurationCacheService.lookup(new CampaignNameVersion(campaignName, campaignVersion));
		
		for(String promptId : promptIds) {
			List<String> configMetadataPromptIds = config.getMetadataPromptIds(promptId);
			
			if(configMetadataPromptIds.contains(promptId)) {
				getAnnotator().annotate(req, "promptId " +  promptId + " is a metadata promptId and disallowed");
				return;
			}	
		}
	}
}
