package edu.ucla.cens.awserver.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.ClassDescription;
import edu.ucla.cens.awserver.domain.UserRoleClassResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Populates the user object of the currently logged in user with all of the
 * classes the belong to and their roles in those classes.
 * 
 * @author John Jenkins
 */
public class UserRoleClassPopulationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleClassPopulationService.class);
	
	/**
	 * Creates this service with a DAO to get the information from the
	 * database.
	 * 
	 * @param dao The DAO to use to retrieve the information from the database.
	 */
	public UserRoleClassPopulationService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Runs the DAO which returns a list of classes and the roles that the
	 * currently logged in user has in those classes.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Adding classes and roles to the user object.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.getResultList().isEmpty()) {
				_logger.info("The user doesn't belong to any classes.");
				return;
			}
			
			ListIterator<?> iter = awRequest.getResultList().listIterator();
			
			while(iter.hasNext()) {
				UserRoleClassResult currResult = (UserRoleClassResult) iter.next();
				
				ClassDescription currClass = new ClassDescription(currResult.getUrn(), currResult.getName(), currResult.getDescription());
				awRequest.getUser().addClassRole(currClass, currResult.getRole());
			}
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}

}
