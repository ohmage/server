package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyUploadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Configurable campaign state validation to be used in various service workflows. Any flow where the campaign state needs to be 
 * checked against a specific state will find this class useful. The AwRequest will be marked as failed if the running_state of the 
 * campaign_urn does not match the running state provided upon construction. 
 * 
 * @author joshua selsky
 */
public class CampaignStateValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignStateValidationService.class);
	private String _allowedState;
	private AwRequestAnnotator _failedRequestAnnotator;
	
	/**
	 * @param annotator - required and used to annotate the AwRequest instead of errors
	 * @param userRoleCacheService - required and used to lookup String values of integer role ids
	 * @param allowedState - required. The state to be validated against
	 * @throws IllegalArgumentException if the annotator, userRoleCacheService, or roles are empty or null
	 */
	public CampaignStateValidationService(AwRequestAnnotator annotator, Dao dao, String allowedState) {
		super(dao, annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(allowedState)) {
			throw new IllegalArgumentException("an allowedState is required");
		}
		_allowedState = allowedState;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		getDao().execute(awRequest);
		
		// hack for now until toProcess is utilized
		SurveyUploadAwRequest req = null;
		try {
			req = (SurveyUploadAwRequest) awRequest;
		} catch (ClassCastException e) {
			_logger.error("Checking campaign running state on a non-SurveyUploadAwRequest object.");
			throw new DataAccessException("Invalid request.");
		}
		
		if(! req.getCampaignRunningState().equals(_allowedState)) {
			awRequest.setFailedRequest(true);
		}
		
		_failedRequestAnnotator.annotate(awRequest, "campaign " + awRequest.getCampaignUrn() + " is not " + _allowedState);
	}
}
