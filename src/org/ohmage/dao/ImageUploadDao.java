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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * DAO for saving an image to the filesystem, saving a thumbnail version of that image, finally, inserting a row into
 * url_based_resource that contains a URL to the (full-size) image. Thumbnails are named by adding a "-s" before the file 
 * extension.
 * 
 * When this class creates directories, it relies on the OS for correct permissions' set up.
 * 
 * @author Joshua Selsky
 */
public class ImageUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(ImageUploadDao.class);
	
	private Object lock = new Object();
	
	private Pattern _numberRegexpDir = Pattern.compile("[0-9]+");
	private Pattern _numberRegexpFile;
	
	private File _currentWriteDir;
	private String _currentFileName;
	private String _fileExtension;
	private int _maxNumberOfDirs;
	private int _maxNumberOfFiles;
	private String _initialFileName; // e.g., 000
	private String _initialDir;      // e.g., 0000
	
	private NumberFileNameFilter _numberFileNameFilter;
	private DirectoryFilter _directoryFilter;
	
	private static final int SCALED_HEIGHT = 150;
	private static final int SCALED_WIDTH = 150;
	
	private static final String _insertSql = "insert into url_based_resource (user_id, uuid, url, client) values (?,?,?,?)";
	
	/**
	 * Creates an instance that uses the provided DataSource for database access; rootDirectory as the root for filesystem
	 * storage; fileExtension for naming saved files with the appropriate extension; maxNumberOfDirs for the maximum number of
	 * storage subdirectories; and maxNumberOfFiles for the maximum number of files per directory. The rootDirectory is used to 
	 * create the initial directory for storage e.g. rootDirectory/000/000/000.
	 */
	public ImageUploadDao(DataSource dataSource, String rootDirectory, String fileExtension, 
			int maxNumberOfDirs, int maxNumberOfFiles) {
		
		super(dataSource);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(rootDirectory)) {
			throw new IllegalArgumentException("rootDirectory is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(fileExtension)) {
			throw new IllegalArgumentException("fileExtension is required");
		}
		if(maxNumberOfDirs <= 0 || maxNumberOfDirs > 1000) {
			throw new IllegalArgumentException("maxNumberOfDirs must be greater than zero and <= 1000");
		}
		if(maxNumberOfFiles <= 0 || maxNumberOfFiles > 1000) {
			throw new IllegalArgumentException("maxNumberOfFiles must be greater than zero and <= 1000");
		}
		
		_fileExtension = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
		_numberRegexpFile = Pattern.compile("[0-9]+" + _fileExtension);
		_numberFileNameFilter = new NumberFileNameFilter();
		_directoryFilter = new DirectoryFilter();
		
		_maxNumberOfDirs = (maxNumberOfDirs % 10 == 0) ? maxNumberOfDirs - 1: maxNumberOfDirs;
		_maxNumberOfFiles = (maxNumberOfFiles % 10 == 0) ? maxNumberOfFiles - 1: maxNumberOfFiles;
		
		_initialFileName = initialName(_maxNumberOfFiles);
		
		init(rootDirectory);
	}
	
	/**
	 * Persists media data to a filesystem location and places a URL to the data into the url_based_resource table. The database
	 * write and filesystem write are handled as one transaction.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		
		synchronized(lock) { // synch the whole process to ensure each media file gets a unique URL
		
			if(_logger.isDebugEnabled()) {
				_logger.debug("Saving a media file to the filesystem and a reference to it in url_based_resource");
			}
			
			final int userId = awRequest.getUser().getId();
			final String client = awRequest.getClient();
			final String uuid = awRequest.getMediaId();
			
			final String url = "file://" + _currentWriteDir + "/" + _currentFileName + _fileExtension;
			final String thumbUrl = "file://" + _currentWriteDir + "/" + _currentFileName + "-s" +_fileExtension;
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("url to file: " + url);
			}
			
			OutputStream outputStream = null;
			
			// Wrap this upload in a transaction 
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("media upload");
			
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
			
			// a Savepoint could be used here, but since there is only one row to be inserted a 
			// regular rollback() will do the trick.
				
			try {
				
				// first, save the id and location to the db
				
				getJdbcTemplate().update( 
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(_insertSql);
							ps.setInt(1, userId);
							ps.setString(2, uuid);
							ps.setString(3, url);
							ps.setString(4, client);
							return ps;
						}
					}
				);
				
				// now save to the file system
				
				File f = new File(new URI(url));
				
				if(! f.createNewFile()) { // bad!! This means the file already exists, but there was no row for it in 
					                      // url_based_resource
					rollback(transactionManager, status);
					f = null;
					throw new DataAccessException("File already exists: " + url); 
				}
				
				outputStream = new BufferedOutputStream(new FileOutputStream(f));
				byte[] bytes = awRequest.getMedia();
				int length = bytes.length;
				int offset = 0;
				int writeLen = 1024;
				int total = 0;
				
				while(total < length) {
					int amountToWrite = writeLen < (length - total) ? writeLen : (length - total);
					outputStream.write(bytes, offset, amountToWrite);
					offset += amountToWrite;
					total += writeLen;
				}
				
				outputStream.close();
				outputStream = null;
				
				// also save a thumbnail version of the image
				// TODO consider using ImageIO for the above write as well?
				BufferedImage originalImage = ImageIO.read(new URL(url));
				if(null == originalImage) {
					throw new IOException("could not convert " + url + " to a buffered image");
				}
				
//				int scaledWidth = originalImage.getWidth() / 4;
//				int scaledHeight = originalImage.getHeight() / 4;
				BufferedImage scaledImage = new BufferedImage(SCALED_WIDTH, SCALED_HEIGHT, originalImage.getType());

				// this is slighty counter-intuitive: you have to use the Graphics2D object to "draw" the image even though
				// it is only being saved to the filesystem
				Graphics2D g = scaledImage.createGraphics();
				g.drawImage(originalImage, 0, 0, SCALED_WIDTH, SCALED_HEIGHT, null);
				g.dispose();
				
				File thumb = new File(new URI(thumbUrl));
				outputStream = new BufferedOutputStream(new FileOutputStream(thumb));
				boolean foundWriter = ImageIO.write(scaledImage, "jpeg", outputStream); // the "jpeg" value here is arbitrary
				
				if(! foundWriter) {
					throw new IOException("could not find an ImageWriter for " + originalImage.getType());
				}
				
				outputStream.close();
				outputStream = null;
				
				// now set the write directory and file name for the next file
				setNextDirAndFile();
				
				// done 
				transactionManager.commit(status);
				
			} catch (DataIntegrityViolationException dive) {
				
				if(isDuplicate(dive)) {
					
					if(_logger.isDebugEnabled()) {
						_logger.info("Found a duplicate media upload message. uuid: " + uuid);
					}
					
					handleDuplicate(awRequest, 1); // 1 is passed here because there is only one media resource uploaded at a time
					rollback(transactionManager, status);
					
				} else {
				
					// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
					// before this DAO runs so there is either missing validation or somehow an auto_incremented key
					// has been duplicated
					
					_logger.error("Caught DataAccessException", dive);
					rollback(transactionManager, status);
					throw new DataAccessException(dive);
				}
				
			} catch (org.springframework.dao.DataAccessException dae) {
				
				_logger.error("Caught DataAccessException when attempting to run the SQL + '" + _insertSql + "' with the following "
						+ "params: " + userId + ", " + uuid + ", " + url, dae);
				rollback(transactionManager, status);
				throw new DataAccessException(dae);
	
			} catch (IOException ioe) {
				
				_logger.error("Caught IOException", ioe);
				rollback(transactionManager, status);
				throw new DataAccessException(ioe);

			} catch (URISyntaxException use) {
				
				_logger.error("Caught URISyntaxException", use);
				rollback(transactionManager, status);
				throw new DataAccessException(use);
				
			} catch(TransactionException te) {
				
				_logger.error("Caught TransactionException when attempting to run the SQL + '" + _insertSql + "' with the following "
					+ "params: " + userId + ", " + uuid + ", " + url, te);
				rollback(transactionManager, status); // attempt to rollback even though the exception was thrown at the transaction level
				throw new DataAccessException(te);
			}
			
			finally { // explicit cleanup for exceptional cases				
				if(null != outputStream) {
					try {
						
						outputStream.close();
						outputStream = null;
						
					} catch (IOException ioe) {
						
						_logger.error("Caught IOException trying to close an output stream", ioe);
					}
				}
			}
		}
	}
	
	/**
	 * Attempts to rollback a transaction. 
	 */
	private void rollback(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus) {
		
		try {
			
			_logger.error("Rolling back a failed media upload transaction");
			transactionManager.rollback(transactionStatus);
			
		} catch (TransactionException te) {
			
			_logger.error("Failed to rollback media upload transaction", te);
			throw new DataAccessException(te);
		}
	}
	
	/**
	 * Sets the current write directory and the current file name by checking the filesystem subtree under the rootDir.
	 */
	private void init(String rootDir) { // e.g., /opt/aw/userdata/images
		
		synchronized(lock) {
			
			File rootDirectory = new File(rootDir);
			
			if(! rootDirectory.isDirectory()) {
				throw new IllegalArgumentException(rootDir + " is not a directory");
			}
			
			File f = null;
			_initialDir = initialName(_maxNumberOfDirs);
			
			if(rootDir.endsWith("/")) {
				f = new File(rootDirectory.getAbsolutePath() + _initialDir + "/" + _initialDir + "/" + _initialDir);
			} else {
				f = new File(rootDirectory.getAbsolutePath() + "/" + _initialDir + "/" + _initialDir + "/" + _initialDir);
			}
			
			if(f.exists()) { // If the initial dir exists, fast-forward to the most recent storage location and use that location  
				             // for the initialization properties
			
				try {
					_currentWriteDir = findStartDir(f.getAbsolutePath());
				} catch (IOException ioe) {
					throw new IllegalStateException(ioe);
				}
				
				_currentFileName = findStartFile(_currentWriteDir);
				
			} else { // first time creating the filesystem subtree
				
				if(! f.mkdirs()) { 
					
					throw new IllegalStateException("Cannot create " + f.getAbsolutePath() 
						+ " some of the intermediate dirs may have been created");
				}
				
				_currentWriteDir = f;
				_currentFileName = _initialFileName;
			}
			
			_logger.info("Current write dir " + _currentWriteDir.getAbsolutePath());
			_logger.info("Current file name " + _currentFileName);
			
		}
	}
	
	/**
	 * Sets up the file name and directory for the next file to be written. This method contains no internal synchronization
	 * and relies on the synchronized block in execute().
	 */
	private void setNextDirAndFile() throws IOException {
		
		if(! directoryHasMaxNumberOfFiles(_currentWriteDir)) { 
			
			_currentFileName = incrementName(_currentFileName, _maxNumberOfFiles);
			
		} else {
			
			File parentDir = _currentWriteDir.getParentFile();
			
			if(directoryHasMaxNumberOfSubdirectories(parentDir)) { // since the current directory is full, a new directory will have
				                                                   // to be created, but only if the number of directories isn't
				                                                   // maxed out
				
				if(directoryHasMaxNumberOfSubdirectories(parentDir.getParentFile())) { 
					// bad!!! the whole subtree is full. if this happens, it means there have been manual changes on the filesystem 
					// or we have a serious amount of data (depending on maxNumberOfFiles and maxNumberOfDirs)
					
					_logger.fatal("Media storage filesystem subtree is full!");
					throw new IOException("Media storage filesystem subtree is full!");
					
				}
				
				_currentWriteDir = new File(incrementDir(parentDir).getAbsolutePath() + "/" + _initialDir);
				_currentWriteDir.mkdir();
				_currentFileName = _initialFileName;
				
			} else {
				
				_currentWriteDir = incrementDir(_currentWriteDir);
				_currentFileName = _initialFileName;
			}
		}
	}
	
	private boolean directoryHasMaxNumberOfFiles(File dir) {
		if(dir.listFiles(_numberFileNameFilter).length < _maxNumberOfFiles) {
			return false;
		}
		return true;
	}
	
	private boolean directoryHasMaxNumberOfSubdirectories(File dir) {
		if(dir.listFiles(_directoryFilter).length < _maxNumberOfDirs) {
			return false;
		}
		return true;
	}
	
	private String incrementName(String name, int max) {
		String s = String.valueOf(Integer.parseInt(name) + 1); // Integer.parseInt will gracefully truncate leading zeros
		int len = s.length();
		int zeroPadSize = String.valueOf(max).length() - len;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < zeroPadSize; i++) {
			builder.append("0");
		}
		return builder.append(s).toString();
	}
	
	private File incrementDir(File dir) throws IOException {
		String path = dir.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf("/") + 1);
		
		String name = incrementName(dir.getName(), _maxNumberOfDirs);
		File f = new File(path + name);
		
		if(! f.mkdir()) {
			throw new IOException("Cannot make directory or directory already exists: " + f.getAbsolutePath());
		}
		return f;
	}
	
	private String initialName(int max) {
		int len = String.valueOf(max).length();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < len; i++) {
			builder.append("0");
		}
		return builder.toString();
	}
	
	private File findStartDir(String path) throws IOException {
		File f = new File(path);
		
		if(! f.exists()) {
			if(f.mkdir()) {
				return f;
			} else {
				throw new IllegalStateException("Cannot mkdir " + path);
			}
		} else {
			
			if(directoryHasMaxNumberOfFiles(f)) {
				
				File parentDir = f.getParentFile();
				
				if(directoryHasMaxNumberOfSubdirectories(parentDir)) {
					
					if(directoryHasMaxNumberOfSubdirectories(parentDir.getParentFile())) {
						
						_logger.fatal("Media storage filesystem subtree is full!");
						throw new IllegalStateException("Media storage filesystem subtree is full!");
						
					} else {
						
						return findStartDir(incrementDir(parentDir).getAbsolutePath() + "/" + _initialDir);
						
					}
					
				} else {
					
					return findStartDir(incrementSearchDir(f.getAbsolutePath()));
				}
				
			} else {
				
				return f;
			}
		}
	}
	
	private String incrementSearchDir(String path) {
		String[] splitPath = path.split("/");
		int leafDir = Integer.parseInt(splitPath[splitPath.length - 1]);
		int nodeDir = Integer.parseInt(splitPath[splitPath.length - 2]);
		
		if(leafDir == _maxNumberOfFiles) {
			
			nodeDir++;
			leafDir = 0;
			
		} else {
			
			leafDir++;
		}
		
		String newLeafDir = zeropad(leafDir, _maxNumberOfFiles);
		String newNodeDir = zeropad(nodeDir, _maxNumberOfDirs);
		StringBuilder newPath = new StringBuilder();
		
		for(int i = 0; i < splitPath.length - 2; i++) {
			newPath.append(splitPath[i]);
			newPath.append("/");
		}
		newPath.append(newNodeDir).append("/").append(newLeafDir);
		return newPath.toString();
	}
	
	private String zeropad(int name, int max) {
		int nameLen = String.valueOf(name).length();
		int maxLen = String.valueOf(max).length();
		int numberOfZeros = maxLen - nameLen;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < numberOfZeros; i++) {
			builder.append("0");
		}
		builder.append(String.valueOf(name));
		return builder.toString();
	}
	
	private String findStartFile(File dir) {
		File[] files = dir.listFiles(_numberFileNameFilter);
		
		if(files.length > 0) {
			Arrays.sort(files);
			File f = files[files.length - 1];
			
			return incrementName(f.getName().substring(0, f.getName().lastIndexOf(_fileExtension)), _maxNumberOfFiles);
			
		} else {
			
			return _initialFileName;
		}
	}
	
	/* These FilenameFilters are tricky (and poorly named). The File object passed to accept() is the parent directory of a file 
	 * with the String name */
	
	public class DirectoryFilter implements FilenameFilter {
		public boolean accept(File f, String name) {
			return _numberRegexpDir.matcher(name).matches();
		}
	}
	
	public class NumberFileNameFilter implements FilenameFilter {
		public boolean accept(File f, String name) {
			return _numberRegexpFile.matcher(name).matches();
		}
	}
}
