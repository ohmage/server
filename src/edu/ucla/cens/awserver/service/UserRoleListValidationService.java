package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Validates the list users and roles in the list in this request.
 * 
 * @author John Jenkins
 */
public class UserRoleListValidationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleListValidationService.class);
	
	/**
	 * Creates this service with a DAO.
	 * 
	 * @param dao The DataAccessObject to use to run this service.
	 */
	public UserRoleListValidationService(Dao dao) {
		super(dao);
	}

	/**
	 * Executes the DAO.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating the usernames and roles in the request.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

}
