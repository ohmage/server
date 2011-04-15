package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks to ensure that the campaign in the request exists.
 * 
 * @author John Jenkins
 */
public class CampaignExistsService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignExistsService.class);
	
	/**
	 * Builds a validation service to check if a campaign exists.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public CampaignExistsService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Checks that the campaign in question actually exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking whether the campaign exists.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The campaign in question does not exist.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
