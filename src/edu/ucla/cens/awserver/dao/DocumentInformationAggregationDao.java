package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.Document;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Takes a list of document IDs and gets information about each document. This
 * includes the document information such as name, description, etc., the
 * specific role that the requesting user has with the document, the campaigns
 * that the user is in and the document is associated with and the document
 * role of that campaign, and the classes that the user is in and the document
 * is associated with and the document role of that class.
 * 
 * @author John Jenkins
 */
public class DocumentInformationAggregationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentInformationAggregationDao.class);
	
	private static final String SQL_GET_DOCUMENT_INFO = "SELECT d.uuid, d.name, d.description, dps.privacy_state, d.last_modified_timestamp, d.size " +
														"FROM document d, document_privacy_state dps " +
														"WHERE d.uuid = ? " +
														"AND d.privacy_state_id = dps.id";
	
	private static final String SQL_GET_USER_DOCUMENT_ROLE = "SELECT dr.role " +
															 "FROM user u, document d, document_user_role dur, document_role dr " +
															 "WHERE d.uuid = ? " +
															 "AND d.id = dur.document_id " +
															 "AND dur.user_id = u.id " +
															 "AND u.login_id = ? " +
															 "AND dur.document_role_id = dr.id";
	
	private static final String SQL_GET_CAMPAIGNS_AND_DOCUMENT_ROLE = "SELECT c.urn, dr.role " +
																	  "FROM campaign c, document d, document_role dr, document_campaign_role dcr, " +
																	  	"user u, user_role_campaign urc " +
																	  "WHERE d.uuid = ? " +
																	  "AND d.id = dcr.document_id " +
																	  "AND dcr.document_role_id = dr.id " +
																	  "AND dcr.campaign_id = c.id " +
																	  "AND urc.campaign_id = c.id " +
																	  "AND urc.user_id = u.id " +
																	  "AND u.login_id = ?";
	
	private static final String SQL_GET_CLASSES_AND_DOCUMENT_ROLE = "SELECT c.urn, dr.role " +
																	"FROM class c, document d, document_role dr, document_class_role dcr, " +
																		"user u, user_class uc " +
																	"WHERE d.uuid = ? " +
																	"AND d.id = dcr.document_id " +
																	"AND dcr.document_role_id = dr.id " +
																	"AND dcr.class_id = c.id " +
																	"AND uc.class_id = c.id " +
																	"AND uc.user_id = u.id " +
																	"AND u.login_id = ?";
	
	public static final String KEY_DOCUMENT_INFORMATION_AGGREGATION_DAO_DOCUMENT_LIST = "document_information_aggregation_dao_document_list";
	
	private String _key;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param key The key to use to get the document ID list from the request.
	 */
	public DocumentInformationAggregationDao(DataSource dataSource, String key) {
		super(dataSource);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
	}

	/**
	 * For each of the document IDs in the list, it gets the information about
	 * the document, the list of document roles that the currently logged in 
	 * user has with the document, the list of classes that the user is in and
	 * the document is associated with and the document role for each class,
	 * and the list of campaigns that the user is in and the document is
	 * associated with and the document role for each campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of document IDs.
		String documentIdList;
		try {
			documentIdList = (String) awRequest.getToReturnValue(_key);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required key in toReturn map: " + _key);
			throw new DataAccessException(e);
		}
		
		// Create the list of Document objects that will be returned.
		List<Document> result = new LinkedList<Document>();
		
		// If the documentIdList is an empty string, then don't attempt to do
		// anything with it because this will cause the split() function to 
		// create an empty String in the array rather than an array of size 0,
		// which will eventually lead to a crash.
		if(! "".equals(documentIdList)) {
			// For each of the document IDs, get the information and the roles
			// for this user.
			String[] documentIdArray = documentIdList.split(InputKeys.LIST_ITEM_SEPARATOR);			
			for(int i = 0; i < documentIdArray.length; i++) {
				// Get the document information and store it in a Document
				// object.
				List<?> currDocumentInfo;
				try {
					currDocumentInfo = getJdbcTemplate().query(SQL_GET_DOCUMENT_INFO, 
							new Object[] { documentIdArray[i] }, 
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									return new Document(rs.getString("uuid"),
														rs.getString("name"),
														rs.getString("description"),
														rs.getString("privacy_state"),
														rs.getTimestamp("last_modified_timestamp"),
														rs.getInt("size"));
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_DOCUMENT_INFO + "' with parameter: " + documentIdArray[i], e);
					throw new DataAccessException(e);
				}
				
				// If the document doesn't exist or there were multiple 
				// documents with the same identifier throw an exception.
				if(currDocumentInfo.size() != 1) {
					_logger.error("We should have found exactly 1 document, but instead we found: " + currDocumentInfo.size());
					throw new DataAccessException("We should have found exactly 1 document, but instead we found: " + currDocumentInfo.size());
				}
				
				// Create the Document object to be returned for this document.
				// This Object is final meaning that it cannot point to a new
				// Object, but it can be manipulated in the query's row 
				// mappers.
				final Document currDocument = (Document) currDocumentInfo.get(0);
				
				// Get the list of roles for this document for the requesting 
				// user and store them in the Document.
				try {
					getJdbcTemplate().query(SQL_GET_USER_DOCUMENT_ROLE, 
							new Object[] { documentIdArray[i], awRequest.getUser().getUserName() }, 
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									currDocument.addUserRole(rs.getString("role"));
									return null;
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USER_DOCUMENT_ROLE + "' with parameters: " + 
							documentIdArray[i] + ", " + awRequest.getUser().getUserName(), e);
					throw new DataAccessException(e);
				}
				
				// Get the list of campaigns and their roles for the requesting
				// user and store it in the Document.
				try {
					getJdbcTemplate().query(SQL_GET_CAMPAIGNS_AND_DOCUMENT_ROLE, 
							new Object[] { documentIdArray[i], awRequest.getUser().getUserName() },
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									currDocument.addCampaignRole(rs.getString("urn"), rs.getString("role"));
									return null;
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGNS_AND_DOCUMENT_ROLE + "' with parameters: " + 
							documentIdArray[i] + ", " + awRequest.getUser().getUserName(), e);
					throw new DataAccessException(e);
				}
				
				// Get the list of classes and their roles for the requesting 
				// user and store it in the Document.
				try {
					getJdbcTemplate().query(SQL_GET_CLASSES_AND_DOCUMENT_ROLE, 
							new Object[] { documentIdArray[i], awRequest.getUser().getUserName() }, 
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									currDocument.addClassRole(rs.getString("urn"), rs.getString("role"));
									return null;
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CLASSES_AND_DOCUMENT_ROLE + "' with parameters: " + 
							documentIdArray[i] + ", " + awRequest.getUser().getUserName(), e);
					throw new DataAccessException(e);
				}
				
				result.add(currDocument);
			}
		}
		
		// Set the results of the query as the result list.
		awRequest.setResultList(result);
	}
}