package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service that verifies that the each user in the user list ("new" data point API) belongs to the campaign in the query. 
 * 
 * @author selsky
 */
public class CampaignUserCheckService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignUserCheckService.class);
	
	/**
	 * The provided DAO must push a list of campaign names into AwRequest.setResultList. 
	 */
	public CampaignUserCheckService(Dao dao, AwRequestAnnotator annotator) {
		super(dao, annotator);
	}
	
	/**
	 * Verifies that the query campaign name is present in each query user's campaign list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("validating that each user in the user list belongs to the campaign specified in the query");
		
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		String userListString = req.getUserListString();
		
		if(! "urn:sys:special:all".equals(userListString)) {
			
			List<String> users = req.getUserList();
			
			for(String user : users) {
				
				req.setCurrentUser(user);
				
				getDao().execute(awRequest);
				
				List<?> results = awRequest.getResultList();
				
				if(! results.contains(awRequest.getCampaignName())) {
					_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
					getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
				}
			}
		}
	}
}
