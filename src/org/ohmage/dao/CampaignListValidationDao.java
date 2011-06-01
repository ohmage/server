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
package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * Checks that all the campaign URNs in the campaign URN list exist.
 * 
 * @author John Jenkins
 */
public class CampaignListValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignListValidationDao.class);
	
	private static final String SQL_GET_CAMPAIGN_EXISTS = "SELECT EXISTS(" +
														  	"SELECT * " +
														  	"FROM campaign " +
														  	"WHERE urn = ?" +
														  ")";
	
	private static final String SQL_GET_USER_IN_CAMPAIGN_EXISTS = "SELECT EXISTS(" +
																  	"SELECT * " +
																  	"FROM user u, campaign c, user_role_campaign urc " +
																  	"WHERE u.login_id = ? " +
																  	"AND u.id = urc.user_id " +
																  	"AND c.urn = ? " +
																  	"AND c.id = urc.campaign_id " +
																  ")";
	
	private boolean _required;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public CampaignListValidationDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Ensures that the campaign URN list exists if required and, if available,
	 * checks that each of the URNs in the list exist as a campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the campaign URN list if available.
		String campaignList;
		try {
			campaignList = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			// If it doesn't exist but is required, throw an exception as this
			// state shouldn't be allowed.
			if(_required) {
				throw new DataAccessException("Missing required parameter in toProcess map: " + InputKeys.CAMPAIGN_URN_LIST);
			}
			// If it doesn't exist and isn't required, just return.
			else {
				return;
			}
		}

		// Parse the list and check each of the URNs individually, setting the
		// request to failed if it doesn't exist.
		String[] campaignArray = campaignList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < campaignArray.length; i++) {
			// Ensure that the class exists.
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_EXISTS, new Object[] { campaignArray[i] }) == 0) {
					_logger.info("No such campaign: " + campaignArray[i]);
					awRequest.setFailedRequest(true);
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_EXISTS + "' with parameter: " + campaignArray[i], e);
				throw new DataAccessException(e);
			}
			
			// Ensure that the user exists in the class.
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_USER_IN_CAMPAIGN_EXISTS, new Object[] { awRequest.getUser().getUserName(), campaignArray[i] }) == 0) {
					_logger.info("The user doesn't belong to the campaign: " + campaignArray[i]);
					awRequest.setFailedRequest(true);
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USER_IN_CAMPAIGN_EXISTS + "' with parameters: " + 
						awRequest.getUser().getUserName() + ", " + campaignArray[i], e);
				throw new DataAccessException(e);
			}
		}
	}
}
