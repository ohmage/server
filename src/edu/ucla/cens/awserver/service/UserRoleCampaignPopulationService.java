package edu.ucla.cens.awserver.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.UserRoleCampaignResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

public class UserRoleCampaignPopulationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleCampaignPopulationService.class);
	
	public UserRoleCampaignPopulationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Adding campaign roles to user object.");
		
		try {
			
			getDao().execute(awRequest);
			
			if(awRequest.getResultList().size() == 0) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "User doesn't belong to campaigns.");
				return;
			}
			
			ListIterator<?> iter = awRequest.getResultList().listIterator();
			while(iter.hasNext()) {
				UserRoleCampaignResult currResult = (UserRoleCampaignResult) iter.next();
				awRequest.getUser().addCampaignRole(currResult.getCampaignUrn(), currResult.getUserRoleId());
			}
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}

}
