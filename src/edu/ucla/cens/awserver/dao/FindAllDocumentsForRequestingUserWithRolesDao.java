package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.cache.CampaignRoleCache;
import edu.ucla.cens.awserver.cache.ClassRoleCache;
import edu.ucla.cens.awserver.cache.DocumentPrivacyStateCache;
import edu.ucla.cens.awserver.cache.DocumentRoleCache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Gets all the document IDs for the requesting user where they have the
 * parameterized roles with the document.
 * 
 * @author John Jenkins
 */
public class FindAllDocumentsForRequestingUserWithRolesDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDocumentsForRequestingUserWithRolesDao.class);
	
	private static final String SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE =
			"SELECT distinct(d.uuid) " +
			"FROM user u, user_class uc, user_class_role ucr, user_role ur, user_role_campaign urc, " +
				"document d, document_role dr, document_privacy_state dps, " +
				"document_campaign_role dcar, document_class_role dclr, document_user_role dur " +
			// For the requesting user,
			"WHERE u.login_id = ? " +
			// For a given document role,
			"AND dr.role = ? " +
			"AND (" +
				// For each of the documents directly associated with this user
				// via the users table,
				"(dur.document_id = d.id " +
				"AND dur.document_role_id = dr.id " +
				"AND dur.user_id = u.id " +
				"AND (" +
					// The document must be shared or the user must be an
					// owner.
					"(d.privacy_state_id = dps.id " +
					"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
					" OR " +
					"(dr.role = '" + DocumentRoleCache.ROLE_OWNER + "')" + 
				"))" +
				" OR " +
				// For each of the documents associated with this user via the
				// document-campaign table,
				"(dcar.document_id = d.id " +
				"AND dcar.document_role_id = dr.id " +
				"AND dcar.campaign_id = urc.campaign_id " +
				"AND urc.user_id = u.id " +
				"AND urc.user_role_id = ur.id " +
				"AND (" +
					// The user must be a supervisor in the class or the
					// privacy state must be shared and the user be an analyst
					// or an author.
					"(ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "')" +
					" OR " +
					"(d.privacy_state_id = dps.id " +
					"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "' " +
					"AND " +
						"(ur.role = '" + CampaignRoleCache.ROLE_ANALYST + "')" +
						" OR " +
						"(ur.role = '" + CampaignRoleCache.ROLE_AUTHOR + "')" +
					")" +
				"))" +
				" OR " +
				// For each of the documents associated with this user via the
				// document-class table,
				"(dclr.document_id = d.id " +
				"AND dclr.document_role_id = dr.id " + 
				"AND dclr.class_id = uc.class_id " +
				"AND uc.user_id = u.id " +
				"AND uc.user_class_role_id = ucr.id " +
				"AND (" +
					// The user must be privileged in the class or the document
					// must be shared.
					"(d.privacy_state_id = dps.id " +
					"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
					" OR " +
					"(ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "')" +
				"))" +
			")";
				
	private List<String> _roles;

	/**
	 * Sets up this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param roles The list of document roles to query for. It is recommended,
	 * 				although not required, to use the constants defined in the
	 * 				document's role cache.
	 */
	public FindAllDocumentsForRequestingUserWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The roles list cannot be null or empty.");
		}
		
		_roles = roles;
	}
	
	/**
	 * Gets all the documents for each of the roles and then aggregates them
	 * into a list and puts them in the request's result list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Create a HashSet that will prevent duplicates as the set of IDs.
		HashSet<String> documentIdsSet = new HashSet<String>();
		
		// Go through the list of document roles on which we are querying.
		ListIterator<String> rolesIter = _roles.listIterator();
		while(rolesIter.hasNext()) {
			String role = rolesIter.next();
			
			// For this role, get all the document IDs.
			List<?> documentIds;
			try {
				documentIds = getJdbcTemplate().query(SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE, 
													  new Object[] { awRequest.getUser().getUserName(), role }, 
													  new SingleColumnRowMapper());
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE + "' with parameters: " +
						awRequest.getUser().getUserName() + ", " + role);
				throw new DataAccessException(e);
			}
			
			// For all the new document IDs, add them to the set that won't
			// allow duplicates.
			ListIterator<?> documentIdsIter = documentIds.listIterator();
			while(documentIdsIter.hasNext()) {
				String documentId = (String) documentIdsIter.next();
				
				documentIdsSet.add(documentId);
			}
		}
		
		awRequest.setResultList(new ArrayList<String>(documentIdsSet));
	}
}