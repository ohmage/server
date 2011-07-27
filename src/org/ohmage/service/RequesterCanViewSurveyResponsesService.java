package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that the requester can view the survey responses of other users in
 * the campaign.
 * 
 * @author John Jenkins
 */
public class RequesterCanViewSurveyResponsesService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(RequesterCanViewSurveyResponsesService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use should the requester not have a
	 * 					required role.
	 * 
	 * @param dao The DAO to get the campaign's privacy state.
	 */
	public RequesterCanViewSurveyResponsesService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Ensures that the requesting user can view the survey responses of other
	 * users in the campaign. This will always be true if the user is 
	 * requesting to see their own data.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking that the user has the required roles in the parameterized campaign.");
		
		if(awRequest.getUser().getUserName().equals(awRequest.getToProcessValue(InputKeys.USER))) {
			return;
		}
		
		// Get the privacy state of the campaign.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		String privacyState = (String) awRequest.getResultList().get(0);
		
		String campaignId = awRequest.getCampaignUrn();
		
		// The user needs to be a supervisor or author of the campaign or they
		// need to be an analyst and the campaign needs to be shared.
		if((! awRequest.getUser().isSupervisorInCampaign(campaignId)) && 
		   (! awRequest.getUser().isAuthorInCampaign(campaignId)) &&
		   (! (awRequest.getUser().isAnalystInCampaign(campaignId) && CampaignPrivacyStateCache.PRIVACY_STATE_SHARED.equals(privacyState)))) {
			
			getAnnotator().annotate(awRequest, "The user doesn't have the required permissions to view the survey responses of other users.");
			awRequest.setFailedRequest(true);
		}
	}
}