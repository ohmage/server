package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service for deleting a campaign.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignDeletionService.class);
	
	/**
	 * Builds a service to delete a campaign.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public CampaignDeletionService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Attempts to delete the campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to delete the campaign.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "This user cannot delete this campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
