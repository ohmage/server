package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.dao.AuthenticationDao.LoginResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Dispatches to a DAO to perform authentication checks.
 * 
 * @author selsky
 */
public class AuthenticationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(AuthenticationService.class);
	private String _errorMessage;
	private String _disabledMessage;
	
	/**
	 * Creates an instance of this class using the supplied DAO as the method of data access. The error message and the data 
	 * message are set through this constructor in order to customize the output depending on the context in which this 
	 * component is used (i.e., is the response being sent to a web page or a phone). 
	 * 
	 * @throws IllegalArgumentException if errorMessage is null, empty, or all whitespace 
	 * @throws IllegalArgumentException if disabledMessage is null, empty, or all whitespace
	 */
	public AuthenticationService(Dao dao, AwRequestAnnotator awRequestAnnotator, String errorMessage, String disabledMessage) {
		super(dao, awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(errorMessage)) {
			throw new IllegalArgumentException("an error message is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(disabledMessage)) {
			throw new IllegalArgumentException("a disabled message is required");
		}
		_errorMessage = errorMessage;
		_disabledMessage = disabledMessage;
	}
	
	/**
	 * If a user is found by the DAO, sets the id and the campaign id on the User in the AwRequest. If a user is not found, sets 
	 * the failedRequest and failedRequestErrorMessage properties on the AwRequest.
	 * 
	 * @throws ServiceException if any DataAccessException occurs in the data layer
	 */
	public void execute(AwRequest awRequest) {
		try {
			// It would be nice if the service could tell the DAO how to format
			// the results the DAO returns
			getDao().execute(awRequest);
			
			// A List is returned from the DAO even though our login ids are unique in the db.
			// List<?> results = (List<?>) awRequest.getAttribute("results");
			List<?> results = awRequest.getResultList();
			
			if(null != results && results.size() > 0) {
			
				// This check is perfunctory, but indicates a serious problem because the condition can only occur if there is more 
				// than one user with the same id, which is something enforced in the data world.
				if(results.size() > 1) {
					throw new ServiceException("more than one user returned for user name (user.login_id) " + awRequest.getUser().getUserName());
				}
				
				LoginResult loginResult = (LoginResult) results.get(0);
				
				// Determine if the user is enabled (i.e., allowed to access the application)
				if(loginResult.isEnabled()) {
					
					awRequest.getUser().setCampaignId(loginResult.getCampaignId());
					awRequest.getUser().setId(loginResult.getUserId());
					awRequest.getUser().setLoggedIn(true);
					
					_logger.info("user " + awRequest.getUser().getUserName() + " successfully logged in");
				
				} else {
					
					getAnnotator().annotate(awRequest, _disabledMessage);
					_logger.info("user " + awRequest.getUser().getUserName() + " is not enabled for access");
				}
				
			} else { // no user found
				
				getAnnotator().annotate(awRequest, _errorMessage);
				_logger.info("user " + awRequest.getUser().getUserName() + " not found");
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
