package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.SimpleUser;
import edu.ucla.cens.awserver.domain.UserPercentage;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class MultiUserSuccessfulLocationUpdatesQueryDao extends SingleUserSuccessfulLocationUpdatesQueryDao {
	private static Logger _logger = Logger.getLogger(MultiUserSuccessfulLocationUpdatesQueryDao.class);
	private Dao _findAllUsersForCampaignDao;
	
	public MultiUserSuccessfulLocationUpdatesQueryDao(DataSource dataSource, Dao findAllUsersForCampaignDao) {
		super(dataSource);
		
		if(null == findAllUsersForCampaignDao) {
			throw new IllegalArgumentException("a non-null Dao is required");
		}
		
		_findAllUsersForCampaignDao = findAllUsersForCampaignDao;
	}
	
	/**
	 * Finds all users for the current campaign, calculates the percentage of successful location updates for each user, and 
	 * places the resulting UserPercentages into the AwRequest.resultList.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_findAllUsersForCampaignDao.execute(awRequest);
		
		List<?> userList = awRequest.getResultList();
		List<UserPercentage> userPercentages = new ArrayList<UserPercentage>();
		
		int size = userList.size();
		
 		if(_logger.isDebugEnabled()) {
 			_logger.debug("about to run queries for " + size + " users");
 		}
		
		for(int i = 0; i < size; i++) {
			
			SimpleUser su = (SimpleUser) userList.get(i);
			UserPercentage userPercentage 
				= executeSqlForUser(Integer.parseInt(awRequest.getUser().getCurrentCampaignId()), su.getId(), su.getUserName());
			userPercentages.add(userPercentage);
		}
		
		awRequest.setResultList(userPercentages);
	}
}
