package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.SurveyResponsePrivacyStateCache;
import edu.ucla.cens.awserver.domain.SurveyResponseReadResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Filters survey responses based on the privacy_state of the response and the currently logged-in user's role(s). 
 * 
 * Authors or analysts cannot view private survey responses.
 * Participants cannot view invisible data (the special case for the CHIPTS study).
 * 
 * @author Joshua Selsky
 */
public class SurveyResponsePrivacyFilterService implements Service {
	private static Logger _logger = Logger.getLogger(SurveyResponsePrivacyFilterService.class);
	
	/**
	 * Retrieves the result list from the awRequest and removes results if they are not viewable by the currently logged-in 
	 * user.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("Filtering survey response results according to our privacy rules.");
		
		List<?> results = awRequest.getResultList();
		
		if(! results.isEmpty()) {
			
			int numberOfResults = results.size();
			
			User user = awRequest.getUser();
			String campaignUrn = awRequest.getCampaignUrn();
			
			if(! user.isSupervisorInCampaign(campaignUrn)) { // supervisors can read all data, all the time
				
				if(user.isParticipantInCampaign(campaignUrn) 
					&& user.getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1) {
					
					for(int i = 0; i < numberOfResults; i++) {
						if(((SurveyResponseReadResult) results.get(i)).getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_INVISIBLE)) { 
							results.remove(i);
							i--;
							numberOfResults--;
						}
					}
					
				} else if (user.isOnlyAnalystOrAuthor(campaignUrn)){
					
					for(int i = 0; i < numberOfResults; i++) {
						if(((SurveyResponseReadResult) results.get(i)).getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_PRIVATE)) { 
							results.remove(i);
							i--;
							numberOfResults--;
						}
					}
				}
			}
		}
		
		_logger.info(results.size() + " results after filtering");
	}
}
