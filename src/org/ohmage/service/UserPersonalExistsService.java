package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks if the user already has a personal entry and, if not, that all of the
 * required fields to build a personal entry exist or that no fields, optional
 * or required, exist.
 * 
 * @author John Jenkins
 */
public class UserPersonalExistsService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(UserPersonalExistsService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with should there not yet be a
	 * 					personal entry for this user, and not all of the 
	 * 					required fields exist.
	 * 
	 * @param dao The DAO to use to query whether or not the user has a 
	 * 			  personal entry.
	 */
	public UserPersonalExistsService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Gets all of the user personal fields that are required to create a new
	 * user personal object and check if any of the optional fields exist. If
	 * any of the fields, optional or not, exist, check if the user is already
	 * associated with a user personal object. If so, any combination of the
	 * fields may exist as they can simply be updated. If not, all of the
	 * required fields must be present.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that either the user already has a personal account or that all of the required values exist to create a new one.");
		
		boolean anyExist = false;
		
		// Get all of the required user personal objects from the request.
		String firstName = null;
		try {
			firstName = (String) awRequest.getToProcessValue(InputKeys.FIRST_NAME);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String lastName = null;
		try {
			lastName = (String) awRequest.getToProcessValue(InputKeys.LAST_NAME);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String organization = null;
		try {
			organization = (String) awRequest.getToProcessValue(InputKeys.ORGANIZATION);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String personalId = null;
		try {
			personalId = (String) awRequest.getToProcessValue(InputKeys.PERSONAL_ID);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		// Check if any of the optional user personal objects exist in the
		// request.
		try {
			awRequest.getToProcessValue(InputKeys.EMAIL_ADDRESS);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		try {
			awRequest.getToProcessValue(InputKeys.USER_JSON_DATA);
			anyExist = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		if(anyExist) {
			try {
				// If any of them exist, run the DAO.
				getDao().execute(awRequest);
			}
			catch(DataAccessException e) {
				throw new ServiceException(e);
			}
			
			// If there is no user information, then all of the required ones 
			// must exist.
			try {
				if(! (Boolean) awRequest.getResultList().get(0)) {
					if(firstName == null) {
						getAnnotator().annotate(awRequest, "The first name was not given, but to create a new user personal object it is required.");
						awRequest.setFailedRequest(true);
					}
					else if(lastName == null) {
						getAnnotator().annotate(awRequest, "The last name was not given, but to create a new user personal object it is required.");
						awRequest.setFailedRequest(true);
					}
					else if(organization == null) {
						getAnnotator().annotate(awRequest, "The organization was not given, but to create a new user personal object it is required.");
						awRequest.setFailedRequest(true);
					}
					else if(personalId == null) {
						getAnnotator().annotate(awRequest, "The personal ID not given, but to create a new user personal object it is required.");
						awRequest.setFailedRequest(true);
					}
				}
			}
			catch(NullPointerException e) {
				_logger.error("The DAO returned without error, but never set the request's result list.");
				throw new DataAccessException(e);
			}
			catch(IndexOutOfBoundsException e) {
				_logger.error("The DAO set the result list, but the list was empty.");
				throw new DataAccessException(e);
			}
			catch(ClassCastException e) {
				_logger.error("The DAO returned a non-empty list, but the type of the object in the list was not the expected type Boolean.");
				throw new DataAccessException(e);
			}
		}
	}
}