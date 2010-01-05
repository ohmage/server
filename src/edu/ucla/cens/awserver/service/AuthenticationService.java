package edu.ucla.cens.awserver.service;


import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.dao.AuthenticationDao.LoginResult;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Dispatches to a DAO to perform authentication checks.
 * 
 * @author selsky
 */
public class AuthenticationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(AuthenticationService.class);
	
	/**
	 * Creates an instances of this class using the supplied DAO as the method of data access.
	 */
	public AuthenticationService(Dao dao) {
		super(dao);
	}
	
	/**
	 *
	 */
	public void execute(AwRequest awRequest) {
		try {
			// It would be nice if the service could tell the DAO how to format
			// the results the DAO returns
			getDao().execute(awRequest);
			
			// A List is returned from the DAO even though our login ids are unique in the db.
			List<?> results = (List<?>) awRequest.getPayload().get("results");
			
			if(null != results && results.size() > 0) {
			
				// This check is perfunctory, but indicates a serious problem because the condition 
				// can only occur if we have more than one user with the same id, which is something enforced in the data world.
				if(results.size() > 1) {
					throw new ServiceException("more than one user returned for id " + awRequest.getUser().getUserName());
				}
				
				LoginResult loginResult = (LoginResult) results.get(0);
				awRequest.getUser().setCampaignId(loginResult.getCampaignId());
				
				_logger.info("user " + awRequest.getUser().getUserName() + " successfully logged in");
				
			} else { // no user found
				
				awRequest.getPayload().put("failedRequest", "true");
				awRequest.getPayload().put("errorMessage", "user not found in db");
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
