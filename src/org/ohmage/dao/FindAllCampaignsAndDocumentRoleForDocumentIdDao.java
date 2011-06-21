package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.EntityAndRole;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.RowMapper;

/**
 * Gets all of the campaign IDs and the campaign's document role.
 * 
 * @author John Jenkins
 */
public class FindAllCampaignsAndDocumentRoleForDocumentIdDao extends AbstractDao {
	public static final Logger _logger = Logger.getLogger(FindAllCampaignsAndDocumentRoleForDocumentIdDao.class);
	
	private static final String SQL_GET_CAMPAIGNS_AND_ROLE_FOR_REQUESTING_USER = 
		"SELECT distinct(c.urn), dr.role " +
		"FROM campaign c, document d, document_role dr, document_campaign_role dcar " +
		"WHERE d.uuid = ? " +
		"AND dcar.document_id = d.id " +
		"AND dcar.document_role_id = dr.id " +
		"AND dcar.campaign_id = c.id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public FindAllCampaignsAndDocumentRoleForDocumentIdDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets all of the campaign IDs and its document role and stores it in the
	 * result list of the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of class IDs.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key in toProcess map: " + InputKeys.DOCUMENT_ID, e);
		}
		
		try {
			awRequest.setResultList(getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_AND_ROLE_FOR_REQUESTING_USER, 
					new Object[] { documentId }, 
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new EntityAndRole(rs.getString("urn"), rs.getString("role"));
						}
					}
			));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGNS_AND_ROLE_FOR_REQUESTING_USER + "' with parameters: " +
					documentId, e);
			throw new DataAccessException(e);
		}
	}
}