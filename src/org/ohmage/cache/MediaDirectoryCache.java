package org.ohmage.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ohmage.domain.Media;
import org.ohmage.domain.Image;
import org.ohmage.domain.Audio;
import org.ohmage.domain.Video;
import org.ohmage.domain.OFile;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.impl.SurveyUploadQuery;

// non-instantiable class
public class MediaDirectoryCache {

	private static File currImageDirectory = null;
	private static File currAudioDirectory = null;
	private static File currVideoDirectory = null; 
	private static File currFileDirectory = null;
	
	
	private static final String KEY_IMAGE_DIRECTORY = PreferenceCache.KEY_IMAGE_DIRECTORY;
	private static final String KEY_AUDIO_DIRECTORY = PreferenceCache.KEY_AUDIO_DIRECTORY;
	private static final String KEY_VIDEO_DIRECTORY = PreferenceCache.KEY_VIDEO_DIRECTORY;
	private static final String KEY_FILE_DIRECTORY = PreferenceCache.KEY_FILE_DIRECTORY;
	
	private static final Logger LOGGER = 
			Logger.getLogger(MediaDirectoryCache.class);
	/**
	 * Filters the sub-directories in a directory to only return those that
	 * match the regular expression matcher for directories.
	 * 
	 * @author Joshua Selsky, 
	 * @author Hongsuda T. 
	 */
	private static final class DirectoryFilter implements FilenameFilter {
		private static final Pattern DIRECTORY_PATTERN = 
			Pattern.compile("[0-9]+");
		
		/**
		 * Returns true iff the filename is appropriate for the regular
		 * expression. 
		 */
		public boolean accept(File f, String name) {
			return DIRECTORY_PATTERN.matcher(name).matches();
		}
	}
	
	/**
	 * Default constructor, made private because this class should be 
	 * referenced statically. It is non-instantiable.
	 */
	private MediaDirectoryCache() {
		throw new AssertionError();
	};
	
	public static File getMediaDirectory(Class<? extends Media> mediaType) throws DomainException{
		if (mediaType.equals(Image.class))
			return getImageDirectory();
		else if (mediaType.equals(Audio.class)) 
			return getAudioDirectory();
		else if (mediaType.equals(Video.class))
			return getVideoDirectory();
		else if (mediaType.equals(OFile.class))
			return getFileDirectory();
		else return null;
		
	}
	
	public static synchronized File getImageDirectory() throws DomainException {
		currImageDirectory = getDirectory(currImageDirectory, KEY_IMAGE_DIRECTORY); 
		return currImageDirectory;
	}
	
	public static synchronized File getAudioDirectory() throws DomainException {
		currAudioDirectory = getDirectory(currAudioDirectory, KEY_AUDIO_DIRECTORY); 
		return currAudioDirectory;
	}
	 
	public static synchronized File getVideoDirectory() throws DomainException {
		currVideoDirectory = getDirectory(currVideoDirectory, KEY_VIDEO_DIRECTORY); 
		return currVideoDirectory;
	}
	
	public static synchronized File getFileDirectory() throws DomainException {
		currFileDirectory = getDirectory(currFileDirectory, KEY_FILE_DIRECTORY); 
		return currFileDirectory;
	}
	
	
	/**
	 * Retrieves the file to use to store a media. Each call to this function
	 * has the implicit expectation that a media file will be stored in the
	 * resulting directory; however, this is not required and is not a 
	 * necessity.
	 * 
	 * @return A File object where a media file should be written.
	 */
	private static synchronized File getDirectory(File currMediaDirectory, String keyRootDirectory) throws DomainException {

		// Get the maximum number of items in a directory.
		int numFilesPerDirectory;
		try {
			numFilesPerDirectory = 
				Integer.decode(
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY));
		}
		catch(CacheMissException e) {
			throw new DomainException(
				"Preference cache doesn't know about 'known' key: " + 
					PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY,
				e);
		}
		catch(NumberFormatException e) {
			throw new DomainException(
				"Stored value for key '" + 
					PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY +
					"' is not decodable as a number.",
				e);
		}
		
		// If the leaf directory was never initialized, then we should do
		// that. Note that the initialization is dumb in that it will get to
		// the end of the structure and not check to see if the leaf node is
		// full.
		if(currMediaDirectory == null) {		
			currMediaDirectory = init(currMediaDirectory, keyRootDirectory, numFilesPerDirectory);  
		}
		
		File[] documents = currMediaDirectory.listFiles();
		// If the 'imageLeafDirectory' directory is full, traverse the tree and
		// find a new directory.
		if(documents.length >= numFilesPerDirectory) {
			currMediaDirectory = getNewDirectory(currMediaDirectory, keyRootDirectory, numFilesPerDirectory);
		}
		
		return currMediaDirectory;
	}
	
	/**
	 * Initializes the directory structure by drilling down to the leaf
	 * directory with each step choosing the directory with the largest
	 * integer value.
	 */
	private static synchronized File init(File currMediaDirectory, String keyRootDirectory,
			final int numFilesPerDirectory)
			throws DomainException {
		
		try {
			// If the current leaf directory has been set, we weren't the first
			// to call init(), so we can just back out.
			if(currMediaDirectory != null) {
				return currMediaDirectory;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = 
					PreferenceCache.instance().lookup(keyRootDirectory);
			}
			catch(CacheMissException e) {
				throw new DomainException(
					"Preference cache doesn't know about 'known' key: " + 
					keyRootDirectory,
					e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DomainException(
					"The root file doesn't exist suggesting an incomplete installation: " + 
						rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DomainException("The root file isn't a directory: " + rootFile );
			}
			
			// Get the number of folders deep that documents are stored.
			int fileDepth;
			try {
				fileDepth = 
					Integer.decode(
						PreferenceCache.instance().lookup(
							PreferenceCache.KEY_FILE_HIERARCHY_DEPTH));
			}
			catch(CacheMissException e) {
				throw new DomainException(
					"Preference cache doesn't know about 'known' key: " +
						PreferenceCache.KEY_FILE_HIERARCHY_DEPTH,
					e);
			}
			catch(NumberFormatException e) {
				throw new DomainException(
					"Stored value for key '" + 
						PreferenceCache.KEY_FILE_HIERARCHY_DEPTH + 
						"' is not decodable as a number.",
					e);
			}
			
			DirectoryFilter directoryFilter = new DirectoryFilter();
			File currDirectory = rootDirectory;
			for(int currDepth = 0; currDepth < fileDepth; currDepth++) {
				// Get the list of directories in the current directory.
				File[] currDirectories = 
					currDirectory.listFiles(directoryFilter);
				
				// If there aren't any, create the first sub-directory in this
				// directory.
				if(currDirectories.length == 0) {
					String newFolderName = 
						directoryNameBuilder(0, numFilesPerDirectory);
					currDirectory = 
						new File(
							currDirectory.getAbsolutePath() + 
							"/" + 
							newFolderName);
					currDirectory.mkdir();
				}
				// If the directory is overly full, step back up in the
				// structure. This should never happen, as it indicates that
				// there is an overflow in the structure.
				else if(currDirectories.length > numFilesPerDirectory) {
					// Take a step back in our depth.
					currDepth--;
					
					// If, while backing up the tree, we back out of the root
					// directory, we have filled up the space.
					if(currDepth < 0) {
						throw new DomainException(
							"Image directory structure full!");
					}

					// Get the next parent and the current directory to it.
					int nextDirectoryNumber = 
						Integer.decode(currDirectory.getName()) + 1;
					currDirectory = 
						new File(
							currDirectory.getParent() + 
							"/" + 
							nextDirectoryNumber);
					
					// If the directory already exists, then there is either a
					// concurrency issue or someone else is adding files.
					// Either way, this shouldn't happen.
					if(currDirectory.exists()) {
						throw new DomainException(
							"Somehow the 'new' directory already exists. This should be looked into: " + 
								currDirectory.getAbsolutePath());
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
			return(currDirectory); 
	
		}
		catch(SecurityException e) {
			throw new DomainException(
				"The current process doesn't have sufficient permiossions to create new directories.",
				e);
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
	private static synchronized File getNewDirectory(File currMediaDirectory, String keyRootDirectory,
			final int numFilesPerDirectory)
			throws DomainException {
		
		try {
			// Make sure that this hasn't changed because another thread may
			// have preempted us and already changed the current leaf
			// directory.
			File[] files = currMediaDirectory.listFiles();
			if(files.length < numFilesPerDirectory) {
				return currMediaDirectory;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(keyRootDirectory);
			}
			catch(CacheMissException e) {
				throw new DomainException(
					"Preference cache doesn't know about 'known' key: " + 
						keyRootDirectory,
					e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DomainException(
					"The root file doesn't exist suggesting an incomplete installation: " + 
						rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DomainException("The root file isn't a directory.");
			}
			String absoluteRootDirectory = rootDirectory.getAbsolutePath();
			
			// A filter when listing a set of directories for a file.
			DirectoryFilter directoryFilter = new DirectoryFilter();
			
			// A local File to use while we are searching to not confuse other
			// threads.
			File newDirectory = currMediaDirectory;
			
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
					String dirName = newDirectory.getName();
					while(dirName.startsWith("0")) {
						dirName = dirName.substring(1);
					}
					if("".equals(dirName)) {
						currDirectoryName = 0;
					}
					else {
						currDirectoryName = Long.decode(dirName);
					}
				}
				catch(NumberFormatException e) {
					if(newDirectory.getAbsolutePath().equals(absoluteRootDirectory)) {
						throw new DomainException(
							"Document structure full!",
							e);
					}
					else {
						throw new DomainException(
							"Potential breach of document structure.",
							e);
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
						throw new DomainException("Document structure full!");
					}
					else {
						depth++;
					}
				}
			}
			
			return(newDirectory);
		}
		catch(NumberFormatException e) {
			throw new DomainException(
				"Could not decode a directory name as an integer.",
				e);
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
	private static String directoryNameBuilder(
			final long name,
			final int numFilesPerDirectory) {
		
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

	/*
	public static void main(String[] args){
		try {
			for (int i = 0; i< 5; i++) {
				System.out.println("--- i = " + i + " ---");
				System.out.println("Image dir: " + getImageDirectory().getAbsolutePath());
				System.out.println("Audio dir: " + getAudioDirectory().getAbsolutePath());
				System.out.println("Video dir: " + getVideoDirectory().getAbsolutePath());
				System.out.println("Document dir: " + getDocumentDirectory().getAbsolutePath());
			}
			
		} catch(DomainException e){
			System.out.println("Something is wrong");
		}
	
	}
	*/
}
