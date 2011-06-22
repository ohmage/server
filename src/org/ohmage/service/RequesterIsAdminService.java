package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks whether or not the requesting user is an admin and, if not, sets the
 * request as failed.
 *  
 * @author John Jenkins
 */
public class RequesterIsAdminService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(RequesterIsAdminService.class);
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to use if the requester is not an admin.
	 * 
	 * @param dao The DAO to call to get whether or not the requester is an
	 * 			  admin.
	 */
	public RequesterIsAdminService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Checks that the requesting user is an admin, and if not, sets the 
	 * request to failed.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Ensuring that the requesting user is an admin.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		boolean isAdmin;
		try {
			isAdmin = (Boolean) awRequest.getResultList().get(0);
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
			_logger.error("The DAO returned without error, but its result was not the expected type Boolean.");
			throw new ServiceException(e);
		}
		
		if(! isAdmin) {
			getAnnotator().annotate(awRequest, "The user is not an admin.");
			awRequest.setFailedRequest(true);
		}
	}
}