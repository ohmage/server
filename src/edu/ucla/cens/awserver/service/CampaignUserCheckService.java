package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service that verifies that the campaign name in the AwRequest represents a campaign that the logged-in user has access to.
 * 
 * @author selsky
 */
public class CampaignUserCheckService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignUserCheckService.class);
	
	public CampaignUserCheckService(Dao dao, AwRequestAnnotator annotator) {
		super(dao, annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		
		 // make sure the user query param represents a user that belongs to the campaign query param
			
		getDao().execute(awRequest);
		
		List<?> results = awRequest.getResultList();
		if(! results.contains(awRequest.getCampaignName())) {
			_logger.warn("logged in user attempting to query against a user who does not belong to the same campaign. " 
				+ " logged in user: " +  awRequest.getUser().getUserName() + " query user: " 
				+ awRequest.getUserNameRequestParam() + " query campaign: " + awRequest.getCampaignName());
			getAnnotator().annotate(awRequest, "logged in user and query user do not belong to the same campaigns");
		}
	}
}
