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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.ohmage.domain.Clazz;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignClassQueries;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to campaign-class relationships.
 * 
 * @author John Jenkins
 */
public final class CampaignClassQueries extends Query implements ICampaignClassQueries {
	// Retrieves the IDs for all of the campaigns associated with a class.
	private static final String SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_CLASS =
		"SELECT ca.urn " +
		"FROM campaign ca, class cl, campaign_class cc " +
		"WHERE cl.urn = ? " +
		"AND cl.id = cc.class_id " +
		"AND ca.id = cc.campaign_id";
	
	// Retrieves the IDs fro all of the classes associated with a campaign.
	private static final String SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN =
		"SELECT cl.urn " +
		"FROM campaign ca, class cl, campaign_class cc " +
		"WHERE ca.urn = ? " +
		"AND ca.id = cc.campaign_id " +
		"AND cl.id = cc.class_id";
	
	// Retrieves all of the default roles for a campaign-class association 
	// based on some class role.
	private static final String SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES =
		"SELECT ur.role " +
		"FROM campaign ca, class cl, campaign_class cc, user_role ur, user_class_role ucr, campaign_class_default_role ccdr " +
		"WHERE ca.urn = ? " +
		"AND cl.urn = ? " +
		"AND ca.id = cc.campaign_id " +
		"AND cl.id = cc.class_id " +
		"AND cc.id = ccdr.campaign_class_id " +
		"AND ccdr.user_class_role_id = ucr.id " +
		"AND ucr.role = ? " +
		"AND ccdr.user_role_id = ur.id";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignClassQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignClassQueries#getCampaignsAssociatedWithClass(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsAssociatedWithClass(String classId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_CLASS,
					new Object[] { classId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_CLASS + "' with parameter: " + classId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignClassQueries#getClassesAssociatedWithCampaign(java.lang.String)
	 */
	@Override
	public List<String> getClassesAssociatedWithCampaign(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN, 
					new Object[] { campaignId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignClassQueries#getClassesAssociatedWithCampaignList(java.lang.String)
	 */
	@Override
	public Map<String, Collection<String>> getClassesAssociatedWithCampaigns(
			final String subSelectStmt, 
			final Collection<Object> subSelectParameters) 
			throws DataAccessException {
		
		StringBuilder sql = new StringBuilder(
			 	"SELECT ca.urn, cl.urn class_urn " + 
			 	"FROM campaign ca JOIN campaign_class cc on (ca.id = cc.campaign_id) " +
			 	"  JOIN class cl ON (cl.id = cc.class_id) " +
		     	"WHERE ca.id in ");
		sql.append("( " + subSelectStmt + " )");
				
		final Map<String, Collection<String>> campaignClasses = new HashMap<String, Collection<String>>();

		try {			
			getJdbcTemplate().query(
					sql.toString(),
					subSelectParameters.toArray(),
					new RowMapper<Object> () {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {				
					
							String campaignUrn = rs.getString("urn");
							String classUrn = rs.getString("class_urn");
							Collection<String> classes = campaignClasses.get(campaignUrn);
							if (classes == null) {
								classes = new LinkedList<String>();
								campaignClasses.put(campaignUrn, classes);
							}
							classes.add(classUrn);
							return null;
						}
					}
		);
		
		return campaignClasses;
		} 
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + sql + 
					"' with parameters: " + subSelectStmt, 
					e);
		}
	
	}

	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignClassQueries#getDefaultCampaignRolesForCampaignClass(java.lang.String, java.lang.String, org.ohmage.domain.Clazz.Role)
	 */
	@Override
	public List<Campaign.Role> getDefaultCampaignRolesForCampaignClass(String campaignId, String classId, Clazz.Role classRole) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES,
					new Object[] { campaignId, classId, classRole.toString() },
					new RowMapper<Campaign.Role>() {
						@Override
						public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return Campaign.Role.getValue(rs.getString("role"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES + 
					"' with parameters: " + 
						campaignId + ", " +
						classId + ", " +
						classRole + ", ",
					e);
		}
	}
}
