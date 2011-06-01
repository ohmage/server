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

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Checks that the user has sufficient permissions in each of the campaigns in
 * the campaign-role list to associate a document with that campaign.
 * 
 * @author John Jenkins
 */
public class UserCanAssociateDocumentsWithCampaignsInCampaignRoleListService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserCanAssociateDocumentsWithCampaignsInCampaignRoleListService.class);

	private String _key;
	private boolean _required;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					sufficient permissions to associate a document with 
	 * 					some campaign in the list.
	 * 
	 * @param required Whether or not this validation is required.
	 * 
	 * @throws IllegalArgumentException Thrown if there is a problem with any
	 * 									of the parameters that prevent this
	 * 									service from being constructed.
	 */
	public UserCanAssociateDocumentsWithCampaignsInCampaignRoleListService(AwRequestAnnotator annotator, String key, boolean required) throws IllegalArgumentException {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}
	
	/**
	 * Ensures that the list of campaigns and roles exist if required and then
	 * checks that the user has sufficient permissions in each of the campaigns
	 * to associate a document it.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of classes and roles.
		String campaignRoleList;
		try {
			campaignRoleList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key '" + InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST + "' in toProcess map.");
			}
			else {
				return;
			}
		}
		
		_logger.info("Checking that the user is allowed to associate documents with each of the classes.");
		
		// Check that the user is allowed to create documents in each of those
		// classes.
		String[] campaignRoleArray = campaignRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < campaignRoleArray.length; i++) {
			String[] campaignRole = campaignRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			if((! awRequest.getUser().isSupervisorInCampaign(campaignRole[0])) &&
			   (! awRequest.getUser().isAuthorInCampaign(campaignRole[0])) &&
			   (! awRequest.getUser().isAnalystInCampaign(campaignRole[0])) &&
			   (! awRequest.getUser().isParticipantInCampaign(campaignRole[0]))) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "The user has insufficient privileges to create a new document in the campaign: " + campaignRole[0]);
			}
		}
	}
}
