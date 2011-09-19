package org.ohmage.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting documents. While it may read information pertaining 
 * to other entities, the information it takes and provides should pertain to 
 * documents only with the exception of linking other entities to documents  
 * such as document creation which must read the IDs of other users, classes, 
 * and campaigns in order to associate them with this document all in a single
 * transaction.
 * 
 * @author John Jenkins
 */
public class DocumentDaos extends Dao {
	private static final Logger LOGGER = Logger.getLogger(DocumentDaos.class);
	
	// Retrieves a boolean representing whether or not a document exists in the
	// database.
	private static final String SQL_EXISTS_DOCUMENT =
		"SELECT EXISTS(" +
			"SELECT uuid " +
			"FROM document " +
			"WHERE uuid = ?" +
		")";
	
	// Retrieves a document's URL.
	private static final String SQL_GET_DOCUMENT_URL = 
		"SELECT url " +
		"FROM document " +
		"WHERE uuid = ?";
	
	// Retrieves a document's name.
	private static final String SQL_GET_DOCUMENT_NAME = 
		"SELECT name " +
		"FROM document " +
		"WHERE uuid = ?";
	
	// Gets a document's information.
	private static final String SQL_GET_DOCUMENT_INFO = 
		"SELECT d.uuid, d.name, d.description, dps.privacy_state, d.last_modified_timestamp, d.creation_timestamp, d.size, duc.username " +
		"FROM document d, document_privacy_state dps, document_user_creator duc " +
		"WHERE d.uuid = ? " +
		"AND d.privacy_state_id = dps.id " +
		"AND d.id = duc.document_id";
	
	// Inserts the document into the database.
	private static final String SQL_INSERT_DOCUMENT = 
		"INSERT INTO document(uuid, name, description, extension, url, size, privacy_state_id, creation_timestamp) " +
		"VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM document_privacy_state WHERE privacy_state = ?), now())";
	
	// Associates a static user string as the creator of a document.
	private static final String SQL_INSERT_DOCUMENT_USER_CREATOR = 
		"INSERT INTO document_user_creator(document_id, username) " +
		"VALUES ((SELECT id FROM document WHERE uuid = ?), ?)";
	
	// Associates a user with a document and gives them a specific role.
	private static final String SQL_INSERT_USER_ROLE = 
		"INSERT INTO document_user_role(document_id, user_id, document_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM document " +
				"WHERE uuid = ?" +
			"), (" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM document_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Associates a campaign with a document and gives it a specific role.
	private static final String SQL_INSERT_CAMPAIGN_ROLE = 
		"INSERT INTO document_campaign_role(document_id, campaign_id, document_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM document " +
				"WHERE uuid = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM document_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Associates a class with a document and gives it a specific role.
	private static final String SQL_INSERT_CLASS_ROLE = 
		"INSERT INTO document_class_role(document_id, class_id, document_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM document " +
				"WHERE uuid = ?" +
			"), (" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM document_role " +
				"WHERE role = ?" +
			")" +
		")";

	private static final String SQL_UPDATE_NAME = 
		"UPDATE document " +
		"SET name = ?, extension = ? " +
		"WHERE uuid = ?";

	private static final String SQL_UPDATE_DESCRIPTION = 
		"UPDATE document " +
		"SET description = ? " +
		"WHERE uuid = ?";

	private static final String SQL_UPDATE_PRIVACY_STATE = 
		"UPDATE document " +
		"SET privacy_state_id = (" +
			"SELECT id " +
			"FROM document_privacy_state " +
			"WHERE privacy_state = ?" +
		") " +
		"WHERE uuid = ?";

	private static final String SQL_UPDATE_SIZE = 
		"UPDATE document " +
		"SET size = ? " +
		"WHERE uuid = ?";

	private static final String SQL_UPDATE_CAMPAIGN_ROLE = 
		"UPDATE document_campaign_role " +
		"SET document_role_id = (" +
			"SELECT id " +
			"FROM document_role " +
			"WHERE role = ?" +
		") " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";

	private static final String SQL_UPDATE_CLASS_ROLE = 
		"UPDATE document_class_role " +
		"SET document_role_id = (" +
			"SELECT id " +
			"FROM document_role " +
			"WHERE role = ?" +
		") " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";

	private static final String SQL_UPDATE_USER_ROLE = 
		"UPDATE document_user_role " +
		"SET document_role_id = (" +
			"SELECT id " +
			"FROM document_role " +
			"WHERE role = ?" +
		") " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	private static final String SQL_DELETE_DOCUMENT = 
		"DELETE FROM document " +
		"WHERE uuid = ?";

	private static final String SQL_DELETE_CAMPAIGN_ROLE = 
		"DELETE FROM document_campaign_role " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";

	private static final String SQL_DELETE_CLASS_ROLE = 
		"DELETE FROM document_class_role " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";

	private static final String SQL_DELETE_USER_ROLE = 
		"DELETE FROM document_user_role " +
		"WHERE document_id = (" +
			"SELECT id " +
			"FROM document " +
			"WHERE uuid = ?" +
		") " +
		"AND user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	private static final String DOCUMENT_DIRECTORY_PATTERN_STRING = "[0-9]+";
	private static final Pattern DOCUMENT_DIRECTORY_PATTERN = Pattern.compile(DOCUMENT_DIRECTORY_PATTERN_STRING);

	private static final Lock DIRECTORY_CREATION_LOCK = new ReentrantLock();
	
	private static final int MAX_EXTENSION_LENGTH = 12;
	
	/**
	 * Filters the subdirectories in a directory to only return those that
	 * match the regular expression matcher for directories.
	 * 
	 * @author Joshua Selsky
	 */
	private static final class DirectoryFilter implements FilenameFilter {
		/**
		 * Returns true iff the filename is appropriate for the regular
		 * expression.
		 */
		public boolean accept(File f, String name) {
			return DOCUMENT_DIRECTORY_PATTERN.matcher(name).matches();
		}
	}
	
	// The current directory to which the next document should be saved.
	private static File currLeafDirectory;
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static DocumentDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private DocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Creates a new document entry in the database. It saves the file to disk
	 * and the database entry contains a reference to that file.
	 * 
	 * @param contents The contents of the file.
	 * 
	 * @param name The name of the file.
	 * 
	 * @param description A description for the file.
	 * 
	 * @param privacyState The initial privacy state of the file.
	 * 
	 * @param campaignRoleMap A Map of campaign IDs to document roles for which
	 * 						  this document will have an initial association.
	 * 
	 * @param classRoleMap A Map of class IDs to document roles for which this
	 * 					   document will have an initial association.
	 * 
	 * @param creatorUsername The username of the creator of this document.
	 * 
	 * @return Returns a unique identifier for this document.
	 */
	public static String createDocument(byte[] contents, String name, String description, DocumentPrivacyStateCache.PrivacyState privacyState, 
			Map<String, DocumentRoleCache.Role> campaignRoleMap, Map<String, DocumentRoleCache.Role> classRoleMap, String creatorUsername) 
		throws DataAccessException {
		
		// Create a new, random UUID to use to save this file.
		String uuid = UUID.randomUUID().toString();
		
		// getDirectory() is used as opposed to accessing the current leaf
		// directory class variable as it will do sanitation in case it hasn't
		// been initialized or is full.
		File documentDirectory = getDirectory();
		File newFile = new File(documentDirectory.getAbsolutePath() + "/" + uuid);
		String url = "file://" + newFile.getAbsolutePath();
		
		// Write the document to the file system.
		try {
			FileOutputStream os = new FileOutputStream(newFile);
			os.write(contents);
			os.flush();
			os.close();
		}
		catch(IOException e) {
			throw new DataAccessException("Error writing the new document to the system.", e);
		}
		long fileLength = newFile.length();
		
		// Parse the name and get the extension.
		String extension = getExtension(name);
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the file in the DB.
			try {
				instance.getJdbcTemplate().update(
						SQL_INSERT_DOCUMENT, 
						new Object[] { 
								uuid, 
								name, 
								description, 
								extension, 
								url, 
								fileLength, 
								privacyState.toString()
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_DOCUMENT + "' with parameters: " +
						uuid + ", " + name + ", " + description + ", " + extension + ", " + url + ", " + fileLength + ", " + privacyState, e);
			}
			
			// Insert the creator in the DB.
			try {
				instance.getJdbcTemplate().update(
						SQL_INSERT_DOCUMENT_USER_CREATOR, 
						new Object[] { 
								uuid, 
								creatorUsername
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_DOCUMENT_USER_CREATOR + "' with parameters: " +
						uuid + ", " + creatorUsername, e);
			}
			
			// Insert this user's user-role in the DB.
			try {
				instance.getJdbcTemplate().update(
						SQL_INSERT_USER_ROLE, 
						new Object[] { 
								uuid, 
								creatorUsername, 
								DocumentRoleCache.Role.OWNER.toString()
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE + "' with parameters: " +
						uuid + ", " + creatorUsername + ", " + DocumentRoleCache.Role.OWNER, e);
			}
			
			// Insert any campaign associations in the DB.
			if(campaignRoleMap != null) {
				for(String campaignId : campaignRoleMap.keySet()) {
					// Attempt to insert it into the database.
					try {
						instance.getJdbcTemplate().update(
								SQL_INSERT_CAMPAIGN_ROLE, 
								new Object[] { 
										uuid, 
										campaignId, 
										campaignRoleMap.get(campaignId).toString()
								}
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						newFile.delete();
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_ROLE + "' with parameters: " + 
								uuid + ", " + campaignId + ", " + campaignRoleMap.get(campaignId), e);
					}
				}
			}
			
			// Insert any class associations in the DB.
			if(classRoleMap != null) {
				for(String classId : classRoleMap.keySet()) {
					// Attempt to insert it into the database.
					try {
						instance.getJdbcTemplate().update(
								SQL_INSERT_CLASS_ROLE, 
								new Object[] { 
										uuid, 
										classId, 
										classRoleMap.get(classId).toString()
								}
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						newFile.delete();
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CLASS_ROLE + "' with parameters: " + 
								uuid + ", " + classId + ", " + classRoleMap.get(classId), e);
					}
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
			
			return uuid;
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Returns whether or not a document with the unique document identifier
	 * 'documentId' exists.
	 * 
	 * @param documentId The unique document identifier of a document.
	 * 
	 * @return Returns true if the document exists and false otherwise.
	 */
	public static boolean getDocumentExists(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_EXISTS_DOCUMENT, new Object[] { documentId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_DOCUMENT + "' with parameter: " + documentId, e);
		}
	}
	
	/**
	 * Returns the URL of the document.
	 * 
	 * @param documentId The unique document identifier of the document in 
	 * 					 question.
	 * 
	 * @return Returns the URL of the document.
	 */
	public static String getDocumentUrl(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_DOCUMENT_URL, new Object[] { documentId }, String.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENT_URL + "' with parameter: " + documentId, e);
		}
	}
	
	/**
	 * Returns the name of the document.
	 * 
	 * @param documentId The unique document identifier of the document in 
	 * 					 question.
	 * 
	 * @return Returns the name of the document.
	 */
	public static String getDocumentName(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_DOCUMENT_NAME, new Object[] { documentId }, String.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENT_NAME + "' with parameter: " + documentId, e);
		}
	}
	
	/**
	 * Retrieves the information about a document whose ID is 'documentId'.
	 * 
	 * @param documentId The unique document ID for the document whose 
	 * 					 information is desired.
	 *  
	 * @return A DocumentInformation object representing the information about
	 * 		   this document.
	 */
	public static DocumentInformation getDocumentInformation(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
				SQL_GET_DOCUMENT_INFO, 
				new Object[] { documentId }, 
				new RowMapper<DocumentInformation>() {
					@Override
					public DocumentInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new DocumentInformation(
								rs.getString("uuid"),
								rs.getString("name"),
								rs.getString("description"),
								DocumentPrivacyStateCache.PrivacyState.getValue(rs.getString("privacy_state")),
								rs.getTimestamp("last_modified_timestamp"),
								rs.getTimestamp("creation_timestamp"),
								rs.getInt("size"),
								rs.getString("username"));
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENT_INFO + "' with parameter: " + documentId, e);
		}
	}
	
	/**
	 * Updates a document. The 'documentId' cannot be null as this is used to
	 * indicate which document is being updated, but the remaining parameters
	 * may all be null.
	 * 
	 * @param documentId The unique identifier for the document to be updated.
	 * 
	 * @param contents The new contents of the document.
	 * 
	 * @param name The new name of the document.
	 * 
	 * @param description The new description for the document.
	 * 
	 * @param privacyState The new privacy state for the document.
	 * 
	 * @param campaignAndRolesToAdd A Map of campaign IDs to document roles 
	 * 								where the document should be associated to
	 * 								the campaign with the given role or, if
	 * 								already associated, should have its role
	 * 								updated with the new role.
	 * 
	 * @param campaignsToRemove A List of campaign IDs that should no longer be
	 * 							associated with this document.
	 * 
	 * @param classAndRolesToAdd A Map of class IDs to document roles where the
	 * 							 document should be associated to the class 
	 * 							 with the given role or, if already associated,
	 * 							 should have its role updated with the new 
	 * 							 role.
	 * 
	 * @param classesToRemove A List of class IDs that should no longer be
	 * 						  associated with this document.
	 * 
	 * @param userAndRolesToAdd A Map of usernames to document roles where the
	 * 							document should be associated to the user with
	 * 							the given role or, if already associated, 
	 * 							should have its role updated with the new role.
	 * 
	 * @param usersToRemove A List of usernames that should no longer be 
	 * 						associated with this document.
	 */
	public static void updateDocument(String documentId, byte[] contents, String name, String description, 
			DocumentPrivacyStateCache.PrivacyState privacyState,
			Map<String, DocumentRoleCache.Role> campaignAndRolesToAdd, List<String> campaignsToRemove, 
			Map<String, DocumentRoleCache.Role> classAndRolesToAdd, List<String> classesToRemove, 
			Map<String, DocumentRoleCache.Role> userAndRolesToAdd, List<String> usersToRemove) throws DataAccessException {
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Document update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				updateName(documentId, name);
				updateDescription(documentId, description);
				updatePrivacyState(documentId, privacyState);
				
				// Update the campaign-document roles.
				updateEntityRoleList(documentId, campaignAndRolesToAdd, campaignsToRemove,
						SQL_INSERT_CAMPAIGN_ROLE, SQL_UPDATE_CAMPAIGN_ROLE, SQL_DELETE_CAMPAIGN_ROLE);
				// Update the class-document roles.
				updateEntityRoleList(documentId, classAndRolesToAdd, classesToRemove, 
						SQL_INSERT_CLASS_ROLE, SQL_UPDATE_CLASS_ROLE, SQL_DELETE_CLASS_ROLE);
				// Update the user-document roles.
				updateEntityRoleList(documentId, userAndRolesToAdd, usersToRemove,
						SQL_INSERT_USER_ROLE, SQL_UPDATE_USER_ROLE, SQL_DELETE_USER_ROLE);
				
				// Update the contents last, so if there are any problems with
				// the other actions, then we fail before we write to the 
				// system.
				updateContents(documentId, contents);
			}
			catch(IllegalArgumentException e) {
				// Rollback transaction and throw a DataAccessException.
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing the update.", e);
			}
			catch(CacheMissException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while reading from the cache.", e);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw e;
			}
			
			// Commit transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while rolling back the transaction.", e);
		}
	}
	
	/**
	 * Deletes a document.
	 * 
	 * @param documentId The unique identifier for the document to be deleted.
	 */
	public static void deleteDocument(String documentId) throws DataAccessException {
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Document delete.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			String documentUrl = getDocumentUrl(documentId);
			
			try {
				instance.getJdbcTemplate().update(SQL_DELETE_DOCUMENT, new Object[] { documentId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_DELETE_DOCUMENT + "' with parameter: " + documentId, e);
			}
			
			try {
				if(! (new File((new URL(documentUrl)).getFile())).delete()) {
					LOGGER.warn("The document no longer existed, so the deletion only removed the entry from the database.");
				}
			}
			catch(MalformedURLException e) {
				LOGGER.warn("The URL was malformed, meaning that we couldn't have referenced the file anyway. Cannot delete the file.", e);
			}
			catch(SecurityException e) {
				LOGGER.warn("Failed to delete the file because the security manager stopped us. Are we attempting to delete a file that isn't part of the heirarchy?", e);
			}
			
			// Commit transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while rolling back the transaction.", e);
		}
	}
	
	/**
	 * Updates the name associated with the document or does nothing if the
	 * name is null. Also, updates the extension for the file.
	 * 
	 * @param documentId The unique identifier for the document whose name is
	 * 					 being updated.
	 * 
	 * @param name The new name for the document with an extension.
	 */
	private static void updateName(String documentId, String name) throws DataAccessException {
		if(name == null) {
			return;
		}
		
		// Update the document's name.
		String extension = getExtension(name);
		try {
			instance.getJdbcTemplate().update(SQL_UPDATE_NAME, new Object[] { name, extension, documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			errorExecutingSql(SQL_UPDATE_NAME, e, name, extension, documentId);
		}
	}
	
	/**
	 * Updates the description of the document or does nothing if the 
	 * description is null.
	 * 
	 * @param documentId The unique identifier for the document whose name is
	 * 					 being updated.
	 * 
	 * @param description The new description for the document.
	 */
	private static void updateDescription(String documentId, String description) throws DataAccessException {
		if(description == null) {
			return;
		}
		
		// Update the document's description.
		try {
			instance.getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { description, documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			errorExecutingSql(SQL_UPDATE_DESCRIPTION, e, description, documentId);
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
	private static void updatePrivacyState(String documentId, DocumentPrivacyStateCache.PrivacyState privacyState) throws CacheMissException, DataAccessException {
		if(privacyState == null) {
			return;
		}
		
		// Update the document's privacy state.
		try {
			instance.getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { privacyState.toString(), documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " +
					privacyState + ", " + documentId, e);
		}
	}
	
	/**
	 * Updates the file's size in the database and updates the contents on the
	 * disk. If the contents is null, nothing happens.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @param contents The new contents of the document.
	 */
	private static void updateContents(String documentId, byte[] contents) throws DataAccessException {
		if(contents == null) {
			return;
		}
		
		// Lookup the document's URL.
		String documentUrl = getDocumentUrl(documentId);
		
		// Update the size in the database.
		try {
			instance.getJdbcTemplate().update(SQL_UPDATE_SIZE, new Object[] { contents.length, documentId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_SIZE + "' with parameters: " + contents.length + ", " + documentId, e);
		}
		
		// Write the new contents to the document.
		try {
			// TODO: If we are really going to support any URL, then we need to
			// find a way to be able to write to URLs as well. Currently, we 
			// are only writing to files, so that is what is "hard coded." But,
			// we really need to switch the writer based on the URL.
			FileOutputStream fileOutputStream = new FileOutputStream(new URL(documentUrl).getFile());
			fileOutputStream.write(contents);
			fileOutputStream.flush();
			fileOutputStream.close();
		} 
		catch(MalformedURLException e) {
			throw new DataAccessException("The URL pointing to this resource is broken. This could be a major issue.", e);
		} 
		catch(FileNotFoundException e) {
			throw new DataAccessException("The file that this URL points to is missing. This could be a major issue.", e);
		}
		catch(SecurityException e) {
			throw new DataAccessException("We don't have permissions to open a connection or write to a document.", e);
		}
		catch (IOException e) {
			throw new DataAccessException("There was an error writing to the URL.", e);
		}
	}
	
	/**
	 * Updates the association between an group of entities and the document
	 * that is being updated.
	 * 
	 * @param documentId The unique identifier for the document that is being
	 * 					 updated.
	 * 
	 * @param entityAndRolesToAdd A Map of entity IDs to document roles that
	 * 							  should be associated with the document or, if
	 * 							  already associated should have their role
	 * 							  updated. These entities should all be of the
	 * 							  same type (user, class, campaign, etc.) and
	 * 							  should be the same as the types in 
	 * 							  'entitiesToRemove'.
	 * 
	 * @param entitiesToRemove A List of entity IDs that should no longer be
	 * 						   associated with the campaign. These entities
	 * 						   should all be of the same type (user, class,
	 * 						   campaign, etc.) and should be the same as the
	 * 						   types in 'entityAndRolesToAdd'.
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
	private static void updateEntityRoleList(String documentId, 
			Map<String, DocumentRoleCache.Role> entityAndRolesToAdd, 
			List<String> entitiesToRemove, 
			String sqlInsertEntity, String sqlUpdateEntity, String sqlDeleteEntity) 
	throws CacheMissException, DataAccessException {

		// Delete roles
		if(entitiesToRemove != null) {
			Iterator<String> removeListIter = entitiesToRemove.iterator();
			while(removeListIter.hasNext()) {
				// Get the campaign's String ID.
				String entityId = removeListIter.next();
				
				// Delete the document-campaign role.
				try {
					instance.getJdbcTemplate().update(sqlDeleteEntity, new Object[] { documentId, entityId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + sqlDeleteEntity + "' with parameters: " + documentId + ", " + entityId, e);
				}
			}
		}
		
		// Add roles
		if(entityAndRolesToAdd != null) {
			Iterator<String> addMapIter = entityAndRolesToAdd.keySet().iterator();
			while(addMapIter.hasNext()) {
				// Get the entity's String ID.
				String entityId = addMapIter.next();
				
				// Get the entity's role.
				DocumentRoleCache.Role role = entityAndRolesToAdd.get(entityId);
				
				// Add the document-entity role.
				try {
					instance.getJdbcTemplate().update(sqlInsertEntity, 
							new Object[] { documentId, entityId, role.toString() });
				}
				catch(org.springframework.dao.DataIntegrityViolationException duplicateEntryException) {
					// If the entity is already associated with the document, then
					// they must be attempting an update.
					try {
						instance.getJdbcTemplate().update(sqlUpdateEntity, new Object[] { role.toString(), documentId, entityId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						throw new DataAccessException("Error executing SQL '" + sqlUpdateEntity + "' with parameters: " + 
								role + ", " + documentId + ", " + entityId, e);
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + sqlInsertEntity + "' with parameters: " + 
							documentId + ", " + entityId + ", " + role, e);
				}
			}
		}
	}
	
	/**
	 * Gets the extension on the file. To be used by other classes that want to
	 * parse the extension from a filename.
	 * 
	 * @param name The name of the file including the extension.
	 * 
	 * @return The extension on the file with the name 'name'.
	 */
	private static String getExtension(String name) {
		String[] parsedName = name.split("\\.");
		String extension = null;
		if((parsedName.length > 1) && (parsedName[parsedName.length - 1].length() <= MAX_EXTENSION_LENGTH)) {
			extension = parsedName[parsedName.length - 1];
		}
		
		return extension;
	}
	
	/**
	 * Gets the directory to which a new file should be saved. This should be
	 * used instead of accessing the class-level variable directly as it
	 * handles the creation of new folders and the checking that the current
	 * folder is not full.
	 * 
	 * @return A File object for where a document should be written.
	 */
	private static File getDirectory() throws DataAccessException {
		// Get the maximum number of items in a directory.
		int numFilesPerDirectory;
		try {
			numFilesPerDirectory = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY));
		}
		catch(CacheMissException e) {
			throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY, e);
		}
		catch(NumberFormatException e) {
			throw new DataAccessException("Stored value for key '" + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY + "' is not decodable as a number.", e);
		}
		
		// If the leaf directory was never initialized, then we should do
		// that. Note that the initialization is dumb in that it will get to
		// the end of the structure and not check to see if the leaf node is
		// full.
		if(currLeafDirectory == null) {
			init(numFilesPerDirectory);
		}
		
		File[] documents = currLeafDirectory.listFiles();
		// If the _currLeafDirectory directory is full, traverse the tree and
		// find a new directory.
		if(documents.length >= numFilesPerDirectory) {
			getNewDirectory(numFilesPerDirectory);
		}
		
		return currLeafDirectory;
	}
	
	/**
	 * Initializes the directory structure by drilling down to the leaf
	 * directory with each step choosing the directory with the largest
	 * integer value.
	 */
	private static void init(int numFilesPerDirectory) throws DataAccessException {
		// Get the lock.
		DIRECTORY_CREATION_LOCK.lock();
		
		try {
			// If the current leaf directory has been set, we weren't the
			// first to call init(), so we can just unlock the lock and back
			// out.
			if(currLeafDirectory != null) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_DOCUMENT_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DIRECTORY, e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DataAccessException("The root file doesn't exist suggesting an incomplete installation: " + rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DataAccessException("The root file isn't a directory.");
			}
			
			// Get the number of folders deep that documents are stored.
			int fileDepth;
			try {
				fileDepth = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_FILE_HIERARCHY_DEPTH));
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_FILE_HIERARCHY_DEPTH, e);
			}
			catch(NumberFormatException e) {
				throw new DataAccessException("Stored value for key '" + PreferenceCache.KEY_FILE_HIERARCHY_DEPTH + "' is not decodable as a number.", e);
			}
			
			DirectoryFilter directoryFilter = new DirectoryFilter();
			File currDirectory = rootDirectory;
			for(int currDepth = 0; currDepth < fileDepth; currDepth++) {
				// Get the list of directories in the current directory.
				File[] currDirectories = currDirectory.listFiles(directoryFilter);
				
				// If there aren't any, create the first subdirectory in this
				// directory.
				if(currDirectories.length == 0) {
					String newFolderName = directoryNameBuilder(0, numFilesPerDirectory);
					currDirectory = new File(currDirectory.getAbsolutePath() + "/" + newFolderName);
					currDirectory.mkdir();
				}
				// If the directory is overly full, step back up in the
				// structure. This should never happen, as it indicates that
				// there is an overflow in the structure.
				else if(currDirectories.length > numFilesPerDirectory) {
					LOGGER.warn("Too many subdirectories in: " + currDirectory.getAbsolutePath());
					
					// Take a step back in our depth.
					currDepth--;
					
					// If, while backing up the tree, we back out of the root
					// directory, we have filled up the space.
					if(currDepth < 0) {
						throw new DataAccessException("Document structure full!");
					}

					// Get the next parent and the current directory to it.
					int nextDirectoryNumber = Integer.decode(currDirectory.getName()) + 1;
					currDirectory = new File(currDirectory.getParent() + "/" + nextDirectoryNumber);
					
					// If the directory already exists, then there is either a
					// concurrency issue or someone else is adding files.
					// Either way, this shouldn't happen.
					if(currDirectory.exists()) {
						LOGGER.error("Somehow the 'new' directory already exists. This should be looked into: " + currDirectory.getAbsolutePath());
					}
					// Otherwise, create the directory.
					else {
						currDirectory.mkdir();
					}
				}
				// Drill down to the directory with the largest, numeric value.
				else {
					currDirectory = getLargestSubfolder(currDirectories);
				}
			}
			
			// After we have found a suitable directory, set it.
			currLeafDirectory = currDirectory;
		}
		catch(SecurityException e) {
			throw new DataAccessException("The current process doesn't have sufficient permiossions to create new directories.", e);
		}
		finally {
			// No matter what happens, unlock the lock.
			DIRECTORY_CREATION_LOCK.unlock();
		}
	}
	
	/**
	 * Locks the creation lock and checks again that the current leaf
	 * directory is full. If it is not, then it will just back out under the
	 * impression someone else made the change. If it is, it will go up and
	 * down the directory tree structure to find a new leaf node in which to
	 * store new files.
	 * 
	 * @param numFilesPerDirectory The maximum allowed number of files in a
	 * 							   leaf directory and the maximum allowed
	 * 							   number of directories in the branches.
	 */
	private static void getNewDirectory(int numFilesPerDirectory) throws DataAccessException {
		// Get the lock.
		DIRECTORY_CREATION_LOCK.lock();
		
		try {
			// Make sure that this hasn't changed because another thread may
			// have preempted us and already changed the current leaf
			// directory.
			File[] files = currLeafDirectory.listFiles();
			if(files.length < numFilesPerDirectory) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_DOCUMENT_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DIRECTORY, e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DataAccessException("The root file doesn't exist suggesting an incomplete installation: " + rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DataAccessException("The root file isn't a directory.");
			}
			String absoluteRootDirectory = rootDirectory.getAbsolutePath();
			
			// A filter when listing a set of directories for a file.
			DirectoryFilter directoryFilter = new DirectoryFilter();
			
			// A local File to use while we are searching to not confuse other
			// threads.
			File newDirectory = currLeafDirectory;
			
			// A flag to indicate when we are done looking for a directory.
			boolean lookingForDirectory = true;
			
			// The number of times we stepped up in the hierarchy.
			int depth = 0;
			
			// While we are still looking for a suitable directory,
			while(lookingForDirectory) {
				// Get the current directory's name which should be a Long
				// Valvalueue.
				long currDirectoryName;
				try {
					currDirectoryName = Long.decode(newDirectory.getName());
				}
				catch(NumberFormatException e) {
					if(newDirectory.getAbsolutePath().equals(absoluteRootDirectory)) {
						throw new DataAccessException("Document structure full!", e);
					}
					else {
						throw new DataAccessException("Potential breach of document structure.", e);
					}
				}
				
				// Move the pointer up a directory.
				newDirectory = new File(newDirectory.getParent());
				// Get the list of files in the parent.
				File[] parentDirectoryFiles = newDirectory.listFiles(directoryFilter);
				
				// If this directory has room for a new subdirectory,
				if(parentDirectoryFiles.length < numFilesPerDirectory) {
					// Increment the name for the next subfolder.
					currDirectoryName++;
					
					// Create the new subfolder.
					newDirectory = new File(newDirectory.getAbsolutePath() + "/" + directoryNameBuilder(currDirectoryName, numFilesPerDirectory));
					newDirectory.mkdir();
					
					// Continue drilling down to reach an appropriate leaf
					// node.
					while(depth > 0) {
						newDirectory = new File(newDirectory.getAbsolutePath() + "/" + directoryNameBuilder(0, numFilesPerDirectory));
						newDirectory.mkdir();
						
						depth--;
					}
					
					lookingForDirectory = false;
				}
				// If the parent is full as well, increment the depth unless
				// we are already at the parent. If we are at the parent, then
				// we cannot go up any further and have exhausted the
				// directory structure.
				else
				{
					if(newDirectory.getAbsoluteFile().equals(absoluteRootDirectory)) {
						throw new DataAccessException("Document structure full!");
					}
					else {
						depth++;
					}
				}
			}
			
			currLeafDirectory = newDirectory;
		}
		catch(NumberFormatException e) {
			throw new DataAccessException("Could not decode a directory name as an integer.", e);
		}
		finally {
			DIRECTORY_CREATION_LOCK.unlock();
		}
	}
	
	/**
	 * Builds the name of a folder by prepending zeroes where necessary and
	 * converting the name into a String.
	 * 
	 * @param name The name of the file as an integer.
	 * 
	 * @param numFilesPerDirectory The maximum number of files allowed in the
	 * 							   directory used to determine how many zeroes
	 * 							   to prepend.
	 * 
	 * @return A String representing the directory name based on the
	 * 		   parameters.
	 */
	private static String directoryNameBuilder(long name, int numFilesPerDirectory) {
		int nameLength = String.valueOf(name).length();
		int maxLength = new Double(Math.log10(numFilesPerDirectory)).intValue();
		int numberOfZeros = maxLength - nameLength;
		
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < numberOfZeros; i++) {
			builder.append("0");
		}
		builder.append(String.valueOf(name));
		
		return builder.toString();
	}
	
	/**
	 * Sorts the directories and returns the one whose alphanumeric value is
	 * the greatest.
	 * 
	 * This will work with any naming for directories, so it is the caller's
	 * responsibility to ensure that the list of directories are what they
	 * want them to be.
	 *  
	 * @param directories The list of directories whose largest alphanumeric
	 * 					  value is desired.
	 * 
	 * @return Returns the File whose path and name has the largest
	 * 		   alphanumeric value.
	 */
	private static File getLargestSubfolder(File[] directories) {
		Arrays.sort(directories);
		
		return directories[directories.length - 1];
	}
}