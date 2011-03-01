package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.dao.LoginResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service for authenticating users via phone or web page.
 * 
 * @author selsky
 */
public class AuthenticationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(AuthenticationService.class);
	private AwRequestAnnotator _errorAwRequestAnnotator;
	private AwRequestAnnotator _disabledAccountAwRequestAnnotator;
	private AwRequestAnnotator _newAccountAwRequestAnnotator;
	private boolean _newAccountsAllowed;
	
	/**
	 * Creates an instance of this class using the supplied DAO as the method of data access. The *Message parameters are set 
	 * through this constructor in order to customize the output depending on the context in which this component is used  
	 * (i.e., is login occuring through a web page or a phone). The newAccountsAllowed parameter specifies whether this 
	 * service will treat new accounts as successful logins. New accounts are allowed access for the intial login via a phone and
	 * blocked in every other case.
	 * 
	 * @throws IllegalArgumentException if errorAwRequestAnnotator is null 
	 * @throws IllegalArgumentException if disabledAccountAwRequestAnnotator is null
	 */
	public AuthenticationService(Dao dao, AwRequestAnnotator errorAwRequestAnnotator, 
			AwRequestAnnotator disabledAccountAwRequestAnnotator, AwRequestAnnotator newAccountAwRequestAnnotator, 
			boolean newAccountsAllowed) {
		
		super(dao);
		
		if(null == errorAwRequestAnnotator) {
			throw new IllegalArgumentException("an error AwRequestAnnotator is required");
		}
		if(null == disabledAccountAwRequestAnnotator) {
			throw new IllegalArgumentException("a disabled account AwRequestAnnotator is required");
		}
		if(null == newAccountAwRequestAnnotator) {
			_logger.info("configured without a new account message");
		}
		
		_errorAwRequestAnnotator = errorAwRequestAnnotator;
		_disabledAccountAwRequestAnnotator = disabledAccountAwRequestAnnotator;
		_newAccountAwRequestAnnotator = newAccountAwRequestAnnotator;
		_newAccountsAllowed = newAccountsAllowed;
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
			
			List<?> results = awRequest.getResultList();
			
			if(null != results && results.size() > 0) {
				
				for(int i = 0; i < results.size(); i++) {
				
					LoginResult loginResult = (LoginResult) results.get(i);
					
					if(! _newAccountsAllowed && loginResult.isNew()) {
						
						_newAccountAwRequestAnnotator.annotate(awRequest, "new account disallowed access");
						_logger.info("user " + awRequest.getUser().getUserName() + " is new and must change their password via " +
							"phone before being granted access");
						return;
					}
					
					if(! loginResult.isEnabled()) {
					
						_disabledAccountAwRequestAnnotator.annotate(awRequest, "disabled user");
						_logger.info("user " + awRequest.getUser().getUserName() + " is not enabled for access");
						return;
					}
					
					// -------
					// TODO -- need to make sure that the user logging in has access to the campaign that was specified in the 
					// HTTP parameters (c and cv, if survey upload) -- separate AuthenticationService ??
					
					if(0 == i) { // first time thru: grab the properties that are common across all LoginResults (i.e., data from
						         // the user table)
						
						awRequest.getUser().setId(loginResult.getUserId());
						awRequest.getUser().setLoggedIn(true);
					}
					
					// set the campaigns and the roles within the campaigns that the user belongs to
					awRequest.getUser().addCampaignRole(loginResult.getCampaignName(), loginResult.getUserRoleId());
				}
				
				_logger.info("user " + awRequest.getUser().getUserName() + " successfully logged in");
				
			} else { // no user found or invalid password
				
				_errorAwRequestAnnotator.annotate(awRequest, "user not found");
				_logger.info("user " + awRequest.getUser().getUserName() + " not found or invalid password was supplied");
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
