package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.cache.CacheMissException;
import edu.ucla.cens.awserver.cache.MobilityPrivacyStateCache;
import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.MobilitySensorDataPacket;
import edu.ucla.cens.awserver.domain.MobilityModeOnlyDataPacket;
import edu.ucla.cens.awserver.request.AwRequest;


/**
 * DAO for handling persistence of uploaded mobility data. 
 * 
 * @author selsky
 */
public class MobilityUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(MobilityUploadDao.class);

	private final String _insertMobilityModeOnlySql =   "INSERT INTO mobility_mode_only"
	                                                  + " (user_id, msg_timestamp, epoch_millis, phone_timezone, location_status," 
	                                                  + " location, mode, client, upload_timestamp, privacy_state_id)"
	                                                  + " VALUES (?,?,?,?,?,?,?,?,?,?)";

	private final String _insertMobilityModeFeaturesSql =   "INSERT INTO mobility_extended"
			                                              + " (user_id, msg_timestamp, epoch_millis, phone_timezone,"
			                                              +	" location_status, location, mode, sensor_data, classifier_version,"
			                                              +	" features, client, upload_timestamp, privacy_state_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public MobilityUploadDao(DataSource datasource) {
		super(datasource);
	}
	
	/**
	 * Attempts to insert mobility DataPackets into the db. If any duplicates are found, they are simply logged. For mobility
	 * uploads, it is possible to receive both types (mode_only and mode_features) within one set of messages, so this method 
	 * handles them both. The entire batch of uploads is handled in one transaction. If duplicate rows are found in the db, a 
	 * savepoint is used to gracefully skip over them during the transaction.
	 * 
	 * @throws DataAccessException if any Spring TransactionException or DataAccessException (except for a 
	 *         DataIntegrityViolationException denoting duplicates) occurs 
	 * @throws IllegalArgumentException if a List of DataPackets is not present as an attribute on the AwRequest
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to persist mobility messages");
		
		List<DataPacket> dataPackets = awRequest.getDataPackets();
		
		if(null == dataPackets) {
			throw new IllegalArgumentException("no DataPackets found in the AwRequest");
		}
		
		int userId = awRequest.getUser().getId();
		String client = awRequest.getClient();
		int index = -1;
		DataPacket currentDataPacket = null;
		
		// Wrap this upload in a transaction 
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("mobility upload");
		
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		// Use a savepoint to handle nested rollbacks if duplicates are found
		Object savepoint = status.createSavepoint();
		
		try { // handle TransactionExceptions
			
			for(DataPacket dataPacket : dataPackets) { 
				
				 try { // handle duplicates and other DataAccessExceptions 
						
					currentDataPacket = dataPacket;
					
					index++;
					int numberOfRowsUpdated = 0;
					
					if(dataPacket instanceof MobilitySensorDataPacket) { // the order of these instanceofs is important because
						                                                       // a MobilitySensorDataPacket is a 
						                                                       // MobilityModeOnlyDataPacket -- need to check for the 
						                                                       // superclass first
						numberOfRowsUpdated = insertMobilityModeFeatures((MobilitySensorDataPacket)dataPacket, userId, client);
						
					} else if (dataPacket instanceof MobilityModeOnlyDataPacket){
						
						numberOfRowsUpdated = insertMobilityModeOnly((MobilityModeOnlyDataPacket)dataPacket, userId, client);
						
					} else { // this is a logical error because this class should never be called with non-mobility packets
						
						throw new IllegalArgumentException("invalid data packet found: " + dataPacket.getClass());
					}
					
					if(1 != numberOfRowsUpdated) {
						throw new DataAccessException("inserted multiple rows even though one row was intended. sql: " 
								+  ((dataPacket instanceof MobilitySensorDataPacket) 
										? _insertMobilityModeFeaturesSql : _insertMobilityModeOnlySql)); 
					}
					
					savepoint = status.createSavepoint();
				
				
				} catch (DataIntegrityViolationException dive) { 
						
					if(isDuplicate(dive)) {
						
						if(_logger.isDebugEnabled()) {
							_logger.info("found a duplicate mobility message");
						}
						handleDuplicate(awRequest, index);
						status.rollbackToSavepoint(savepoint);
						
					} else {
					
						// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
						// before this DAO runs so there is either missing validation or somehow an auto_incremented key
						// has been duplicated
						
						_logger.error("caught DataAccessException", dive);
						logErrorDetails(currentDataPacket, userId, client);
						rollback(transactionManager, status, currentDataPacket, userId, client);
						throw new DataAccessException(dive);
					}
					
				} catch (org.springframework.dao.DataAccessException dae) { // some other database problem happened that prevented
					                                                        // the SQL from completing normally
					
					_logger.error("caught DataAccessException", dae);
					logErrorDetails(currentDataPacket, userId, client);
					rollback(transactionManager, status, currentDataPacket, userId, client);
					throw new DataAccessException(dae);
				}
			}
		
			transactionManager.commit(status);
			_logger.info("completed mobility message persistence");	
		
		} catch (TransactionException te) { 
			
			_logger.error("failed to commit mobility upload transaction, attempting to rollback", te);
			logErrorDetails(currentDataPacket, userId, client);
			rollback(transactionManager, status, currentDataPacket, userId, client);
			throw new DataAccessException(te);
		}
	}
	
	/**
	 * Attempts to rollback a transaction. 
	 */
	private void rollback(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus, 
			DataPacket dataPacket, int userId, String client) {
		
		try {
			
			_logger.error("rolling back a failed mobility upload transaction");
			transactionManager.rollback(transactionStatus);
			
		} catch (TransactionException te) {
			
			_logger.error("failed to rollback mobility upload transaction", te);
			logErrorDetails(dataPacket, userId, client);
			throw new DataAccessException(te);
		}
	}
	
	/**
	 * Insert a row into mobility_mode_only_entry. 
	 */
	private int insertMobilityModeOnly(final MobilityModeOnlyDataPacket dataPacket, final int userId, final String client) { 
		
		return getJdbcTemplate().update( 
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(_insertMobilityModeOnlySql);
					ps.setInt(1, userId);
					ps.setTimestamp(2, Timestamp.valueOf(dataPacket.getDate()));
					ps.setLong(3, dataPacket.getEpochTime());
					ps.setString(4, dataPacket.getTimezone());
					ps.setString(5, dataPacket.getLocationStatus());
					ps.setString(6, dataPacket.getLocation());
					ps.setString(7, dataPacket.getMode());
					ps.setString(8, client);
					ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
					try {
						ps.setInt(10, MobilityPrivacyStateCache.instance().lookup(MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE));
					} catch (CacheMissException e) {
						_logger.error("Cache doesn't know about 'known' privacy state: " + MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE);
						throw new SQLException(e);
					}
					return ps;
				}
			}
		); 
	}
	
	/**
	 * Insert a row into mobility_mode_features_entry. 
	 */
	private int insertMobilityModeFeatures(final MobilitySensorDataPacket dataPacket, final int userId, final String client) {
		
		return getJdbcTemplate().update( 
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(_insertMobilityModeFeaturesSql);

					ps.setInt(1, userId);
					ps.setTimestamp(2, Timestamp.valueOf(dataPacket.getDate()));
					ps.setLong(3, dataPacket.getEpochTime());
					ps.setString(4, dataPacket.getTimezone());
					ps.setString(5, dataPacket.getLocationStatus());
					ps.setString(6, dataPacket.getLocation());
					ps.setString(7, dataPacket.getMode());
					ps.setString(8, dataPacket.getSensorDataString());
					ps.setString(9, dataPacket.getClassifierVersion());
					ps.setString(10, dataPacket.getFeatures());
					ps.setString(11, client);
					ps.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
					try {
						ps.setInt(13, MobilityPrivacyStateCache.instance().lookup(MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE));
					} catch (CacheMissException e) {
						_logger.error("Cache doesn't know about 'known' privacy state: " + MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE);
						throw new SQLException(e);
					}
					return ps;
				}
			}
		);
	}
	
	private void logErrorDetails(DataPacket dataPacket, int userId, String client) {
		
		if(dataPacket instanceof MobilitySensorDataPacket) {
			
			MobilitySensorDataPacket mmfdp = (MobilitySensorDataPacket) dataPacket;
			
			try {
				_logger.error("an error occurred when atempting to run this SQL '" + _insertMobilityModeFeaturesSql + "' with the following" +
					" parameters: " + userId + ", " + Timestamp.valueOf(mmfdp.getDate()) + ", " + mmfdp.getEpochTime() + ", " +
					mmfdp.getTimezone() + ", " + mmfdp.getLocationStatus() + ", " + mmfdp.getLocation() + ", " + mmfdp.getMode() + ", " + 
				    mmfdp.getSensorDataString() + ", " + mmfdp.getClassifierVersion() + ", " + mmfdp.getFeatures() + ", " + client + ", " +
				    System.currentTimeMillis() + ", " + MobilityPrivacyStateCache.instance().lookup(MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE));
			} catch (CacheMissException e) {
				_logger.error("Cache doesn't know about 'known' privacy state: " + MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE);
				throw new DataAccessException(e);
			}
			 
		} else {
			
			MobilityModeOnlyDataPacket mmodp = (MobilityModeOnlyDataPacket) dataPacket;

			try {
				_logger.error("an error occurred when atempting to run this SQL '" + _insertMobilityModeOnlySql +"' with the following " +
					"parameters: " + userId + ", " + mmodp.getDate() + ", " + mmodp.getEpochTime() + ", " + mmodp.getTimezone() + ", "
					 + mmodp.getLocationStatus() + ", " + mmodp.getLocation() + " ," + mmodp.getMode() + ", " + client + ", " 
					 + System.currentTimeMillis() + ", " + MobilityPrivacyStateCache.instance().lookup(MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE));
			} catch (CacheMissException e) {
				_logger.error("Cache doesn't know about 'known' privacy state: " + MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE);
				throw new DataAccessException(e);
			}
		}
	}
}
