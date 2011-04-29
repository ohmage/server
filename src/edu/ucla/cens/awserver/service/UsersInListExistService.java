package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that all the users in a list exist. This is done in the DAO and all
 * this class does is call the DAO it was built with.
 * 
 * @author John Jenkins
 */
public class UsersInListExistService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UsersInListExistService.class);
	
	/**
	 * Builds the service with the annotator and DAO given.
	 * 
	 * @param annotator What to annotate back to the user if a user in the
	 * 					list does not exist.
	 * 
	 * @param dao The DAO that will be run.
	 */
	public UsersInListExistService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Runs the DAO and reports back if there is an issue.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checks that all the users in a list exist.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "A user in the list does not exist.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
