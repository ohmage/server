package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks the contents of a DAO to ensure that all of the information in the
 * XML is valid to add a new campaign.
 * 
 * @author John Jenkins
 */
public class CampaignContentsValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignContentsValidationService.class);
	
	/**
	 * Creates an annotating DAO object that will check the database for some
	 * information and, on error, will report the contents of the annotator.
	 * 
	 * @param annotator What should be reported if there is an error.
	 * 
	 * @param dao The DAO to use to access the database.
	 */
	public CampaignContentsValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Calls the DAO and, if an error occurrs, reports it via the annotator.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating the contents of a campaign that is attempting to be created.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "Campaign with the same URN already exists.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
