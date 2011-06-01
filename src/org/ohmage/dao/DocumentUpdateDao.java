/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.dao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Updates the document based on the request's parameters.
 * 
 * @author John Jenkins
 */
public class DocumentUpdateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentUpdateDao.class);
	
	private static final String SQL_GET_DOCUMENT_ID = "SELECT id " +
													  "FROM document " +
													  "WHERE uuid = ?";
	
	private static final String SQL_GET_CAMPAIGN_ID = "SELECT id " +
													  "FROM campaign " +
													  "WHERE urn = ?";
	
	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn = ?";
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id = ?";
	
	private static final String SQL_GET_URL = "SELECT url " +
											  "FROM document " +
											  "WHERE uuid = ?";
	
	private static final String SQL_INSERT_CAMPAIGN_ROLE = "INSERT INTO document_campaign_role(document_id, campaign_id, document_role_id) " +
														   "VALUES (?, ?, ?)";
	
	private static final String SQL_INSERT_CLASS_ROLE = "INSERT INTO document_class_role(document_id, class_id, document_role_id) " +
														"VALUES (?, ?, ?)";
	
	private static final String SQL_INSERT_USER_ROLE = "INSERT INTO document_user_role(document_id, user_id, document_role_id) " +
													   "VALUES (?, ?, ?)";
	
	private static final String SQL_UPDATE_NAME = "UPDATE document " +
												  "SET name = ?, extension = ? " +
												  "WHERE uuid = ?";
	
	private static final String SQL_UPDATE_DESCRIPTION = "UPDATE document " +
														 "SET description = ? " +
														 "WHERE uuid = ?";
	
	private static final String SQL_UPDATE_PRIVACY_STATE = "UPDATE document " +
														   "SET privacy_state_id = ? " +
														   "WHERE uuid = ?";
	
	private static final String SQL_UPDATE_SIZE = "UPDATE document " +
												  "SET size = ? " +
												  "WHERE uuid = ?";
	
	private static final String SQL_UPDATE_CAMPAIGN_ROLE = "UPDATE document_campaign_role " +
														   "SET document_role_id = ? " +
														   "WHERE document_id = ? " +
														   "AND campaign_id = ?";
	
	private static final String SQL_UPDATE_CLASS_ROLE = "UPDATE document_class_role " +
														"SET document_role_id = ? " +
														"WHERE document_id = ? " +
														"AND class_id = ?";
	
	private static final String SQL_UPDATE_USER_ROLE = "UPDATE document_user_role " +
													   "SET document_role_id = ? " +
													   "WHERE document_id = ? " +
													   "AND user_id = ?";
	
	private static final String SQL_DELETE_CAMPAIGN_ROLE = "DELETE FROM document_campaign_role " +
														   "WHERE document_id = ? " +
														   "AND campaign_id = ?";
	
	private static final String SQL_DELETE_CLASS_ROLE = "DELETE FROM document_class_role " +
														"WHERE document_id = ? " +
														"AND class_id = ?";
	
	private static final String SQL_DELETE_USER_ROLE = "DELETE FROM document_user_role " +
													   "WHERE document_id = ? " +
													   "AND user_id = ?";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when updating or querying the
	 * 					 database.
	 */
	public DocumentUpdateDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Creates a transaction then cycles through all of the different entities
	 * that can be updated and updates them. If anything fails, it rolls back
	 * the transaction and throws an exception.
	 */
	@Override
	public void execute(AwRequest request) {
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Document update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				updateName(request);
				updateDescription(request);
				updatePrivacyState(request);
				
				// Update the campaign-document roles.
				updateEntityRoleList(request, InputKeys.CAMPAIGN_ROLE_LIST_ADD, InputKeys.CAMPAIGN_LIST_REMOVE,
						SQL_GET_CAMPAIGN_ID, SQL_INSERT_CAMPAIGN_ROLE, SQL_UPDATE_CAMPAIGN_ROLE, SQL_DELETE_CAMPAIGN_ROLE);
				// Update the class-document roles.
				updateEntityRoleList(request, InputKeys.CLASS_ROLE_LIST_ADD, InputKeys.CLASS_LIST_REMOVE, 
						SQL_GET_CLASS_ID, SQL_INSERT_CLASS_ROLE, SQL_UPDATE_CLASS_ROLE, SQL_DELETE_CLASS_ROLE);
				// Update the user-document roles.
				updateEntityRoleList(request, InputKeys.USER_ROLE_LIST_ADD, InputKeys.USER_LIST_REMOVE,
						SQL_GET_USER_ID, SQL_INSERT_USER_ROLE, SQL_UPDATE_USER_ROLE, SQL_DELETE_USER_ROLE);
				
				// Update the contents last, so if there are any problems with
				// the other actions, then we fail before we write to the 
				// system.
				updateContents(request);
			}
			catch(IllegalArgumentException e) {
				// Rollback transaction and throw a DataAccessException.
				_logger.error("Error while executing the update.", e);
				transactionManager.rollback(status);
				request.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			catch(CacheMissException e) {
				_logger.error("Error while reading from the cache.", e);
				transactionManager.rollback(status);
				request.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			catch(DataAccessException e) {
				transactionManager.rollback(status);
				request.setFailedRequest(true);
				throw e;
			}
			
			// Commit transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				_logger.error("Error while committing the transaction.", e);
				transactionManager.rollback(status);
				request.setFailedRequest(true);
				throw new DataAccessException(e);
			}
		}
		catch(TransactionException e) {
			_logger.error("Error while rolling back the transaction.", e);
			request.setFailedRequest(true);
			throw new DataAccessException(e);
		}
	}

	/**
	 * Updates the name associated with the document or does nothing if there
	 * is no new name in the request. Also, updates the extension for the file.
	 * 
	 * @param request The request with the new name.
	 */
	private void updateName(AwRequest request) {
		// Get the name if it exists or return if not.
		String newName;
		try {
			newName = (String) request.getToProcessValue(InputKeys.DOCUMENT_NAME);
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		// Get the ID for the document.
		String documentId;
		try {
			documentId = (String) request.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw e;
		}
		
		// Update the document's name.
		String extension = DocumentCreationDao.getExtension(newName);
		try {
			getJdbcTemplate().update(SQL_UPDATE_NAME, new Object[] { newName, extension, documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_NAME + "' with parameters: " + 
					newName + ", " + extension + ", " + documentId,e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Updates the description of the document if one exists in the request.
	 * 
	 * @param request The request potentially containing the new description.
	 */
	private void updateDescription(AwRequest request) {
		// Get the description if it exists or return if not.
		String newDescription;
		try {
			newDescription = (String) request.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		// Get the ID for the document.
		String documentId;
		try {
			documentId = (String) request.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw e;
		}
		
		// Update the document's description.
		try {
			getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { newDescription, documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_DESCRIPTION + "' with parameters: " + newDescription + ", " + documentId,e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Updates the privacy state of the document if one exists in the request.
	 * 
	 * @param request The request containing the new privacy state if one
	 * 				  exists.
	 * 
	 * @throws CacheMissException The privacy state is unknown to the cache.
	 * 							  This should never happen as it has already
	 * 							  been validated.
	 */
	private void updatePrivacyState(AwRequest request) throws CacheMissException {
		// Get the privacy state if it exists or return if not.
		String newPrivacyState;
		try {
			newPrivacyState = (String) request.getToProcessValue(InputKeys.PRIVACY_STATE);
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		// Get the ID for the document.
		String documentId;
		try {
			documentId = (String) request.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw e;
		}
		
		// Update the document's privacy state.
		try {
			getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { DocumentPrivacyStateCache.instance().lookup(newPrivacyState), documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " + 
					DocumentPrivacyStateCache.instance().lookup(newPrivacyState) + ", " + documentId,e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Writes the contents of the file to the output stream if present and
	 * updates the size in the database.
	 * 
	 * @param request The request that may contain the new contents of the
	 * 				  file.
	 */
	private void updateContents(AwRequest request) {
		// Get the new document contents.
		String newContents;
		try {
			newContents = (String) request.getToProcessValue(InputKeys.DOCUMENT);
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		// Get the ID for the document.
		String documentId;
		try {
			documentId = (String) request.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw e;
		}
		
		// Lookup the document's URL.
		String documentUrl;
		try {
			List<?> documentUrlList = getJdbcTemplate().query(SQL_GET_URL, new Object[] { documentId }, new SingleColumnRowMapper());
			
			if(documentUrlList.size() < 1) {
				throw new DataAccessException("Document ID doesn't exist.");
			}
			else if(documentUrlList.size() > 1) {
				throw new DataAccessException("Data integrity violation. Multiple documents have the same ID.");
			}
			else {
				documentUrl = (String) documentUrlList.listIterator().next();
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_URL + "' with parameters: " + documentId, e);
			throw new DataAccessException(e);
		}
		
		// Write the new contents to the document.
		try {
			// TODO: If we are really going to support any URL, then we need to
			// find a way to be able to write to files as well. Currently, we 
			// are only writing to files, so that is what is "hard coded." But,
			// we really need to switch the writer based on the URL.
			FileWriter fileWriter = new FileWriter(new URL(documentUrl).getFile());
			BufferedWriter writer = new BufferedWriter(fileWriter);
			
			writer.write(newContents);
			writer.flush();
			writer.close();
			fileWriter.close();
		} 
		catch (MalformedURLException e) {
			_logger.error("The URL pointing to this resource is broken. This could be a major issue.", e);
			throw new DataAccessException(e);
		} 
		catch (IOException e) {
			_logger.error("There was an error writing to the URL.");
			throw new DataAccessException(e);
		}
		
		// Update the size in the database.
		try {
			getJdbcTemplate().update(SQL_UPDATE_SIZE, new Object[] { newContents.length(), documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_SIZE + "' with parameters: " + newContents.length() + ", " + documentId, e);
			_logger.warn("This has probably resulted in an inconsistancy with what is on the filesystem and what is stored in the database.");
			// We don't thrown an exception because everything else has been
			// updated and the file has been written to disk.
		}
	}
	
	/**
	 * Updates the association between an entity and a document.
	 * 
	 * @param request The request with the list of entities to be added,
	 * 				  updated, or removed.
	 * 
	 * @param addListKey The key to use to get the list of entities to be added
	 * 					 or updated for this document.
	 * 
	 * @param removeListKey The key to use to get the list of entities to be
	 * 						disassociated with this document.
	 * 
	 * @param sqlGetEntityId The SQL to use to get an entity's database ID.
	 * 
	 * @param sqlInsertEntity The SQL to use to add the entity's association
	 * 						  into the database.
	 * 
	 * @param sqlUpdateEntity The SQL to use to update the entity's role with
	 * 						  this role in the database.
	 * 
	 * @param sqlDeleteEntity The SQL to use to delete the entity's association
	 * 						  with this document.
	 * 
	 * @throws CacheMissException Thrown if there is an issue getting the 
	 * 							  database ID for the document.
	 */
	private void updateEntityRoleList(AwRequest request, String addListKey, String removeListKey, 
			String sqlGetEntityId, String sqlInsertEntity, String sqlUpdateEntity, String sqlDeleteEntity) throws CacheMissException {
		// Get the list of campaigns and their roles to add where each pair is
		// an element in the array.
		Map<String, String> addMap = new HashMap<String, String>();
		try {
			String[] addArray = ((String) request.getToProcessValue(addListKey)).split(InputKeys.LIST_ITEM_SEPARATOR);
			
			// This is done if the list is an empty string which causes the
			// split() function to create this useless single-index array.
			if(! ((addArray.length == 1) && ("".equals(addArray[0])))) {
				// The list is non-empty, so transfer the items into the map.
				for(int i = 0; i < addArray.length; i++) {
					String[] entityIdAndRole = addArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
					
					addMap.put(entityIdAndRole[0], entityIdAndRole[1]);
				}
			}
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		// Get the list of campaigns to remove.
		Set<String> removeList = new HashSet<String>();
		try {
			removeList = new HashSet<String>(Arrays.asList(((String) request.getToProcessValue(removeListKey)).split(InputKeys.LIST_ITEM_SEPARATOR)));
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		// If neither is present, abort.
		if((addMap.size() == 0) && (removeList.size() == 0)) {
			return;
		}
		
		// Get the ID for the document.
		String documentId;
		try {
			documentId = (String) request.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw e;
		}
		
		// Get the database ID for the document.
		long documentDbId;
		try {
			documentDbId = getJdbcTemplate().queryForLong(SQL_GET_DOCUMENT_ID, new Object[] { documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_DOCUMENT_ID + "' with parameters: " + documentId, e);
			throw new DataAccessException(e);
		}
		
		// Delete roles
		Iterator<String> removeListIter = removeList.iterator();
		while(removeListIter.hasNext()) {
			// Get the campaign's String ID.
			String entityId = removeListIter.next();
			
			// Get the campaign's database ID.
			long entityDbId;
			try {
				entityDbId = getJdbcTemplate().queryForLong(sqlGetEntityId, new Object[] { entityId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + sqlGetEntityId + "' with parameters: " + entityId, e);
				throw new DataAccessException(e);
			}
			
			// Delete the document-campaign role.
			try {
				getJdbcTemplate().update(sqlDeleteEntity, new Object[] { documentDbId, entityDbId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + sqlDeleteEntity + "' with parameters: " + documentDbId + ", " + entityDbId, e);
				throw new DataAccessException(e);
			}
		}
		
		// Add roles
		Iterator<String> addMapIter = addMap.keySet().iterator();
		while(addMapIter.hasNext()) {
			// Get the campaign's String ID.
			String entityId = addMapIter.next();
			
			// Get the campaign's database ID.
			long entityDbId;
			try {
				entityDbId = getJdbcTemplate().queryForLong(sqlGetEntityId, new Object[] { entityId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + sqlGetEntityId + "' with parameters: " + entityId, e);
				throw new DataAccessException(e);
			}
			
			// Add the document-campaign role.
			try {
				getJdbcTemplate().update(sqlInsertEntity, 
						new Object[] { documentDbId, entityDbId, DocumentRoleCache.instance().lookup(addMap.get(entityId)) });
			}
			catch(org.springframework.dao.DataIntegrityViolationException duplicateEntryException) {
				// If the entity is already associated with the document, then
				// they must be attempting an update.
				try {
					getJdbcTemplate().update(sqlUpdateEntity, 
							new Object[] { DocumentRoleCache.instance().lookup(addMap.get(entityId)), documentDbId, entityDbId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + sqlUpdateEntity + "' with parameters: " + 
							DocumentRoleCache.instance().lookup(addMap.get(entityId)) + ", " + documentDbId + ", " + entityDbId, e);
					throw new DataAccessException(e);
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + sqlInsertEntity + "' with parameters: " + 
						documentDbId + ", " + entityDbId + ", " + DocumentRoleCache.instance().lookup(addMap.get(entityId)), e);
				throw new DataAccessException(e);
			}
		}
	}
}
