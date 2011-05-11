package edu.ucla.cens.awserver.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.Campaign;
import edu.ucla.cens.awserver.domain.UserRoleCampaignResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service populating User objects with Campaign and User Role information.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
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
			
			if(awRequest.getResultList().isEmpty()) {
				getAnnotator().annotate(awRequest, "User doesn't belong to campaigns.");
				return;
			}
			
			ListIterator<?> iter = awRequest.getResultList().listIterator();
			
			while(iter.hasNext()) {
				UserRoleCampaignResult currResult = (UserRoleCampaignResult) iter.next();
				Campaign campaign = new Campaign();
				campaign.setCampaignCreationTimestamp(currResult.getCampaignCreationTimestamp());
				campaign.setDescription(currResult.getCampaignDescription());
				campaign.setName(currResult.getCampaignName());
				campaign.setPrivacyState(currResult.getCampaignPrivacyState());
				campaign.setRunningState(currResult.getCampaignRunningState());
				campaign.setUrn(currResult.getCampaignUrn());
				
				awRequest.getUser().addCampaignRole(campaign, currResult.getUserRole());
			}
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
