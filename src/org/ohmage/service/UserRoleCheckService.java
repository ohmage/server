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
import org.ohmage.domain.UserRole;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service that checks that the currently logged-in user is running a query against their own data or, if the logged-in user is 
 * running queries against another user's data, checks that the logged-in user is a researcher or admin.  
 * 
 * @author selsky
 */
public class UserRoleCheckService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserRoleCheckService.class);
	
	public UserRoleCheckService(AwRequestAnnotator annotator) {
		super(annotator);
	}
     
	/**
	 * Checks whether a participant user is attempting to run queries against other user's data. Assumes the logged-in user 
	 * belongs to the campaign in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		
		_logger.info("verifying the logged-in user's role and whether the role specifies access to other user's data");
		
		List<UserRole> list = awRequest.getUser().getCampaignUserRoleMap().get(awRequest.getCampaignUrn()).getUserRoles();
		
		// TODO for now, only supervisors have access to see other user's data
		boolean supervisor = false;
		
		for(UserRole ur : list) {
			if("supervisor".equals(ur.getRole())) {
				supervisor = true;
				break;
			}
		}
		
		if(! supervisor) { // participants can only run queries for themselves
			
			// hackeroo - this should be a strategy or something like it
			if(awRequest instanceof SurveyResponseReadAwRequest) {
				// assumes that the userNameList has been validated (i.e., that there is at least one user in the list) 
				
				SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
				
				if(req.getUserList().size() != 1) {
					
					_logger.warn("logged in participant attempting to run query for another user. " 
						+ " logged in user: " +  req.getUser().getUserName() + " query user: "
						+ req.getUserListString());
					getAnnotator().annotate(req, "logged in user and query user must be the same for users with a role "  
						+ "of participant");
					
				} else {
					
					String u = req.getUserList().get(0);
					
					if(! req.getUser().getUserName().equals(u)) { // this will also cover the case where the logged-in participant
						                                          // is attemping to use urn:ohmage:special:all
						
						_logger.warn("logged in participant attempting to run query for another user. " 
							+ " logged in user: " +  req.getUser().getUserName() + " query user: "
							+ req.getUserListString());
						getAnnotator().annotate(awRequest, "logged in user and query user must be the same for users with a role "  
							+ "of participant");
					}
				}
				 
			} else {

				if(! awRequest.getUser().getUserName().equals(awRequest.getUserNameRequestParam())) {
					_logger.warn("logged in participant attempting to run query for another user. " 
						+ " logged in user: " +  awRequest.getUser().getUserName() + " query user: "
						+ awRequest.getUserNameRequestParam());
					getAnnotator().annotate(awRequest, "logged in user and query user must be the same for users with a role "  
						+ "of participant");
				}
			}
		} 
	}
}
