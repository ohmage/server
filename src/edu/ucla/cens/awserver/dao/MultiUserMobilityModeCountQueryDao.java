package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.MobilityModeCountQueryResult;
import edu.ucla.cens.awserver.domain.SimpleUser;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for finding the aggregate number of mobility modes uploaded per day for each user in a campaign.
 * 
 * @author selsky
 */
public class MultiUserMobilityModeCountQueryDao extends SingleUserMobilityModeCountQueryDao {
	private static Logger _logger = Logger.getLogger(MultiUserMobilityModeCountQueryDao.class);
	private Dao _findAllUsersForCampaignDao;

	/**
	 * 
	 * @param dataSource
	 * @param findAllUsersForCampaignDao
	 */
	public MultiUserMobilityModeCountQueryDao(DataSource dataSource, Dao findAllUsersForCampaignDao) {
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
		List<MobilityModeCountQueryResult> totalResults = new ArrayList<MobilityModeCountQueryResult>();
		
		int size = userList.size();
		
 		if(_logger.isDebugEnabled()) {
 			_logger.debug("about to run queries for " + size + " users");
 		}
		
		for(int i = 0; i < size; i++) {
			
			SimpleUser su = (SimpleUser) userList.get(i);
			
			List<MobilityModeCountQueryResult> userResults 
				= executeSqlForUser(su.getId(),
						            su.getUserName(),
						            awRequest.getStartDate(),
						            awRequest.getEndDate());
			
			totalResults.addAll(userResults);
		}
		
		awRequest.setResultList(totalResults);
	}
}
