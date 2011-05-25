package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.cache.DocumentPrivacyStateCache;
import edu.ucla.cens.awserver.cache.DocumentRoleCache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Gets the list of documents that are directly connected to the requesting 
 * user via the document-user table.
 * 
 * @author John Jenkins
 */
public class FindAllDocumentsSpecificToRequestingUserWithRolesDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDocumentsSpecificToRequestingUserWithRolesDao.class);
	
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER =
		"SELECT distinct(d.uuid) " +
		"FROM user u, document d, document_role dr, document_privacy_state dps, document_user_role dur " +
		"WHERE u.login_id = ? " +
		"AND dr.role = ? " +
		"AND dur.document_id = d.id " +
		"AND dur.document_role_id = dr.id " +
		"AND dur.user_id = u.id " +
		"AND (" +
			"(d.privacy_state_id = dps.id " +
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.ROLE_OWNER + "')" + 
		")";
	
	private List<String> _roles;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param roles A list of document roles on which to search.
	 */
	public FindAllDocumentsSpecificToRequestingUserWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles must be non-null and contain at least one element.");
		}
		
		_roles = roles;
	}

	/**
	 * Gets the list of document IDs to which this user is directly associated
	 * and sets them as the result list. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// User a HashSet as it will prevent duplicates on insertion for each
		// role iteration. While this should never happen because document-user
		// relationships should be unique to exactly one role, this way if it
		// ever changes we will still not return duplicate data. Plus insertion
		// into a HashSet is constant-time*.
		Set<String> documentIds = new HashSet<String>();
		for(String role : _roles) {
			List<?> currDocumentIds;
			
			try {
				currDocumentIds = getJdbcTemplate().query(SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER,
													  	  new Object[] { awRequest.getUser().getUserName(), role },
													  	  new SingleColumnRowMapper());
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER + "' with parameters: " + 
						awRequest.getUser().getUserName() + ", " + role, e);
				throw new DataAccessException(e);
			}
			
			ListIterator<?> currDocumentIdsIter = currDocumentIds.listIterator();
			while(currDocumentIdsIter.hasNext()) {
				documentIds.add((String) currDocumentIdsIter.next());
			}
		}
		
		awRequest.setResultList(new ArrayList<String>(documentIds));
	}
}