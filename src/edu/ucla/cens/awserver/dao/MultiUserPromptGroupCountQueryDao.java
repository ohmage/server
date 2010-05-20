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
 * @author selsky
 */
public class MultiUserPromptGroupCountQueryDao extends SingleUserPromptGroupCountQueryDao {
	private static Logger _logger = Logger.getLogger(MultiUserPromptGroupCountQueryDao.class);
	
	private Dao _findAllUsersForCampaignDao;
	
//	private String _sql = "select login_id, campaign_prompt_group_id, date(time_stamp), count(*)" +
//			              " from prompt_response, prompt, user, campaign_prompt_group" +
//	                      " where prompt_response.prompt_id = prompt.id" +
//	                      " and campaign_id = ?" +
//	                      " and prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
//	                      " and time_stamp between ? and ?" +
//	                      " and user.id = user_id" +
//	                      " group by login_id, campaign_prompt_group_id, date(time_stamp)" +
//	                      " order by login_id, date(time_stamp), campaign_prompt_group_id";

	public MultiUserPromptGroupCountQueryDao(DataSource dataSource, Dao findAllUsersForCampaignDao) {
		super(dataSource);
		
		if(null == findAllUsersForCampaignDao) {
			throw new IllegalArgumentException("a non-null Dao is required");
		}
		
		_findAllUsersForCampaignDao = findAllUsersForCampaignDao;
	}
	
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
