package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that the parameterized user in the request exists.
 * 
 * @author John Jenkins
 */
public class UserExistsService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(UserExistsService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use if the user doesn't exist.
	 * 
	 * @param dao The DAO to call to check if the user exists.
	 */
	public UserExistsService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Calls the DAO and checks that the result is true.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking whether the user in the request exists.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		if(! (Boolean) awRequest.getResultList().get(0)) {
			getAnnotator().annotate(awRequest, "The parameterized user doesn't exist.");
		}
	}
}