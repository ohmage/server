package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * @author selsky
 */
public class MobilityQueryUserCheckService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(MobilityQueryUserCheckService.class);
	private Dao _loggedInUserCampaignDao;
	private Dao _queryUserCampaignDao;
	
	public MobilityQueryUserCheckService(AwRequestAnnotator annotator, Dao loggedInUserCampaignDao, Dao queryUserCampaignDao) {
		super(annotator);
		if(null == loggedInUserCampaignDao) {
			throw new IllegalArgumentException("loggedInUserCampaignDao cannot be null");
		}
		if(null == queryUserCampaignDao) {
			throw new IllegalArgumentException("queryUserCampaignDao cannot be null");
		}
		_loggedInUserCampaignDao = loggedInUserCampaignDao;
		_queryUserCampaignDao = queryUserCampaignDao;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// find all of the campaigns the logged-in user belongs to
		_loggedInUserCampaignDao.execute(awRequest);
		@SuppressWarnings("unchecked")
		List<String> queryResults = (List<String>) awRequest.getResultList();
		List<String> loggedInUserCampaigns = new ArrayList<String>(queryResults);
		
		// find all of the campaigns the user in the query parameters belongs to
		_queryUserCampaignDao.execute(awRequest);
		
		for(String campaignName : loggedInUserCampaigns) {
			if(awRequest.getResultList().contains(campaignName)) {
				return;
			}
		}
		
		_logger.warn("logged-in user and user specified in the query do not share membership in any campaigns. logged-in user: "
			+ awRequest.getUser().getUserName() + " query user: " + awRequest.getUserNameRequestParam()
		);
		getAnnotator().annotate(awRequest, "user attempt to query a campaign they do not belong to");
	}
}
