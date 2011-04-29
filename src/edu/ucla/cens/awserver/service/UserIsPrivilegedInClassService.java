package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks if the currently logged in user is a privileged user in the only
 * class in a request.
 * 
 * @author John Jenkins
 */
public class UserIsPrivilegedInClassService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserIsPrivilegedInClassService.class);
	
	/**
	 * Sets up the annotator and DAO for this service.
	 * 
	 * @param annotator The verbage to annotate back to the user if they are
	 * 					not a privileged user in this class.
	 * 
	 * @param dao The DataAccessObject that will be called to run the query
	 * 			  against the database.
	 */
	public UserIsPrivilegedInClassService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Calls the DAO to execute the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking if the user is a privileged user in the class.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The user is not a privileged user in the class.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
