package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that the user has sufficient roles to modify the campaign and then
 * makes all the changes to the campaign.
 * 
 * @author John Jenkins
 */
public class CampaignUpdateService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignUpdateService.class);
	
	/**
	 * Default constructor.
	 * 
	 * @param annotator What to be returned to the user if the service fails.
	 * 
	 * @param dao The DAO to call to run the service.
	 */
	public CampaignUpdateService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Calls the DAO and annotates if there are any errors.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to update the campaign.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The user has insufficient permissions to modify this campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
