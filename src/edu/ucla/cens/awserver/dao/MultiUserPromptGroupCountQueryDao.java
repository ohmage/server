package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.PromptGroupCountQueryResult;
import edu.ucla.cens.awserver.domain.SimpleUser;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for finding the number of prompt groups uploaded for each user in a campaign.
 * 
 * @author selsky
 */
public class MultiUserPromptGroupCountQueryDao extends SingleUserPromptGroupCountQueryDao {
	private static Logger _logger = Logger.getLogger(MultiUserPromptGroupCountQueryDao.class);
	private Dao _findAllUsersForCampaignDao;

	/**
	 * 
	 * @param dataSource
	 * @param findAllUsersForCampaignDao
	 */
	public MultiUserPromptGroupCountQueryDao(DataSource dataSource, Dao findAllUsersForCampaignDao) {
		super(dataSource);
		
		if(null == findAllUsersForCampaignDao) {
			throw new IllegalArgumentException("a non-null Dao is required");
		}
		
		_findAllUsersForCampaignDao = findAllUsersForCampaignDao;
	}
	
	/**
	 * Finds all of the users in the current campaign for the User in the provided AwRequest and dispatches to
	 * executeSqlForUser() for each of those users.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		
		_findAllUsersForCampaignDao.execute(awRequest);
		
		List<?> userList = awRequest.getResultList();
		List<PromptGroupCountQueryResult> totalResults = new ArrayList<PromptGroupCountQueryResult>();
		User loggedInUser = awRequest.getUser();
		
		int size = userList.size();
		
 		if(_logger.isDebugEnabled()) {
 			_logger.debug("about to run queries for " + size + " users");
 		}
		
		for(int i = 0; i < size; i++) {
			
			SimpleUser su = (SimpleUser) userList.get(i);
			
			List<PromptGroupCountQueryResult> userResults 
				= executeSqlForUser(Integer.parseInt(loggedInUser.getCurrentCampaignId()), 
						            su.getId(),
						            su.getUserName(),
						            awRequest.getStartDate(),
						            awRequest.getEndDate());
			
			totalResults.addAll(userResults);
		}
		
		awRequest.setResultList(totalResults);
	}
}
