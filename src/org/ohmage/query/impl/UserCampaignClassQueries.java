/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserCampaignClassQueries;

/**
 * This class contains all of the functionality for reading information about
 * user-campaign-class relationships.
 * 
 * @author John Jenkins
 */
public final class UserCampaignClassQueries extends Query implements IUserCampaignClassQueries {
	// Retrieves the number of ways a user is associated with a campaign via
	// their class relationships.
	private static final String SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES =
		"SELECT COUNT(cc.id) " +
		"FROM user u, campaign c, campaign_class cc, user_class uc " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND uc.class_id = cc.class_id " +
		"AND c.urn = ? " +
		"AND c.id = cc.campaign_id";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserCampaignClassQueries(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Retrieves the number of classes through which the user is associated 
	 * with a campaign. Another way of phrasing it is, this determines how many
	 * classes to which the user is associated and then returns the number of
	 * those classes which are associated with the campaign.<br />
	 * <br />
	 * Note: This may be misleading in our current implementation. Our current
	 * method of handling this information grants all of the users in a class
	 * direct associations with the campaign when the class is associated. If a
	 * user were to then have those roles revoked, there would be no way to
	 * detect this meaning that this call would still return that class'
	 * association despite having those roles removed.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The number of classes that are associated with the campaign and
	 * 		   of which the user is a member.
	 */
	public int getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(String username, String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForInt(SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES, username, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES + "' with parameters: " +
					username + ", " + campaignId, e);
		}
	}
}
