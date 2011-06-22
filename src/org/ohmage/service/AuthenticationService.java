/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.LoginResult;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


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
			throw new IllegalArgumentException("An error AwRequestAnnotator is required");
		}
		if(null == disabledAccountAwRequestAnnotator) {
			throw new IllegalArgumentException("A disabled account AwRequestAnnotator is required");
		}
		if(null == newAccountAwRequestAnnotator) {
			_logger.info("Configured without a new account message");
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
					
					if(! loginResult.isEnabled()) {
						
						_disabledAccountAwRequestAnnotator.annotate(awRequest, "Disabled account.");
						_logger.info("User " + awRequest.getUser().getUserName() + "'s account is disabled");
						return;
					}
					
					if(! _newAccountsAllowed && loginResult.isNew()) {
						
						_newAccountAwRequestAnnotator.annotate(awRequest, "New account disallowed access.");
						_logger.info("User " + awRequest.getUser().getUserName() + " is new and must change their password via " +
							"phone before being granted access");
						return;
					}
					
					if(0 == i) { // first time thru: grab the properties that are common across all LoginResults (i.e., data from
						         // the user table)
						
						awRequest.getUser().setId(loginResult.getUserId());
						awRequest.getUser().setLoggedIn(true);
					}
					
					// set the campaigns and the roles within the campaigns that the user belongs to
					//awRequest.getUser().addCampaignRole(loginResult.getCampaignUrn(), loginResult.getUserRoleId());
				}
				
				_logger.info("User " + awRequest.getUser().getUserName() + " successfully logged in");
				
			} else { // no user found or invalid password
				
				_errorAwRequestAnnotator.annotate(awRequest, "User not found or invalid password");
				_logger.info("User " + awRequest.getUser().getUserName() + " not found or invalid password was supplied");
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
