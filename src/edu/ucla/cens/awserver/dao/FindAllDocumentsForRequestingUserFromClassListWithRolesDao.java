package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.cache.ClassRoleCache;
import edu.ucla.cens.awserver.cache.DocumentPrivacyStateCache;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Gets all document IDs or the requesting user for each of the classes in the
 * request with each of the roles in the list.
 * 
 * @author John Jenkins
 */
public class FindAllDocumentsForRequestingUserFromClassListWithRolesDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDocumentsForRequestingUserFromClassListWithRolesDao.class);
	
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER = 
		"SELECT distinct(d.uuid) " +
		"FROM user u, class c, user_class uc, user_class_role ucr, " +
			"document d, document_role dr, document_privacy_state dps, document_class_role dclr " +
		"WHERE u.login_id = ? " +
		"AND dr.role = ? " +
		"AND c.urn = ? " +
		"AND dclr.document_id = d.id " +
		"AND dclr.document_role_id = dr.id " + 
		"AND dclr.class_id = c.id " +
		"AND dclr.class_id = uc.class_id " +
		"AND uc.user_id = u.id " +
		"AND uc.user_class_role_id = ucr.id " +
		"AND (" +
			"(d.privacy_state_id = dps.id " +
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
			" OR " +
			"(ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "')" +
		")";
	
	private List<String> _roles;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param roles The list of roles to use when specifying which document IDs
	 * 				to retrieve.
	 */
	public FindAllDocumentsForRequestingUserFromClassListWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles cannot be null or whitespace only.");
		}
		
		_roles = roles;
	}
	
	/**
	 * Gets the list of documents for the requesting user, with any of the 
	 * specified roles, in each of the classes in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of class IDs.
		String classIdList;
		try {
			classIdList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key in toProcess map: " + InputKeys.CLASS_URN_LIST, e);
		}
		String[] classIdArray = classIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		Set<String> documentIds = new HashSet<String>();
		// For each of the roles,
		for(String role : _roles) {
			// For each of the classes,
			for(int i = 0; i < classIdArray.length; i++) {
				// Get the document IDs for this user with the role for the
				// current class.
				List<?> currDocumentIds;
				try {
					currDocumentIds = getJdbcTemplate().query(SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER, 
															  new Object[] { awRequest.getUser().getUserName(), role, classIdArray[i] }, 
															  new SingleColumnRowMapper());
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER + "' with parameters: " +
							awRequest.getUser().getUserName() + ", " + role + ", " + classIdArray[i], e);
					throw new DataAccessException(e);
				}
				
				// Add the new document IDs to the list.
				ListIterator<?> currDocumentIdsIter = currDocumentIds.listIterator();
				while(currDocumentIdsIter.hasNext()) {
					documentIds.add((String) currDocumentIdsIter.next());
				}
			}
		}
		
		awRequest.setResultList(new ArrayList<String>(documentIds));
	}
}