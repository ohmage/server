package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that the user has sufficient permissions to create a new campaign.
 * 
 * @author John Jenkins
 */
public class UserCanCreateCampaignsValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserCanCreateCampaignsValidationService.class);
	
	/**
	 * Creates a validation service to check if the user has sufficient
	 * permissions to create a campaign.
	 * 
	 * @param annotator The annotator to respond with if the user doesn't have
	 * 					sufficient permissions.
	 * 
	 * @param dao The DAO to run our queries against.
	 */
	public UserCanCreateCampaignsValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Executes the DAO and throws a ServiceException if the user doesn't have
	 * sufficient permissions.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating whether or not the user is allowed to create campaigns.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "User doesn't have sufficient permissions to create a campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
