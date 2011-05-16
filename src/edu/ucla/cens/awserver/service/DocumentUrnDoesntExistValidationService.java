package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates with the database that the URN doesn't already exist.
 * 
 * @author John Jenkins
 */
public class DocumentUrnDoesntExistValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(DocumentUrnDoesntExistValidationService.class);
	
	/**
	 * Sets up this service with an annotator to respond with should the
	 * validation fail and a DAO to use to check the database.
	 * 
	 * @param annotator The annotator to use should the validation fail.
	 * 
	 * @param dao The DAO to run that will query the database.
	 */
	public DocumentUrnDoesntExistValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Runs the DAO and catches any exceptions it throws.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that this document's URN doesn't already exist.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The URN already exists for another document.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

}
