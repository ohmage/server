package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service that dispatches to a DAO to check whether the currently logged-in user owns the survey represented by the survey key.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsForParticipantValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsForParticipantValidationService.class);

	/**
	 * Basic constructor.
	 * 
	 * @param dao the DAO to be used for querying.
	 */
    public SurveyKeyExistsForParticipantValidationService(AwRequestAnnotator annotator, Dao dao) {
    	super(dao, annotator);
    }
	
    /**
     * Dispatches to the DAO only if the logged-in user is a participant (and not also a supervisor).
     */
	public void execute(AwRequest awRequest) {
		User user = awRequest.getUser();
		String campaignUrn = awRequest.getCampaignUrn();
		
		if(user.isParticipantInCampaign(campaignUrn) && ! user.isSupervisorInCampaign(campaignUrn)) {
			
			_logger.info("Checking whether the participant owns the survey represented by the survey key in the request.");
			
			try {
				
				getDao().execute(awRequest);
				
				if(awRequest.isFailedRequest()) {
					getAnnotator().annotate(awRequest, "The logged-in participant user is not the " +
							"owner of the survey he or she is attempting to update.");
				}
				
			} catch (DataAccessException dae) {
				
				throw new ServiceException(dae);
				
			}
		}
	}
}
