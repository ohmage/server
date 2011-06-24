package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Compares whether or not a class exists with whether or not it should exist 
 * and if there is a miss-match, it will set the request as failed and annotate
 * a response.
 * 
 * @author John Jenkins
 */
public class ClassExistsService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(ClassExistsService.class);
	
	private final boolean _shouldExist;
	private final boolean _required;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with should the class exist 
	 * 					and isn't supposed to or visa versa.
	 * 
	 * @param dao The DAO to call to get whether or not the class exists.
	 * 
	 * @param shouldExist Whether or not the class should exist.
	 * 
	 * @param required Whether or not this check is required.
	 */
	public ClassExistsService(AwRequestAnnotator annotator, Dao dao, boolean shouldExist, boolean required) {
		super(dao, annotator);
		
		_shouldExist = shouldExist;
		_required = required;
	}

	/**
	 * Checks whether or not a class exists and, if that doesn't match with
	 * whether or not it should exist, it sets the request as failed.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Check if the class ID exists and, if not and it's not required, just
		// return.
		try {
			awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key: " + InputKeys.CLASS_URN);
			}
			else {
				return;
			}
		}
		
		_logger.info("Checking if the class exists and if that is expected.");
		
		// Get whether or not the class in the request exists.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Compare the classes actual existance with whether or not it should
		// exist.
		try {
			Boolean exists = (Boolean) awRequest.getResultList().get(0);
			
			if(exists != _shouldExist) {
				getAnnotator().annotate(awRequest, "The request requires that the service " + ((! _shouldExist) ? "not" : "") + " exist but the class does " + ((_shouldExist) ? "not" : "") + " exist.");
				awRequest.setFailedRequest(true);
				return;
			}
		}
		catch(NullPointerException e) {
			throw new ServiceException("The DAO returned without error, but it didn't set the result list.", e);
		}
		catch(IndexOutOfBoundsException e) {
			throw new ServiceException("The DAO returned without error, but the result list it set was empty.", e);
		}
		catch(ClassCastException e) {
			throw new ServiceException("The DAO returned a non-empty result list, but the objects in the list were not of expected type Boolean.");
		}
	}
}