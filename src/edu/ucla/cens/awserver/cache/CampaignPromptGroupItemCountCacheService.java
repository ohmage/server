package edu.ucla.cens.awserver.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.ParameterLessDao;
import edu.ucla.cens.awserver.domain.CampaignPromptGroup;
import edu.ucla.cens.awserver.domain.CampaignPromptGroupItemCount;

/**
 * Unsynchronized (i.e., read only) cache of campaign_id-group_id combinations and the number of prompts located at each 
 * campaign_id-group_id. 
 * 
 * @author selsky
 */
public class CampaignPromptGroupItemCountCacheService extends AbstractCacheService {
	private Map<CampaignPromptGroup, Integer> _cache;
	private static Logger _logger = Logger.getLogger(CampaignPromptGroupItemCountCacheService.class);
	
	public CampaignPromptGroupItemCountCacheService(ParameterLessDao dao) {
		super(dao);
		init();
	}

	private void init() {
		_cache = new HashMap<CampaignPromptGroup, Integer>();
		
		List<?> list = _dao.execute();
		int size = list.size();
		
		if(list.size() < 1) {
			throw new IllegalStateException("incorrect database state -- less than one item found for the count of prompts per " +
			    "group per campaign");
		}
		
		for(int i = 0; i < size; i++) {
			CampaignPromptGroupItemCount cpgic = (CampaignPromptGroupItemCount) list.get(i);
			_cache.put(cpgic.getCampaignPromtGroup(), cpgic.getCount());
		}
		
		_logger.info("loaded " + size + " campaign_id-group_id prompt counts");
	}
	
	@Override
	public Object lookup(Object key) {
		return _cache.get(key);
	}
}
