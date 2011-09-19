package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to campaign-class relationships.
 * 
 * @author John Jenkins
 */
public final class CampaignClassDaos extends Dao {
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
	
	private static CampaignClassDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignClassDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves the list of unique identifiers for campaigns that are 
	 * associated with the class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @return A List of campaign identifiers for all of the campaigns 
	 * 		   associated with this class.
	 */
	public static List<String> getCampaignsAssociatedWithClass(String classId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_CLASS,
					new Object[] { classId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_CLASS + "' with parameter: " + classId, e);
		}
	}
	
	/**
	 * Retrieves the list of unique identifiers for all of the classes that are
	 * associated with the campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A list of class IDs for all of the classes associated with this
	 * 		   campaign.
	 */
	public static List<String> getClassesAssociatedWithCampaign(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN, 
					new Object[] { campaignId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Retrieves the list of default campaign roles for a user in a class with
	 * the specified class role.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param classRole The class role.
	 * 
	 * @return A, possibly empty but never null, list of campaign roles.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<CampaignRoleCache.Role> getDefaultCampaignRolesForCampaignClass(String campaignId, String classId, ClassRoleCache.Role classRole) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES,
					new Object[] { campaignId, classId, classRole.toString() },
					new RowMapper<CampaignRoleCache.Role>() {
						@Override
						public CampaignRoleCache.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return CampaignRoleCache.Role.getValue(rs.getString("role"));
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