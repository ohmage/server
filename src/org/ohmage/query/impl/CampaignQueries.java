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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.Survey;
import org.ohmage.domain.campaign.SurveyItem;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.IUserCampaignClassQueries;
import org.ohmage.query.IUserClassQueries;
import org.ohmage.query.impl.QueryResultsList.QueryResultListBuilder;
import org.ohmage.util.DateTimeUtils;
import org.ohmage.util.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting campaigns. While it may read information pertaining 
 * to other entities, the information it takes and provides should pertain to 
 * campaigns only with the exception of linking other entities to campaigns  
 * such as campaign creation which needs to be able to associate users in a 
 * in a class with this campaign in a single transaction.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class CampaignQueries extends Query implements ICampaignQueries {
	private IUserCampaignClassQueries userCampaignClassQueries;
	private IUserClassQueries userClassQueries;
	
	// Returns a boolean value of whether or not the campaign exists.
	private static final String SQL_EXISTS_CAMPAIGN = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";

	// Returns the name of a campaign.
	private static final String SQL_GET_NAME =
		"SELECT name " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the description of a campaign.
	private static final String SQL_GET_DESCRIPTION = 
		"SELECT description " +
		"FROM campaign " +
		"WHERE urn = ?";

	// Returns the XML for a campaign.
	private static final String SQL_GET_XML = 
		"SELECT xml " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the icon URL for a campaign.
	private static final String SQL_GET_ICON_URL = 
		"SELECT icon_url " +
		"FROM campaign " +
		"WHERE urn = ?";

	// Returns the running state String of a campaign.
	private static final String SQL_GET_RUNNING_STATE =
		"SELECT crs.running_state " +
		"FROM campaign c, campaign_running_state crs " +
		"WHERE c.urn = ? " +
		"AND c.running_state_id = crs.id";

	// Returns the privacy state String of a campaign.
	private static final String SQL_GET_PRIVACY_STATE = 
		"SELECT cps.privacy_state " +
		"FROM campaign c, campaign_privacy_state cps " +
		"WHERE c.urn = ?" +
		"AND c.privacy_state_id = cps.id";

	// Returns the campaign's creation timestamp.
	private static final String SQL_GET_CREATION_TIMESTAMP =
		"SELECT creation_timestamp " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the information pertaining directly to a campaign.
	private static final String SQL_GET_CAMPAIGN_INFORMATION =
		"SELECT c.name, c.description, c.icon_url, c.authored_by, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp " +
		"FROM campaign c, campaign_running_state crs, campaign_privacy_state cps " +
		"WHERE c.urn = ? " +
		"AND c.running_state_id = crs.id " +
		"AND c.privacy_state_id = cps.id";

	// Returns the unique identifier for all of the campaigns in the system.
	private static final String SQL_GET_ALL_IDS =
		"SELECT urn " +
		"FROM campaign";
	
	// Returns all campaign IDs that contain the parameterized value. Be sure
	// to add the "%"s around the parameter before calling this.
	private static final String SQL_GET_LIKE_ID =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE urn LIKE ?";
	
	// Returns all campaign IDs that contain the parameterized value. Be sure
	// to add the "%"s around the parameter before calling this.
	private static final String SQL_GET_LIKE_NAME =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE name LIKE ?";
	
	// Returns all campaign IDs that contain the parameterized value. Be sure
	// to add the "%"s around the parameter before calling this.
	private static final String SQL_GET_LIKE_DESCRIPTION =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE description LIKE ?";
	
	// Returns all campaign IDs that contain the parameterized value. Be sure
	// to add the "%"s around the parameter before calling this.
	private static final String SQL_GET_LIKE_XML =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE xml LIKE ?";
	
	// Returns all campaign IDs that contain the parameterized value. Be sure
	// to add the "%"s around the parameter before calling this.
	private static final String SQL_GET_LIKE_AUTHORED_BY =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE authored_by LIKE ?";
	
	// Returns all of the IDs for all of the campaigns whose creation timestamp
	// was on or after some date.
	private static final String SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE creation_timestamp >= ?";
	
	// Returns all of the IDs for all of the campaigns whose creation timestamp
	// was on or before some date.
	private static final String SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE creation_timestamp <= ?";
	
	// Returns all of the IDs for all of the campaigns whose privacy state is
	// some value.
	private static final String SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE privacy_state_id = (" +
			"SELECT id " +
			"FROM campaign_privacy_state " +
			"WHERE privacy_state = ?" +
		")";
	
	// Returns all of the IDs for all of the campaigns whose running state is
	// some value.
	private static final String SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE running_state_id = (" +
			"SELECT id " +
			"FROM campaign_running_state " +
			"WHERE running_state = ?" +
		")";
	
	// Retrieves the campaign roles for a user based on the default roles for
	// a campaign-class association.
	private static final String SQL_GET_USER_DEFAULT_ROLES = "SELECT ur.role "
			+ "FROM user u, campaign ca, class cl, campaign_class cc, user_role ur, user_class uc, campaign_class_default_role ccdr "
			+ "WHERE u.username = ? " + "AND ca.urn = ? " + "AND cl.urn = ? "
			+ "AND ca.id = cc.campaign_id " + "AND cl.id = cc.class_id "
			+ "AND cc.id = ccdr.campaign_class_id " + "AND u.id = uc.user_id "
			+ "AND cl.id = uc.class_id "
			+ "AND uc.user_class_role_id = ccdr.user_class_role_id "
			+ "AND ccdr.user_role_id = ur.id";
	
	// Inserts a new campaign.
	private static final String SQL_INSERT_CAMPAIGN = 
		"INSERT INTO campaign(urn, name, xml, description, icon_url, authored_by, creation_timestamp, running_state_id, privacy_state_id) " +
		"VALUES (?, ?, ?, ?, ?, ?, now(), (" +
				"SELECT id " +
				"FROM campaign_running_state " +
				"WHERE running_state = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign_privacy_state " +
				"WHERE privacy_state = ?" +
			")" +
		")";
	
	// Inserts a campagin-class association.
	private static final String SQL_INSERT_CAMPAIGN_CLASS =
		"INSERT INTO campaign_class(campaign_id, class_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			")" +
		")";
	
	// Inserts an entry into the campaign_class_default_role table.
	private static final String SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE = 
		"INSERT INTO campaign_class_default_role(campaign_class_id, user_class_role_id, user_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT cc.id " +
				"FROM class cl, campaign ca, campaign_class cc " +
				"WHERE ca.urn = ? " +
				"AND ca.id = cc.campaign_id " +
				"AND cl.urn = ? " +
				"AND cl.id = cc.class_id" +
			"), (" +
				"SELECT id " +
				"FROM user_class_role " +
				"WHERE role = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Associates a user with a campaign and a given campaign role.
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN =
		"INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Updates the campaign's XML.
	private static final String SQL_UPDATE_XML =
		"UPDATE campaign " +
		"SET xml = ?, creation_timestamp = now() " +
		"WHERE urn = ?";
	
	// Updates a campaign's description.
	private static final String SQL_UPDATE_DESCRIPTION = 
		"UPDATE campaign " +
		"SET description = ? " +
		"WHERE urn = ?";
	
	// Updates a campaign's privacy state.
	private static final String SQL_UPDATE_PRIVACY_STATE =
		"UPDATE campaign " +
		"SET privacy_state_id = (" +
			"SELECT id " +
			"FROM campaign_privacy_state " +
			"WHERE privacy_state = ?" +
		") " +
		"WHERE urn = ?";
	
	// Updates a campaign's running state.
	private static final String SQL_UPDATE_RUNNING_STATE =
		"UPDATE campaign " +
		"SET running_state_id = (" +
			"SELECT id " +
			"FROM campaign_running_state " +
			"WHERE running_state = ?" +
		") " +
		"WHERE urn = ?";
		
	// Deletes a campaign.
	private static final String SQL_DELETE_CAMPAIGN = 
		"DELETE FROM campaign " +
		"WHERE urn = ?";
	
	// Deletes a campaign, class association.
	private static final String SQL_DELETE_CAMPAIGN_CLASS =
		"DELETE FROM campaign_class " +
		"WHERE campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Removes a role from a user in a campaign.
	private static final String SQL_DELETE_USER_ROLE_CAMPAIGN =
		"DELETE FROM user_role_campaign " +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		") " +
		"AND campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		") " +
		"AND user_role_id = (" +
			"SELECT id " +
			"FROM user_role " +
			"WHERE role = ?" +
		")";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignQueries(
			DataSource dataSource, 
			IUserCampaignClassQueries iUserCampaignClassQueries, 
			IUserClassQueries iUserClassQueries) {
		
		super(dataSource);
		
		if(iUserCampaignClassQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignClassQueries is a required argument.");
		}
		
		if(iUserClassQueries == null) {
			throw new IllegalArgumentException("An instance of IUserClassQueries is a required argument.");
		}
		
		userCampaignClassQueries = iUserCampaignClassQueries; 
		userClassQueries = iUserClassQueries;
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#createCampaign(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.ohmage.domain.campaign.Campaign.RunningState, org.ohmage.domain.campaign.Campaign.PrivacyState, java.util.Collection, java.lang.String)
	 */
	public void createCampaign(
			final Campaign campaign,
			final Collection<String> classIds,
			final String creatorUsername)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			String iconUrlString = null;
			URL iconUrl = campaign.getIconUrl();
			if(iconUrl != null) {
				iconUrlString = iconUrl.toString();
			}
			
			String xml;
			try {
				xml = campaign.getXml();
			}
			catch(DomainException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("The XML could not be saved.");
			}
			
			// Create the campaign.
			try {
				getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN, 
					new Object[] { 
						campaign.getId(), 
						campaign.getName(), 
						xml, 
						campaign.getDescription(), 
						iconUrlString, 
						campaign.getAuthoredBy(), 
						campaign.getRunningState().toString(), 
						campaign.getPrivacyState().toString()
					}
				);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_INSERT_CAMPAIGN + 
						"' with parameters: " +
						campaign.getId() + ", " + 
						campaign.getName() + ", " + 
						xml + ", " + 
						campaign.getDescription() + ", " + 
						iconUrlString + ", " + 
						campaign.getAuthoredBy() + ", " + 
						campaign.getRunningState().toString() + ", " + 
						campaign.getPrivacyState().toString(),
					e);
			}
			
			// Create the set of survey and prompt IDs for this campaign.
			final Set<String> surveyIds = new HashSet<String>();
			final Set<String> promptIds = new HashSet<String>();
			
			// Loop through all of the surveys and add the survey and prompt
			// IDs.
			for(Survey survey : campaign.getSurveys().values()) {
				// Get this survey's ID.
				surveyIds.add(survey.getId());
				
				Queue<SurveyItem> surveyItems = new LinkedList<SurveyItem>();
				surveyItems.addAll(survey.getSurveyItems().values());
				while(surveyItems.size() > 0) {
					SurveyItem surveyItem = surveyItems.poll();
					
					if(surveyItem instanceof RepeatableSet) {
						RepeatableSet repeatableSet = 
							(RepeatableSet) surveyItem;
						
						for(SurveyItem rsSurveyItem : repeatableSet.getSurveyItems().values()) {
							surveyItems.add(rsSurveyItem);
						}
					}
					else if(surveyItem instanceof Prompt) {
						promptIds.add(((Prompt) surveyItem).getId());
					}
				}
			}
			
			// Get the campaign's ID.
			final String campaignId = campaign.getId();
			
			// Compile the list of parameters for the survey ID lookup table.
			List<Object[]> surveyParameters = 
				new ArrayList<Object[]>(surveyIds.size());
			for(String surveyId : surveyIds) {
				Object[] params = new Object[2];
				params[0] = surveyId;
				params[1] = campaignId;
				surveyParameters.add(params);
			}

			// The SQL to write the data.
			final String surveyIdLookupBatchSql =
				"INSERT INTO " +
					"campaign_survey_lookup(survey_id, campaign_id) " +
				"VALUES (?, (SELECT id FROM campaign WHERE urn = ?))";
			
			// Add the survey IDs to the lookup table.
			try {
				getJdbcTemplate()
					.batchUpdate(surveyIdLookupBatchSql, surveyParameters);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + 
						surveyIdLookupBatchSql + 
						"'.",
					e);
			}
			
			// Compile the list of parameters for the prompt ID lookup table.
			List<Object[]> promptParameters = 
				new ArrayList<Object[]>(surveyIds.size());
			for(String promptId : promptIds) {
				Object[] params = new Object[2];
				params[0] = promptId;
				params[1] = campaignId;
				promptParameters.add(params);
			}

			// The SQL to write the data.
			final String promptIdLookupBatchSql =
				"INSERT INTO " +
					"campaign_prompt_lookup(prompt_id, campaign_id) " +
				"VALUES (?, (SELECT id FROM campaign WHERE urn = ?))";
			
			// Add the prompt IDs to the lookup table.
			try {
				getJdbcTemplate()
					.batchUpdate(promptIdLookupBatchSql, promptParameters);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + 
						promptIdLookupBatchSql + 
						"'.",
					e);
			}
			
			// Add each of the classes to the campaign.
			for(String classId : classIds) {
				associateCampaignAndClass(
					transactionManager, 
					status, 
					campaign.getId(), 
					classId);
			}
			
			// Add the requesting user as the author. This may have already 
			// happened above.
			try {
				getJdbcTemplate()
					.update(
						SQL_INSERT_USER_ROLE_CAMPAIGN,
						creatorUsername, 
						campaign.getId(), 
						Campaign.Role.AUTHOR.toString());
			}
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				// The user was already an author of this campaign implying 
				// that it's one of the default campaign roles based on a class
				// role that the 'creatorUsername' has.
				e.printStackTrace();
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_INSERT_USER_ROLE_CAMPAIGN + 
						"' with parameters: " + 
						creatorUsername + ", " + 
						campaign.getId() + ", " + 
						Campaign.Role.AUTHOR.toString(),
					e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error while committing the transaction.",
					e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException(
				"Error while attempting to rollback the transaction.",
				e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignExists(java.lang.String)
	 */
	public Boolean getCampaignExists(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_CAMPAIGN, 
					new Object[] { campaignId }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_CAMPAIGN + "' with parameter: " + campaignId, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getAllCampaignIds()
	 */
	@Override
	public List<String> getAllCampaignIds() throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_ALL_IDS,
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_ALL_IDS + 
						"'.",
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignsFromPartialId(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsFromPartialId(String partialCampaignId)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_ID, 
					new Object[] { "%" + partialCampaignId + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_LIKE_ID + 
						"' with parameter: " +
						"%" + partialCampaignId + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignsFromPartialName(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsFromPartialName(String partialCampaignName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_NAME, 
					new Object[] { "%" + partialCampaignName + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_LIKE_NAME + 
						"' with parameter: " +
						"%" + partialCampaignName + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignsFromPartialDescription(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsFromPartialDescription(
			String partialDescription) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_DESCRIPTION, 
					new Object[] { "%" + partialDescription + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_LIKE_DESCRIPTION + 
						"' with parameter: " +
						"%" + partialDescription + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignsFromPartialXml(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsFromPartialXml(String partialXml)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_XML, 
					new Object[] { "%" + partialXml + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_LIKE_XML + 
						"' with parameter: " +
						"%" + partialXml + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignsFromPartialAuthoredBy(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsFromPartialAuthoredBy(
			String partialAuthoredBy) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_AUTHORED_BY, 
					new Object[] { "%" + partialAuthoredBy + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_LIKE_AUTHORED_BY + 
						"' with parameter: " +
						"%" + partialAuthoredBy + "%",
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignsOnOrAfterDate(java.util.Calendar)
	 */
	public List<String> getCampaignsOnOrAfterDate(DateTime date) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE,
					new Object[] { DateTimeUtils.getIso8601DateString(date, true) },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE + "' with parameter: " + DateTimeUtils.getIso8601DateString(date, true), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignsOnOrBeforeDate(java.util.Calendar)
	 */
	public List<String> getCampaignsOnOrBeforeDate(DateTime date) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE,
					new Object[] { DateTimeUtils.getIso8601DateString(date, true) },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE + "' with parameter: " + DateTimeUtils.getIso8601DateString(date, true), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignsWithPrivacyState(org.ohmage.domain.campaign.Campaign.PrivacyState)
	 */
	public List<String> getCampaignsWithPrivacyState(Campaign.PrivacyState privacyState) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE,
					new Object[] { privacyState.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE + "' with parameter: " + privacyState, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignsWithRunningState(org.ohmage.domain.campaign.Campaign.RunningState)
	 */
	public List<String> getCampaignsWithRunningState(Campaign.RunningState runningState) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE,
					new Object[] { runningState.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE + "' with parameter: " + runningState, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getName(java.lang.String)
	 */
	public String getName(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_NAME, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_NAME + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#findCampaignConfiguration(java.lang.String)
	 */
	public Campaign findCampaignConfiguration(final String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_INFORMATION, 
					new Object[] { campaignId }, 
					new RowMapper<Campaign>() {
						@Override
						public Campaign mapRow(ResultSet rs, int rowNum) 
								throws SQLException {
						
							try {
								return new Campaign(
										null,
										null,
										rs.getString("description"),
										Campaign.RunningState.getValue(
												rs.getString("running_state")),
										Campaign.PrivacyState.getValue(
												rs.getString("privacy_state")),
										rs.getTimestamp("creation_timestamp"),
										rs.getString("xml")
								);
							}
							catch(DomainException e) {
								throw new SQLException(
										"The XML is corrupt.", 
										e);
							}
						}
					}
				);
		}
		catch(IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() == 0) {
				return null;
			}
			
			throw new DataAccessException("Multiple campaigns have the same ID: " + campaignId, e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("General error executing SQL '" + SQL_GET_CAMPAIGN_INFORMATION + "' with parameter: " + campaignId, e);
		}
	}
    
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getDescription(java.lang.String)
	 */
	public String getDescription(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_DESCRIPTION, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DESCRIPTION + "' with parameter: " + campaignId, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignPrivacyState(java.lang.String)
	 */
	public Campaign.PrivacyState getCampaignPrivacyState(String campaignId) throws DataAccessException {
		try {
			return Campaign.PrivacyState.getValue(getJdbcTemplate().queryForObject(SQL_GET_PRIVACY_STATE, new Object[] { campaignId }, String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_PRIVACY_STATE + "' with parameter: " + campaignId, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignRunningState(java.lang.String)
	 */
	public Campaign.RunningState getCampaignRunningState(String campaignId) throws DataAccessException {
		try {
			return Campaign.RunningState.getValue(getJdbcTemplate().queryForObject(SQL_GET_RUNNING_STATE, new Object[] { campaignId }, String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_PRIVACY_STATE + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getXml(java.lang.String)
	 */
	public String getXml(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_XML, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_XML + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getIconUrl(java.lang.String)
	 */
	public String getIconUrl(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_ICON_URL, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_ICON_URL + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCreationTimestamp(java.lang.String)
	 */
	public DateTime getCreationTimestamp(String campaignId) throws DataAccessException {
		try {
			return new DateTime(
					((Timestamp) getJdbcTemplate().queryForObject(
						SQL_GET_CREATION_TIMESTAMP, 
						new Object[] { campaignId }, 
						Timestamp.class))
					.getTime());
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CREATION_TIMESTAMP + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#getCampaignInformation(java.lang.String)
	 */
	public Campaign getCampaignInformation(final String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_INFORMATION,
					new Object[] { campaignId },
					new RowMapper<Campaign>() {
						@Override
						public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException {
							URL iconUrl = null;
							String iconString = rs.getString("icon_url");
							if(iconString != null) {
								try {
									iconUrl = new URL(iconString);
								}
								catch(MalformedURLException e) {
									// This parameter is still experimental, so
									// we will leave this alone for now.
								}
							}
							
							try {
								return new Campaign(
										campaignId,
										rs.getString("name"),
										rs.getString("description"),
										iconUrl,
										rs.getString("authored_by"),
										Campaign.RunningState.valueOf(rs.getString("running_state").toUpperCase()),
										Campaign.PrivacyState.valueOf(rs.getString("privacy_state").toUpperCase()),
										new DateTime(rs.getTimestamp("creation_timestamp").getTime()),
										new HashMap<String, Survey>(0),
										rs.getString("xml"));
							} 
							catch(DomainException e) {
								throw new SQLException(
										"There was a problem creating the campaign.",
										e);
							}
						}
					});
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Mutiple campaigns have the same ID: " + campaignId, e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + SQL_GET_CAMPAIGN_INFORMATION +
					"' with parameter: " +
						campaignId,
					e);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignListSql(java.lang.String, java.util.Collection, java.lang.String, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, org.ohmage.domain.campaign.Campaign.PrivacyState, org.ohmage.domain.campaign.Campaign.RunningState, org.ohmage.domain.campaign.Campaign.Role)
	 */
	@Override
	public String getVisibleCampaignsSql(
			final Collection<Object> parameters,
			final String username,
			final Collection<String> campaignIds,
			final Collection<String> classIds,
			final Collection<String> nameTokens,
			final Collection<String> descriptionTokens,
			final DateTime startDate,
			final DateTime endDate,
			final Campaign.PrivacyState privacyState,
			final Campaign.RunningState runningState,
			final Campaign.Role role)
			throws DataAccessException {
					
		try {
			// Begin with a common set of elements to select, and the tables to
			// which those elements belong.
			StringBuilder builder = 
				new StringBuilder(
					"SELECT ca.id " +
					"FROM " +
						"user u, " +
						"campaign ca, " +
							"campaign_running_state crs, " +
							"campaign_privacy_state cps " +
					"WHERE u.username = ? " +
					"AND ca.running_state_id = crs.id " +
					"AND ca.privacy_state_id = cps.id " +
					// ACL
					"AND (" +
						"(u.admin = true)" +
						" OR " +
						"EXISTS (" +
							"SELECT id " +
							"FROM user_role_campaign urc " +
							"WHERE u.id = urc.user_id " +
							"AND ca.id = urc.campaign_id" +
						")" +
					")");
			
			parameters.add(username);
			
			if(campaignIds != null && campaignIds.size() > 0) {			
				builder
					.append(" AND ca.urn IN ")
					.append(StringUtils.generateStatementPList(
							campaignIds.size()));
				
				parameters.addAll(campaignIds);
			}
			
			if(classIds != null && classIds.size() > 0) {
				builder.append(
						" AND (" +
							"ca.id IN (" +
								"SELECT cc.campaign_id " +
								"FROM campaign_class cc " +
								"WHERE cc.class_id IN (" +
									"SELECT cl.id " +
									"FROM class cl " +
									"WHERE cl.urn IN " +
									StringUtils.generateStatementPList(
											classIds.size()) +
								")" +
							")" +
						")"
					);
				
				parameters.addAll(classIds);
			}
			
			if(nameTokens != null && nameTokens.size() > 0) {	
				boolean firstPass = true;
				builder.append(" AND (");
				for(String nameToken : nameTokens) {
					if(firstPass) {
						firstPass = false;
					}
					else {
						builder.append(" OR ");
					}
					
					builder.append("ca.name LIKE ?");
					parameters.add('%' + nameToken + '%');
				}
				builder.append(")");
			}
			
			if(descriptionTokens != null && descriptionTokens.size() > 0 ) {		
				boolean firstPass = true;
				builder.append(" AND (");
				for(String descriptionToken : descriptionTokens) {
					if(firstPass) {
						firstPass = false;
					}
					else {
						builder.append(" OR ");
					}
					
					builder.append("ca.description LIKE ?");
					parameters.add('%' + descriptionToken + '%');
				}
				builder.append(")");
			}
			
			if(startDate != null) {
				builder.append(" AND ca.creation_timestamp >= ?");
				
				parameters.add(DateTimeUtils.getIso8601DateString(startDate, true));
			}
			
			if(endDate != null) {
				builder.append(" AND ca.creation_timestamp <= ?");
				
				parameters.add(DateTimeUtils.getIso8601DateString(endDate, true));
			}
			
			if(runningState != null) {
				builder.append(" AND crs.running_state = ?");
				
				parameters.add(runningState.toString());
			}
			
			if(privacyState != null) {
				builder.append(" AND cps.privacy_state = ?");
				
				parameters.add(privacyState.toString());
			}
			
			if(role != null) {
				builder.append(
						" AND (" +
							"ca.id IN (" +
								"SELECT urc.campaign_id " +
								"FROM user_role ur, user_role_campaign urc " +
								"WHERE u.id = urc.user_id " +
								"AND ur.id = urc.user_role_id " +
								"AND ur.role = ?" +
							")" +
						")"
					);
				
				parameters.add(role.toString());
			}
		
			return builder.toString();
		} catch (Exception e) {
			throw new DataAccessException("Cannot build an sql statement", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaignInformation(java.lang.String, java.util.Collection)
	 */
	@Override
	public QueryResultsList<Campaign> getCampaignInformation(
			final String subSelectStmt,
			final Collection<Object> subSelectParameters
			)
			throws DataAccessException {
		
		
		if (subSelectStmt == null) {
			throw new DataAccessException("The subSelectStmt cannot be null!");
		}
		
		StringBuilder builder;
		try {
			builder = new StringBuilder( 
					"SELECT ca.urn, ca.name, ca.description, " +
						"crs.running_state, cps.privacy_state, " +
						"ca.creation_timestamp, " +
						"ca.xml " + 
					"FROM campaign ca " +
					  	"JOIN campaign_running_state crs on (ca.running_state_id = crs.id) " +
						"JOIN campaign_privacy_state cps on (ca.privacy_state_id = cps.id) " +
					"WHERE ca.id in "
					);
			builder.append("(" + subSelectStmt + " )");
			Collection<Object> parameters = subSelectParameters;

			return getJdbcTemplate().query(
					builder.toString(),
					parameters.toArray(),
					new ResultSetExtractor<QueryResultsList<Campaign>>() {
						/**
						 * Counts the total number of results and converts each
						 * of the actual results into a Campaign object.
						 */
						@Override
						public QueryResultsList<Campaign> extractData(
								ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							try {
								QueryResultListBuilder<Campaign> result = 
										new QueryResultListBuilder<Campaign>();
								
								while(rs.next()) {
									result.addResult(
											new Campaign(
													null,
													null,
													rs.getString("description"),
													Campaign.RunningState.valueOf(rs.getString("running_state").toUpperCase()),
													Campaign.PrivacyState.valueOf(rs.getString("privacy_state").toUpperCase()),
													new DateTime(rs.getTimestamp("creation_timestamp").getTime()).toDate(),
													rs.getString("xml")));
								}
							
								return result.getQueryResult();
							}
							catch(DomainException e) {
								throw new SQLException(e);
							}
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignQueries#getCampaigns(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, org.joda.time.DateTime, org.joda.time.DateTime, org.ohmage.domain.campaign.Campaign.PrivacyState, org.ohmage.domain.campaign.Campaign.RunningState, long, long)
	 */
	public List<Campaign> getCampaigns(
			final Collection<String> campaignIds,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final Collection<String> classIds,
			final DateTime startDate,
			final DateTime endDate,
			final Campaign.PrivacyState privacyState,
			final Campaign.RunningState runningState,
			final long numToSkip,
			final long numToReturn)
			throws DataAccessException {
		
		// Build the base SQL that will select the necessary columns to 
		// construct the campaigns.
		StringBuilder sqlBuilder =
			new StringBuilder(
				"SELECT " +
					"ca.urn, ca.name, ca.description, " +
					"ca.icon_url, ca.authored_by, " +
					"crs.running_state, cps.privacy_state, " +
					"ca.creation_timestamp, " +
					"ca.xml " +
				"FROM " +
					"campaign ca, " +
						"campaign_running_state crs, " + 
						"campaign_privacy_state cps " +
				"WHERE ca.running_state_id = crs.id " +
				"AND ca.privacy_state_id = cps.id");
		
		// Add the initial set of parameters.
		List<Object> parameters = new LinkedList<Object>();
		
		// Add the campaign IDs, if any.
		if((campaignIds != null) && (campaignIds.size() > 0)) {
			sqlBuilder.append(
				" AND ca.urn IN " + 
					StringUtils.generateStatementPList(campaignIds.size()));
			parameters.addAll(campaignIds);
		}
		
		// Add the survey IDs, if any.
		if((surveyIds != null) && (surveyIds.size() > 0)) {
			sqlBuilder.append(
				" AND ca.id IN (" +
					"SELECT csl.campaign_id " +
					"FROM campaign_survey_lookup csl " +
					"WHERE csl.survey_id IN " +
					StringUtils.generateStatementPList(surveyIds.size()) +
				")");
			parameters.addAll(surveyIds);
		}
		
		// Add the prompt IDs, if any.
		if((promptIds != null) && (promptIds.size() > 0)) {
			sqlBuilder.append(
				" AND ca.id IN (" +
					"SELECT cpl.campaign_id " +
					"FROM campaign_prompt_lookup cpl " +
					"WHERE cpl.prompt_id IN " +
					StringUtils.generateStatementPList(promptIds.size()) +
				")");
			parameters.addAll(promptIds);
		}
		
		// And the classes, if any.
		if((classIds != null) && (classIds.size() != 0)) {
			sqlBuilder.append(
				" AND ca.id IN (" +
					"SELECT cc.campaign_id " +
					"FROM campaign_class cc " +
					"WHERE cc.class_id IN (" +
						"SELECT cl.id " +
						"FROM class cl " +
						"WHERE cl.urn IN " +
						StringUtils.generateStatementPList(
								classIds.size()) +
					")" +
				")");
			parameters.addAll(classIds);
		}
		
		// Add the start date if one was given.
		if(startDate != null) {
			sqlBuilder.append(" AND ca.creation_timestamp >= ?");
			parameters.add(DateTimeUtils.getIso8601DateString(startDate, true));
		}
		
		// Add the end date if one was given.
		if(endDate != null) {
			sqlBuilder.append(" AND ca.creation_timestamp <= ?");
			parameters.add(DateTimeUtils.getIso8601DateString(endDate, true));
		}
		
		// Add the privacy state if one was given.
		if(privacyState != null) {
			sqlBuilder.append(" AND cps.privacy_state = ?");
			parameters.add(privacyState.toString());
		}
		
		// Add the running state if one was given.
		if(runningState != null) {
			sqlBuilder.append(" AND crs.running_state = ?");
			parameters.add(runningState.toString());
		}
		
		// Limit the number of results.
		sqlBuilder.append(" LIMIT ?, ?");
		parameters.add(numToSkip);
		parameters.add(numToReturn);
		
		try {
			return 
				getJdbcTemplate().query(
					sqlBuilder.toString(),
					parameters.toArray(),
					new RowMapper<Campaign>() {
						/**
						 * Counts the total number of results and converts each
						 * of the actual results into a Campaign object.
						 */
						@Override
						public Campaign mapRow(
								ResultSet rs,
								int rowNum)
								throws SQLException {
							
							try {
								return
									new Campaign(
										null,
										null,
										rs.getString("description"),
										Campaign.RunningState.getValue(rs.getString("running_state")),
										Campaign.PrivacyState.getValue(rs.getString("privacy_state")),
										new Date(rs.getTimestamp("creation_timestamp").getTime()),
										rs.getString("xml"));
							}
							catch(DomainException e) {
								throw new SQLException(e);
							}
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sqlBuilder.toString() +
					"' with parameters: " +
					parameters.toArray(),
				e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#updateCampaign(java.lang.String, java.lang.String, java.lang.String, org.ohmage.domain.campaign.Campaign.RunningState, org.ohmage.domain.campaign.Campaign.PrivacyState, java.util.Collection, java.util.Collection, java.util.Map, java.util.Map)
	 */
	public void updateCampaign(String campaignId, String xml, String description, 
			Campaign.RunningState runningState, 
			Campaign.PrivacyState privacyState, 
			Collection<String> classesToAdd,
			Collection<String> classesToRemove,
			Map<String, Set<Campaign.Role>> usersAndRolesToAdd, 
			Map<String, Set<Campaign.Role>> usersAndRolesToRemove)
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the XML if it is present.
			if(xml != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_XML, new Object[] { xml, campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_XML + "' with parameters: " + xml + ", " + campaignId, e);
				}
			}
			
			// Update the description if it is present.
			if(description != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { description, campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_DESCRIPTION + "' with parameters: " + description + ", " + campaignId, e);
				}
			}
			
			// Update the running state if it is present.
			if(runningState != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_RUNNING_STATE, new Object[] { runningState.toString(), campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_RUNNING_STATE + "' with parameters: " + runningState + ", " + campaignId, e);
				}
			}
			
			// Update the privacy state if it is present.
			if(privacyState != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { privacyState.toString(), campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " + privacyState + ", " + campaignId, e);
				}
			}
			
			// Add the specific users with specific roles.
			if(usersAndRolesToAdd != null) {
				for(String username : usersAndRolesToAdd.keySet()) {
					for(Campaign.Role role : usersAndRolesToAdd.get(username)) {
						try {
							getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { username, campaignId, role.toString() });
						}
						catch(org.springframework.dao.DuplicateKeyException e) {
							// This means that the user already had the role in
							// the campaign. We can ignore this.
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + 
									"' with parameters: " + username + ", " + campaignId + ", " + role, e);
						}
					}
				}
			}
			
			// Remove the specific users and their roles.
			if(usersAndRolesToRemove != null) {
				for(String username : usersAndRolesToRemove.keySet()) {
					for(Campaign.Role role : usersAndRolesToRemove.get(username)) {
						try {
							getJdbcTemplate().update(SQL_DELETE_USER_ROLE_CAMPAIGN, new Object[] { username, campaignId, role.toString() });
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + 
									"' with parameters: " + username + ", " + campaignId + ", " + role, e);
						}
					}
				}
			}
			
			if(classesToRemove != null) {
				// For all of the classes that are associated with the campaign
				// but are not in the classIds list,
				for(String classId : classesToRemove) {
					// For each of the users in the class, if they are only 
					// associated with the campaign through this class then 
					// remove them.
					List<String> usernames;
					try {
						usernames = userClassQueries.getUsersInClass(classId);
					}
					catch(DataAccessException e) {
						transactionManager.rollback(status);
						throw e;
					}
					
					for(String username : usernames) {
						// If the user is not associated with the campaign 
						// through any other class, they are removed from the
						// campaign.
						int numClasses;
						try {
							numClasses = userCampaignClassQueries.getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(username, campaignId); 
						}
						catch(DataAccessException e) {
							transactionManager.rollback(status);
							throw e;
						}
						if(numClasses == 1) {
							// Retrieve the default roles that the user was 
							// given when they joined the class.
							List<Campaign.Role> roles;
							try {
								roles = getJdbcTemplate().query(
										SQL_GET_USER_DEFAULT_ROLES, 
										new Object[] { username, campaignId, classId }, 
										new RowMapper<Campaign.Role> () {
											@Override
											public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
												return Campaign.Role.getValue(rs.getString("role"));
											}
										});
							}
							catch(org.springframework.dao.DataAccessException e) {
								transactionManager.rollback(status);
								throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
										username + ", " + campaignId + ", " + classId, e);
							}
							
							for(Campaign.Role role : roles) {
								try {
									getJdbcTemplate().update(
											SQL_DELETE_USER_ROLE_CAMPAIGN, 
											new Object[] { username, campaignId, role.toString() });
								}
								catch(org.springframework.dao.DataAccessException e) {
									transactionManager.rollback(status);
									throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + "' with parameters: " + 
											username + ", " + campaignId + ", " + role, e);
								}
							}
						}
					}

					// Remove the campaign, class association.
					try {
						getJdbcTemplate().update(SQL_DELETE_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_DELETE_CAMPAIGN_CLASS + 
								"' with parameters: " + campaignId + ", " + classId, e);
					}
				}
			}
			
			if(classesToAdd != null) {
				// For all of the classes that are in the classIds list but not
				// associated with the campaign,
				for(String classId : classesToAdd) {
					associateCampaignAndClass(transactionManager, status, campaignId, classId);
				}
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignQueries#deleteCampaign(java.lang.String)
	 */
	public void deleteCampaign(String campaignId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(SQL_DELETE_CAMPAIGN, campaignId);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_DELETE_CAMPAIGN + "' with parameter: " + campaignId, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Creates the association between a class and a campaign in the database.
	 * It then creates a set of default roles for all users of the classes and
	 * adds all of the users in the class to the campaign with the default 
	 * roles.
	 * 
	 * @param transactionManager The PlatformTransactionManager that is 
	 * 							 managing the transaction from which this was
	 * 							 called.
	 * 
	 * @param status The current status of the transaction.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param classId The unique identifier for the class.
	 */
	private void associateCampaignAndClass(PlatformTransactionManager transactionManager, TransactionStatus status, String campaignId, String classId) 
		throws DataAccessException {
		
		// Associate this class to the campaign.
		try {
			getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
		}
		catch(org.springframework.dao.DuplicateKeyException e) {
			// If the campaign was already associated with the class, ignore
			// this call.
			return;
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + 
					campaignId + ", " + classId, e);
		}
		
		// Insert the default campaign_class_default_role
		// relationships for privileged users.
		// TODO: This should be a parameter in the API.
		try {
			getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							Clazz.Role.PRIVILEGED.toString(), 
							Campaign.Role.SUPERVISOR.toString() }
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.PRIVILEGED + ", " + Campaign.Role.SUPERVISOR, e);
		}
		try {
			getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							Clazz.Role.PRIVILEGED.toString(), 
							Campaign.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.PRIVILEGED + ", " + Campaign.Role.PARTICIPANT, e);
		}
		
		// Insert the default campaign_class_default_role
		// relationships for restricted users.
		// TODO: This should be a parameter in the API.
		try {
			getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							Clazz.Role.RESTRICTED.toString(), 
							Campaign.Role.ANALYST.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.RESTRICTED + ", " + Campaign.Role.ANALYST, e);
		}
		try {
			getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId,
							classId,
							Clazz.Role.RESTRICTED.toString(), 
							Campaign.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.RESTRICTED + ", " + Campaign.Role.PARTICIPANT, e);
		}
		
		// Get the list of users in the class.
		List<String> usernames;
		try {
			usernames = userClassQueries.getUsersInClass(classId);
		}
		catch(DataAccessException e) {
			transactionManager.rollback(status);
			throw e;
		}
		
		// For each of the users in the class, assign them their default roles
		// in the campaign.
		for(String username : usernames) {
			List<Campaign.Role> roles;
			try {
				roles = getJdbcTemplate().query(
						SQL_GET_USER_DEFAULT_ROLES, 
						new Object[] { username, campaignId, classId }, 
						new RowMapper<Campaign.Role>() {
							@Override
							public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
								return Campaign.Role.getValue(rs.getString("role"));
							}
						});
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
						username + ", " + campaignId + ", " + classId, e);
			}
			
			for(Campaign.Role role : roles) {
				try {
					getJdbcTemplate().update(
							SQL_INSERT_USER_ROLE_CAMPAIGN, 
							new Object[] { username, campaignId, role.toString() });
				}
				catch(org.springframework.dao.DuplicateKeyException e) {
					// If the user already has the role in the campaign then
					// ignore it.
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
							username + ", " + campaignId + ", " + role, e);
				}
			}
		}
	}
}
