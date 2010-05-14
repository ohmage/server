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
public class MultiUserSuccessfulLocationUpdatesQueryDao extends SuccessfulLocationUpdatesQueryDao {
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
	 * 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_findAllUsersForCampaignDao.execute(awRequest);
		
		List<?> userList = awRequest.getResultList();
		List<UserPercentage> percentList = new ArrayList<UserPercentage>();
		
		int size = userList.size();
		
		_logger.info(size);
		
		for(int i = 0; i < size; i++) {
			
			SimpleUser su = (SimpleUser) userList.get(i);
			executeSqlForUser(su.getId(), su.getUserName(), percentList);
			
		}
		
		awRequest.setResultList(percentList);
	}
}
