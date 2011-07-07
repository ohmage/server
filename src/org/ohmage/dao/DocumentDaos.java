package org.ohmage.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.cache.PreferenceCache;
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
	
	// Inserts the document into the database.
	private static final String SQL_INSERT_DOCUMENT = 
		"INSERT INTO document(uuid, name, description, extension, url, size, privacy_state_id, creation_timestamp) " +
		"VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM document_privacy_state WHERE privacy_state = ?), now())";
	
	// Associates a static user string as the creator of a document.
	private static final String SQL_INSERT_DOCUMENT_USER_CREATOR = 
		"INSERT INTO document_user_creator(document_id, username) " +
		"VALUES ((SELECT id FROM document WHERE uuid = ?), ?)";
	
	// Associates a user with a document and gives them a specific role.
	private static final String SQL_INSERT_DOCUMENT_USER_ROLE = 
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
	private static final String SQL_INSERT_DOCUMENT_CAMPAIGN_ROLE = 
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
	private static final String SQL_INSERT_DOCUMENT_CLASS_ROLE = 
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
	 * Sets up this DAO with a shared DataSource to use. This is called from
	 * Spring and is an error to call within application code.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	public DocumentDaos(DataSource dataSource) {
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
	public static String createDocument(byte[] contents, String name, String description, String privacyState, 
			Map<String, String> campaignRoleMap, Map<String, String> classRoleMap, String creatorUsername) {
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
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the file in the DB.
			try {
				instance.jdbcTemplate.update(
						SQL_INSERT_DOCUMENT, 
						new Object[] { 
								uuid, 
								name, 
								description, 
								extension, 
								url, 
								fileLength, 
								privacyState
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
				instance.jdbcTemplate.update(
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
				instance.jdbcTemplate.update(
						SQL_INSERT_DOCUMENT_USER_ROLE, 
						new Object[] { 
								uuid, 
								creatorUsername, 
								DocumentRoleCache.ROLE_OWNER
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				newFile.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_DOCUMENT_USER_ROLE + "' with parameters: " +
						uuid + ", " + creatorUsername + ", " + DocumentRoleCache.ROLE_OWNER, e);
			}
			
			// Insert any campaign associations in the DB.
			if(campaignRoleMap != null) {
				for(String campaignId : campaignRoleMap.keySet()) {
					// Attempt to insert it into the database.
					try {
						instance.jdbcTemplate.update(
								SQL_INSERT_DOCUMENT_CAMPAIGN_ROLE, 
								new Object[] { 
										uuid, 
										campaignId, 
										campaignRoleMap.get(campaignId)
								}
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						newFile.delete();
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_INSERT_DOCUMENT_CAMPAIGN_ROLE + "' with parameters: " + 
								uuid + ", " + campaignId + ", " + campaignRoleMap.get(campaignId), e);
					}
				}
			}
			
			// Insert any class associations in the DB.
			if(classRoleMap != null) {
				for(String classId : classRoleMap.keySet()) {
					// Attempt to insert it into the database.
					try {
						instance.jdbcTemplate.update(
								SQL_INSERT_DOCUMENT_CLASS_ROLE, 
								new Object[] { 
										uuid, 
										classId, 
										classRoleMap.get(classId)
								}
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						newFile.delete();
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_INSERT_DOCUMENT_CLASS_ROLE + "' with parameters: " + 
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
	private static File getDirectory() {
		// Get the maximum number of items in a directory.
		int numFilesPerDirectory;
		try {
			numFilesPerDirectory = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY));
		}
		catch(CacheMissException e) {
			throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY, e);
		}
		catch(NumberFormatException e) {
			throw new DataAccessException("Stored value for key '" + PreferenceCache.KEY_MAXIMUM_NUMBER_OF_DOCUMENTS_PER_DIRECTORY + "' is not decodable as a number.", e);
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
	private static void init(int numFilesPerDirectory) {
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
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DIRECTORY);
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
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DEPTH, e);
			}
			catch(NumberFormatException e) {
				throw new DataAccessException("Stored value for key '" + PreferenceCache.KEY_DOCUMENT_DEPTH + "' is not decodable as a number.", e);
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
	private static void getNewDirectory(int numFilesPerDirectory) {
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
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_DOCUMENT_DIRECTORY);
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
				// value.
				long currDirectoryName;
				try {
					currDirectoryName = Long.decode(newDirectory.getName());
				}
				catch(NumberFormatException e) {
					if(newDirectory.getAbsolutePath().equals(absoluteRootDirectory)) {
						throw new DataAccessException("Document structure full!");
					}
					else {
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
	private static File getLargestSubfolder(File[] directories) {
		Arrays.sort(directories);
		
		return directories[directories.length - 1];
	}
}