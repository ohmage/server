package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
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
			return instance.jdbcTemplate.query(
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
			return instance.jdbcTemplate.query(
					SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN, 
					new Object[] { campaignId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CLASSES_ASSOCIATED_WITH_CAMPAIGN + "' with parameter: " + campaignId, e);
		}
	}
}
