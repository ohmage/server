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

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.ReturnKeys;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Writes the document to the disk and inserts the new entry into the
 * database.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class DocumentCreationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentCreationDao.class);
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id = ?";
	
	private static final String SQL_GET_DOCUMENT_ID = "SELECT id " +
													  "FROM document " +
													  "WHERE uuid = ?";
	
	private static final String SQL_GET_CAMPAIGN_ID = "SELECT id " +
													  "FROM campaign " +
													  "WHERE urn = ?";
	
	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn = ?";
	
	private static final String SQL_INSERT_DOCUMENT = "INSERT INTO document(uuid, name, description, extension, url, size, privacy_state_id, creation_timestamp) " +
													  "VALUES (?,?,?,?,?,?,?,?)";
	
	private static final String SQL_INSERT_DOCUMENT_USER_CREATOR = "INSERT INTO document_user_creator(document_id, username) " +
																   "VALUES (?,?)";
	
	private static final String SQL_INSERT_DOCUMENT_USER_ROLE = "INSERT INTO document_user_role(document_id, user_id, document_role_id) " +
																"VALUES (?,?,?)";
	
	private static final String SQL_INSERT_DOCUMENT_CAMPAIGN_ROLE = "INSERT INTO document_campaign_role(document_id, campaign_id, document_role_id) " +
																	"VALUES (?,?,?)";
	
	private static final String SQL_INSERT_DOCUMENT_CLASS_ROLE = "INSERT INTO document_class_role(document_id, class_id, document_role_id) " +
																 "VALUES (?,?,?)";
	
	private static final int MAX_EXTENSION_LENGTH = 12;
	
	private Pattern _numberRegexpDir;
	private String _preferenceCacheRootDirectoryKey;
	
	private File _currLeafDirectory;
	private Lock _directoryCreationLock;
	
	/**
	 * Filters the subdirectories in a directory to only return those that
	 * match the regular expression matcher for directories.
	 * 
	 * @author Joshua Selsky
	 */
	private class DirectoryFilter implements FilenameFilter {
		/**
		 * Returns true iff the filename is appropriate for the regular
		 * expression.
		 */
		public boolean accept(File f, String name) {
			return _numberRegexpDir.matcher(name).matches();
		}
	}

	/**
	 * Creates the DAO with a DataSource with which to run the queries.
	 * 
	 * @param dataSource The DataSource to use when running queries.
	 */
	public DocumentCreationDao(DataSource dataSource, String preferenceCacheRootDirectoryKey) {
		super(dataSource);
		
		// If the root directory key is null, throw an exception.
		if(StringUtils.isEmptyOrWhitespaceOnly(preferenceCacheRootDirectoryKey)) {
			throw new IllegalArgumentException("The key from the preference cache to get the root directory for this resource.");
		}
		_preferenceCacheRootDirectoryKey = preferenceCacheRootDirectoryKey;
		
		_numberRegexpDir = Pattern.compile("[0-9]+");
		_directoryCreationLock = new ReentrantLock();
		_currLeafDirectory = null;
	}
	
	/**
	 * Writes the file to the directory that should take the next file. It is
	 * the case that multiple threads may all write to the same file exceeding
	 * the file "limit", but that is acceptable as the "limit" is an arbitrary
	 * constraint to prevent the number of files in a directory from getting
	 * out of hand.
	 * 
	 * It then creates the database associations from the parameterized values,
	 * and if any error occurs it rolls back the transaction and deletes the
	 * file.
	 */
	@Override
	public void execute(AwRequest awRequest) throws DataAccessException {
		_logger.info("Beginning document creation.");
		
		// Create a new, random UUID to use to save this file.
		UUID uuid = UUID.randomUUID();
		
		// getDirectory() is used as opposed to accessing the current leaf
		// directory class variable as it will do sanitation in case it hasn't
		// been initialized or is full.
		File documentDirectory = getDirectory();
		File newFile = new File(documentDirectory.getAbsolutePath() + "/" + uuid.toString());
		String url = "file://" + newFile.getAbsolutePath();
		
		// Get the document from the request.
		String document;
		try {
			document = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT);
		}
		catch(IllegalArgumentException e) {
			_logger.error("The document is missing from the toProcess map.");
			throw new DataAccessException(e);
		}
		
		// Write the document to the file system.
		try {
			FileWriter writer = new FileWriter(newFile);
			writer.write(document);
			writer.flush();
		}
		catch(IOException e) {
			_logger.error("Error writing the new document to the system.", e);
			throw new DataAccessException(e);
		}
		long fileLength = newFile.length();
		
		// Get the name of the file.
		String name;
		try {
			name = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_NAME);
		}
		catch(IllegalArgumentException e) {
			_logger.error("The name of the document is missing from the toProcess map.");
			newFile.delete();
			throw new DataAccessException(e);
		}
		
		// Parse the name and get the extension.
		String extension = getExtension(name);
		
		// Get the initial privacy state ID.
		long privacyStateId;
		try {
			String privacyState = (String) awRequest.getToProcessValue(InputKeys.PRIVACY_STATE);
			privacyStateId = DocumentPrivacyStateCache.instance().lookup(privacyState);
		}
		catch(IllegalArgumentException e) {
			_logger.error("The privacy state of the document is missing from the toProcess map.");
			newFile.delete();
			throw new DataAccessException(e);
		}
		catch(CacheMissException e) {
			_logger.error("Unknown document privacy state in the request.");
			newFile.delete();
			throw new DataAccessException(e);
		}
		
		// Get the optional description.
		String description = null;
		try {
			description = (String) awRequest.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch(IllegalArgumentException e) {
			// The description is optional, so if it doesn't exist we don't
			// care.
		}
		
		// Get the campaign URN list and break it down into an array.
		String campaignUrnRoleList = "";
		try {
			campaignUrnRoleList = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST);
		}
		catch(IllegalArgumentException e) {
			// Its acceptable if there are no initial campaigns with which to
			// associate this document. 
		}
		String[] campaignUrnRoleArray = campaignUrnRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		if((campaignUrnRoleArray.length == 1) && ("".equals(campaignUrnRoleArray[0]))) {
			// This isn't required, but an empty String will always split into
			// a 1-sized array with its only entry being an empty String.
			// Instead, I catch this here so processing later on is seemless.
			campaignUrnRoleArray = new String[0];
		}
		
		// Get the class URN list and break it down into an array.
		String classUrnRoleList = "";
		try {
			classUrnRoleList = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_CLASS_ROLE_LIST);
		}
		catch(IllegalArgumentException e) {
			// It is acceptable if there are no initial classes with which to
			// associate this document.
		}
		String[] classUrnRoleArray = classUrnRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		if((classUrnRoleArray.length == 1) && ("".equals(classUrnRoleArray[0]))) {
			// This isn't required, but an empty String will always split into
			// a 1-sized array with its only entry being an empty String.
			// Instead, I catch this here so processing later on is seemless.
			classUrnRoleArray = new String[0];
		}
		
		// Get the logged in user's database ID.
		long userId;
		try {
			userId = getJdbcTemplate().queryForLong(SQL_GET_USER_ID, new Object[] { awRequest.getUser().getUserName() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + awRequest.getUser().getUserName(), e);
			newFile.delete();
			throw new DataAccessException(e);
		}
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new document entry.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the file in the DB.
			try {
				getJdbcTemplate().update(SQL_INSERT_DOCUMENT, 
						new Object[] { uuid.toString(), name, description, extension, url, fileLength, privacyStateId, new Timestamp(System.currentTimeMillis()) });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_INSERT_DOCUMENT + "' with parameters: " + 
						uuid + ", " + name + ", " + description + ", " + extension + ", " + url + ", " + fileLength + ", " + privacyStateId, e);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			
			// Get the new document's ID.
			long documentId;
			try {
				documentId = getJdbcTemplate().queryForLong(SQL_GET_DOCUMENT_ID, new Object[] { uuid.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_DOCUMENT_ID + "' with parameter: " + uuid, e);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			
			// Insert the creator in the DB.
			try {
				getJdbcTemplate().update(SQL_INSERT_DOCUMENT_USER_CREATOR, new Object[] { documentId, awRequest.getUser().getUserName()  });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_INSERT_DOCUMENT_USER_CREATOR + "' with parameters: " +
						documentId + ", " + userId + ", " + new Timestamp(System.currentTimeMillis()), e);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			
			// Insert this user's user-role in the DB.
			try {
				getJdbcTemplate().update(SQL_INSERT_DOCUMENT_USER_ROLE, new Object[] { documentId, userId, DocumentRoleCache.instance().lookup(DocumentRoleCache.ROLE_OWNER) });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_INSERT_DOCUMENT_USER_ROLE + "' with parameters: " +
						documentId + ", " + userId + ", (DocumentRoleCache's value for" + DocumentRoleCache.ROLE_OWNER + ")", e);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			catch(CacheMissException e) {
				_logger.error("Cache miss while looking up 'known' key: " + DocumentRoleCache.ROLE_OWNER);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			
			// Insert any campaign associations in the DB.
			for(int i = 0; i < campaignUrnRoleArray.length; i++) {
				String[] campaignUrnRole = campaignUrnRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				// Get the campaign's ID.
				long campaignId;
				try {
					campaignId = getJdbcTemplate().queryForLong(SQL_GET_CAMPAIGN_ID, new Object[] { campaignUrnRole[0] });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ID + "' with parameter: " + campaignUrnRole[0], e);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
				
				// Attempt to insert it into the database.
				try {
					getJdbcTemplate().update(SQL_INSERT_DOCUMENT_CAMPAIGN_ROLE, 
							new Object[] { documentId, campaignId, DocumentRoleCache.instance().lookup(campaignUrnRole[1]) });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ID + "' with parameters: " + 
							documentId + ", " + campaignId + ", (DocumentRoleCache's value for " + campaignUrnRole[1] + ")", e);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
				catch(CacheMissException e) {
					_logger.error("Cache miss while looking up key: " + campaignUrnRole[1]);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
			}
			
			// Insert any class associations in the DB.
			for(int i = 0; i < classUrnRoleArray.length; i++) {
				String[] classUrnRole = classUrnRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				// Get the class' ID.
				long classId;
				try {
					classId = getJdbcTemplate().queryForLong(SQL_GET_CLASS_ID, new Object[] { classUrnRole[0] });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + classUrnRole[0], e);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
				
				// Attempt to insert it into the database.
				try {
					getJdbcTemplate().update(SQL_INSERT_DOCUMENT_CLASS_ROLE, 
							new Object[] { documentId, classId, DocumentRoleCache.instance().lookup(classUrnRole[1]) });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_INSERT_DOCUMENT_CLASS_ROLE + "' with parameters: " + 
							documentId + ", " + classId + ", (DocumentRoleCache's value for " + classUrnRole[1] + ")", e);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
				catch(CacheMissException e) {
					_logger.error("Cache miss while looking up key: " + classUrnRole[1]);
					newFile.delete();
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
				
				awRequest.addToReturn(ReturnKeys.DOCUMENT_ID, uuid.toString(), true);
			}
			catch(TransactionException e) {
				_logger.error("Error while committing the transaction.", e);
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
		}
		catch(TransactionException e) {
			_logger.error("Error while attempting to rollback the transaction.");
			newFile.delete();
			throw new DataAccessException(e);
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
	public static String getExtension(String name) {
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
	private File getDirectory() {
		// Get the maximum number of items in a directory.
		int numFilesPerDirectory;
		try {
			numFilesPerDirectory = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY));
		}
		catch(CacheMissException e) {
			_logger.error("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY);
			throw new DataAccessException(e);
		}
		catch(NumberFormatException e) {
			_logger.error("Stored value for key '" + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY + "' is not decodable as a number.");
			throw new DataAccessException(e);
		}
		
		// If the leaf directory was never initialized, then we should do
		// that. Note that the initialization is dumb in that it will get to
		// the end of the structure and not check to see if the leaf node is
		// full.
		if(_currLeafDirectory == null) {
			init(numFilesPerDirectory);
		}
		
		File[] documents = _currLeafDirectory.listFiles();
		// If the _currLeafDirectory directory is full, traverse the tree and
		// find a new directory.
		if(documents.length >= numFilesPerDirectory) {
			getNewDirectory(numFilesPerDirectory);
		}
		
		return _currLeafDirectory;
	}
	
	/**
	 * Initializes the directory structure by drilling down to the leaf
	 * directory with each step choosing the directory with the largest
	 * integer value.
	 */
	private void init(int numFilesPerDirectory) {
		// Get the lock.
		_directoryCreationLock.lock();
		
		try {
			// If the current leaf directory has been set, we weren't the
			// first to call init(), so we can just unlock the lock and back
			// out.
			if(_currLeafDirectory != null) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(_preferenceCacheRootDirectoryKey);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("The preference cache was unaware of the root directory key: " + _preferenceCacheRootDirectoryKey);
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
				fileDepth = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_DOCUMENT_DEPTH));
			}
			catch(CacheMissException e) {
				_logger.error("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DEPTH);
				throw new DataAccessException(e);
			}
			catch(NumberFormatException e) {
				_logger.error("Stored value for key '" + PreferenceCache.KEY_DOCUMENT_DEPTH + "' is not decodable as a number.");
				throw new DataAccessException(e);
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
					_logger.warn("Too many subdirectories in: " + currDirectory.getAbsolutePath());
					
					// Take a step back in our depth.
					currDepth--;
					
					// If, while backing up the tree, we back out of the root
					// directory, we have filled up the space.
					if(currDepth < 0) {
						_logger.error("The end of the heirarchy was reached! We are out of space to save documents!");
						throw new DataAccessException("Document structure full!");
					}

					// Get the next parent and the current directory to it.
					int nextDirectoryNumber = Integer.decode(currDirectory.getName()) + 1;
					currDirectory = new File(currDirectory.getParent() + "/" + nextDirectoryNumber);
					
					// If the directory already exists, then there is either a
					// concurrency issue or someone else is adding files.
					// Either way, this shouldn't happen.
					if(currDirectory.exists()) {
						_logger.error("Somehow the 'new' directory already exists. This should be looked into: " + currDirectory.getAbsolutePath());
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
			_currLeafDirectory = currDirectory;
		}
		catch(SecurityException e) {
			_logger.error("The current process doesn't have sufficient permiossions to create new directories.", e);
			throw new DataAccessException(e);
		}
		finally {
			// No matter what happens, unlock the lock.
			_directoryCreationLock.unlock();
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
	private void getNewDirectory(int numFilesPerDirectory) {
		// Get the lock.
		_directoryCreationLock.lock();
		
		try {
			// Make sure that this hasn't changed because another thread may
			// have preempted us and already changed the current leaf
			// directory.
			File[] files = _currLeafDirectory.listFiles();
			if(files.length < numFilesPerDirectory) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(_preferenceCacheRootDirectoryKey);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("The preference cache was unaware of the root directory key: " + _preferenceCacheRootDirectoryKey);
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
			File newDirectory = _currLeafDirectory;
			
			// A flag to indicate when we are done looking for a directory.
			boolean lookingForDirectory = true;
			
			// The number of times we stepped up in the hierarchy.
			int depth = 0;
			
			// While we are still looking for a suitable directory,
			while(lookingForDirectory) {
				// Get the current directory's name which should be a Long
				// value.
				long currDirectoryName;
				try {
					currDirectoryName = Long.decode(newDirectory.getName());
				}
				catch(NumberFormatException e) {
					if(newDirectory.getAbsolutePath().equals(absoluteRootDirectory)) {
						_logger.error("Document structure full!");
						throw new DataAccessException("Document structure full!");
					}
					else {
						_logger.error("WARNING: Potential breach of document strucutre. Someone may be attempting to retrieve files that are not in the document structure!");
						throw new DataAccessException("Potential breach of document structure.");
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
						_logger.error("The end of the heirarchy was reached! We are out of space to save documents!");
						throw new DataAccessException("Document structure full!");
					}
					else {
						depth++;
					}
				}
			}
			
			_currLeafDirectory = newDirectory;
		}
		catch(NumberFormatException e) {
			_logger.error("Could not decode a directory name as an integer.", e);
			throw new DataAccessException(e);
		}
		finally {
			_directoryCreationLock.unlock();
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
	private String directoryNameBuilder(long name, int numFilesPerDirectory) {
		int nameLength = String.valueOf(name).length();
		int maxLength = String.valueOf(numFilesPerDirectory).length();
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
	private File getLargestSubfolder(File[] directories) {
		Arrays.sort(directories);
		
		return directories[directories.length - 1];
	}
}
