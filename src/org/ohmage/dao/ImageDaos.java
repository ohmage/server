package org.ohmage.dao;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class is responsible for all operations directly pertaining to images.
 * It may read information from other entities as required but the parameters
 * to these functions, the return values from these functions, and the changes
 * made by these functions should only pertain to images.
 * 
 * @author John Jenkins
 */
public final class ImageDaos extends Dao {
	private static final Logger LOGGER = Logger.getLogger(ImageDaos.class);
	
	// Checks if an image exists.
	private static final String SQL_EXISTS_IMAGE = 
		"SELECT EXISTS(" +
			"SELECT ubr.uuid " +
			"FROM url_based_resource ubr, prompt_response pr " +
			"WHERE ubr.uuid = ? " +
			"AND pr.response = ubr.uuid " +
			"AND pr.prompt_type = 'photo'" +
		")";
	
	// Retrieves the URL for an image.
	private static final String SQL_GET_IMAGE_URL =
		"SELECT DISTINCT(ubr.url) " +
		"FROM url_based_resource ubr, prompt_response pr " +
		"WHERE ubr.uuid = ? " +
		"AND pr.response = ubr.uuid " +
		"AND pr.prompt_type = 'photo'";
	
	// Inserts an images information into the url_based_resource table.
	private static final String SQL_INSERT_IMAGE = 
		"INSERT INTO url_based_resource(user_id, client, uuid, url) " +
		"VALUES (" +
			"(" +	// user_id
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			")," +
			"?," +	// client
			"?," +	// uuid
			"?" +	// url
		")";
	
	// Deletes an image form the url_based_resource table.
	private static final String SQL_DELETE_IMAGE =
		"DELETE FROM url_based_resource " +
		"WHERE uuid = ?";
	
	private static final Pattern IMAGE_DIRECTORY_PATTERN = Pattern.compile("[0-9]+");
	
	public static final String IMAGE_STORE_FORMAT = "png";
	public static final String IMAGE_SCALED_EXTENSION = "-s";
	private static final double IMAGE_SCALED_MAX_DIMENSION = 150.0;
	
	/**
	 * Filters the sub-directories in a directory to only return those that
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
			return IMAGE_DIRECTORY_PATTERN.matcher(name).matches();
		}
	}
	
	// The current directory to which the next document should be saved.
	private static File currLeafDirectory;
	
	private static ImageDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private ImageDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Stores a BufferedImage onto the file system with a thumbnail version,
	 * then it adds the reference to the image in the database.
	 *  
	 * @param username The username of the user that is storing the image.
	 * 
	 * @param client The client parameter that was given that is storing the
	 * 				 image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @param imageContents The contents of the image to be stored on the file
	 * 						system along with a thumbnail version.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static void createImage(String username, String client, String imageId, BufferedImage imageContents) throws DataAccessException {
		// First, we need to make sure the file wasn't already uploaded in 
		// which case we just ignore this call as we allow for duplicates.
		if(getImageExists(imageId)) {
			return;
		}
		
		// getDirectory() is used as opposed to accessing the current leaf
		// directory class variable as it will do sanitation in case it hasn't
		// been initialized or is full.
		File imageDirectory = getDirectory();
		File regularImage = new File(imageDirectory.getAbsolutePath() + "/" + imageId);
		File scaledImage = new File(imageDirectory.getAbsolutePath() + "/" + imageId + IMAGE_SCALED_EXTENSION);
		
		// Write the original to the file system.
		try {
			ImageIO.write(imageContents, IMAGE_STORE_FORMAT, regularImage);
		}
		catch(IOException e) {
			throw new DataAccessException("Error writing the regular image to the system.", e);
		}
		
		// Write the scaled image to the file system.
		try {
			// Get the percentage to scale the image.
			Double scalePercentage;
			if(imageContents.getWidth() > imageContents.getHeight()) {
				scalePercentage = IMAGE_SCALED_MAX_DIMENSION / imageContents.getWidth();
			}
			else {
				scalePercentage = IMAGE_SCALED_MAX_DIMENSION / imageContents.getHeight();
			}
			
			// Calculate the scaled image's width and height.
			int width = (new Double(imageContents.getWidth() * scalePercentage)).intValue();
			int height = (new Double(imageContents.getHeight() * scalePercentage)).intValue();
			
			// Create the new image of the same type as the original and of the
			// scaled dimensions.
			BufferedImage scaledContents = new BufferedImage(width, height, imageContents.getType());
			
			// Paint the original image onto the scaled canvas.
			Graphics2D graphics2d = scaledContents.createGraphics();
			graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2d.drawImage(imageContents, 0, 0, width, height, null);
			
			// Cleanup.
			graphics2d.dispose();
			
			// Write the scaled image to the filesystem.
			ImageIO.write(scaledContents, IMAGE_STORE_FORMAT, scaledImage);
		}
		catch(IOException e) {
			regularImage.delete();
			throw new DataAccessException("Error writing the scaled image to the system.", e);
		}
		
		// Get the image's URL.
		String url = "file://" + regularImage.getAbsolutePath();
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a reference to an image.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the image URL into the database.
			try {
				instance.getJdbcTemplate().update(
						SQL_INSERT_IMAGE, 
						new Object[] { username, client, imageId, url }
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				regularImage.delete();
				scaledImage.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_IMAGE + "' with parameters: " +
						username + ", " + client + ", " + imageId + ", " + url, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				regularImage.delete();
				scaledImage.delete();
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}

	/**
	 * Retrieves whether or not an image with the given ID exists.
	 * 
	 * @param imageId The image's ID.
	 * 
	 * @return Returns true if the image exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static Boolean getImageExists(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_EXISTS_IMAGE, new Object[] { imageId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_IMAGE + "' with parameter: " + imageId, e);
		}
	}
	
	/**
	 * Retrieves the URL for the image if the image exists. If the image does
	 * not exist, null is returned. 
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @return Returns the URL for the image if it exists; otherwise, null is
	 * 		   returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static String getImageUrl(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_IMAGE_URL, new Object[] { imageId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple images have the same unique identifier.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_URL + "' with parameter: " + imageId, e);
		}
	}
	
	/**
	 * Deletes an image reference from the database and, if successful, deletes
	 * the images off of the file system.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static void deleteImage(String imageId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting an image.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			String imageUrl = getImageUrl(imageId);
			
			try {
				instance.getJdbcTemplate().update(
						SQL_DELETE_IMAGE,
						new Object[] { imageId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing SQL '" + SQL_DELETE_IMAGE + 
						"' with parameter: " +
							imageId, 
						e);
			}
			
			try {
				// Delete the original image.
				if((new File((new URL(imageUrl)).getFile())).delete()) {
					LOGGER.warn("The image no longer existed.");
				}
				
				// Delete the scaled image.
				if((new File((new URL(imageUrl + IMAGE_SCALED_EXTENSION)).getFile())).delete()) {
					LOGGER.warn("The scaled image no longer existed.");
				}
			}
			catch(MalformedURLException e) {
				LOGGER.warn("The URL was malformed, but we are deleting the image anyway.", e);
			}
			catch(SecurityException e) {
				LOGGER.warn("The system would not allow us to delete the image.", e);
			}

			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Gets the directory to which a image should be saved. This should be used
	 * instead of accessing the class-level variable directly as it handles the
	 * creation of new folders and the checking that the current
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
		// If the 'currLeafDirectory' directory is full, traverse the tree and
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
	private static synchronized void init(int numFilesPerDirectory) throws DataAccessException {
		try {
			// If the current leaf directory has been set, we weren't the
			// first to call init(), so we can just back out.
			if(currLeafDirectory != null) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_IMAGE_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_IMAGE_DIRECTORY, e);
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
						LOGGER.error("Image directory structure full!");
						throw new DataAccessException("Image directory structure full!");
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
	}
	
	/**
	 * Checks again that the current leaf directory is full. If it is not, then
	 * it will just back out under the impression someone else made the change.
	 * If it is, it will go up and down the directory tree structure to find a
	 * new leaf node in which to store new files.
	 * 
	 * @param numFilesPerDirectory The maximum allowed number of files in a
	 * 							   leaf directory and the maximum allowed
	 * 							   number of directories in the branches.
	 */
	private static synchronized void getNewDirectory(int numFilesPerDirectory) throws DataAccessException {
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
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_IMAGE_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_IMAGE_DIRECTORY, e);
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