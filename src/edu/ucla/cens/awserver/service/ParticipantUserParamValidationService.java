package edu.ucla.cens.awserver.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates that the user_list parameter only contains the logged-in user, if the logged-in user is a participant.
 * 
 * @author Joshua Selsky
 */
public class ParticipantUserParamValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(ParticipantUserParamValidationService.class);
	
	public ParticipantUserParamValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("checking to see if a participant is attempting to query another user's data");
		
		// Hack FIXME
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		
		Map<String, List<UserRole>> userRoleMap = req.getUser().getCampaignUserRoleMap();
		List<UserRole> rolesInCampaign = userRoleMap.get(req.getCampaignUrn());
		
		if(null == rolesInCampaign || rolesInCampaign.isEmpty()) {
			throw new ServiceException("expected user roles to be found for campaign, but none were found");
		}
		
		if(rolesInCampaign.size() == 1 && "participant".equals(rolesInCampaign.get(0).getRole())) {
		
			if(! req.getUser().getUserName().equals(req.getUserListString())) {
				getAnnotator().annotate(req, "participants may not run queries against other user's data");
			}
		}
	}
}
