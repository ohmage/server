package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Gets whether or not the username already exists in the database.
 * 
 * @author John Jenkins
 */
public class NewUsernameDoesNotExistService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(NewUsernameDoesNotExistService.class);
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the username 
	 * 					already exist.
	 * 
	 * @param dao The DAO to run to get whether or not the username already 
	 * 			  exists.
	 */
	public NewUsernameDoesNotExistService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Calls the DAO which returns whether or not the username already exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking if the new username already exists.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		boolean alreadyExists;
		try {
			alreadyExists = (Boolean) awRequest.getResultList().get(0);
		}
		catch(NullPointerException e) {
			_logger.error("The DAO returned without error but never set the result list.");
			throw new ServiceException(e);
		}
		catch(IndexOutOfBoundsException e) {
			_logger.error("The DAO returned a list, but it was empty.");
			throw new ServiceException(e);
		}
		catch(ClassCastException e) {
			_logger.error("The DAO returned a non-empty list, but its result was not the expected type Boolean.");
			throw new ServiceException(e);
		}
		
		if(alreadyExists) {
			getAnnotator().annotate(awRequest, "The username already exists.");
			awRequest.setFailedRequest(true);
		}
	}
}