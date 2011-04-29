package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;
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
		
		// FIXME: Hackalicious!
		String userListString;
		List<String> users;
		if(awRequest instanceof SurveyResponseReadAwRequest) {
			SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
			userListString = req.getUserListString();
			users = req.getUserList();
			
			if(! "urn:ohmage:special:all".equals(userListString)) {
				
				for(String user : users) {
					
					req.setCurrentUser(user);
					
					getDao().execute(awRequest);
					
					List<?> results = awRequest.getResultList();
					
					if(! results.contains(awRequest.getCampaignUrn())) {
						_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
						getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
					}
				}
			}
		}
		else if(awRequest instanceof UserStatsQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else if(awRequest instanceof DataPointFunctionQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else if(awRequest instanceof MediaQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else {
			awRequest.setFailedRequest(true);
			throw new ServiceException("Invalid request for CampaignUserCheckService.");
		}
	}
}
