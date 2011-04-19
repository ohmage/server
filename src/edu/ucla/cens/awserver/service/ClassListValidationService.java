package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks the list of classes in a campaign creation request against the list
 * of classes and this user's ability to create campaigns associated with the
 * classes.
 * 
 * @author John Jenkins
 */
public class ClassListValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(ClassListValidationService.class);
	
	/**
	 * Builds a validation service for the class list in the campaign creation
	 * request.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public ClassListValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Checks that the use
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating the class list for this campaign creation request.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "Problem with class in list.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
