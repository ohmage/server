package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.MobilityModeFeaturesDataPacket;
import edu.ucla.cens.awserver.domain.MobilityModeOnlyDataPacket;


/**
 * DAO for handling persistence of uploaded mobility mode_only data. 
 * 
 * @author selsky
 */
public class MobilityUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(MobilityUploadDao.class);

	private final String _insertMobilityModeOnlySql = "insert into mobility_mode_only_entry" +
	                                                  " (user_id, utc_time_stamp, utc_epoch_millis, phone_timezone, latitude," +
	                                                  " longitude, mode) values (?,?,?,?,?,?,?) ";

	private final String _insertMobilityModeFeaturesSql = "insert into mobility_mode_features_entry" +
			                                              " (user_id, utc_time_stamp, utc_epoch_millis, phone_timezone, latitude," +
			                                              " longitude, mode, speed, variance, average, fft)" +
			                                              " values (?,?,?,?,?,?,?,?,?,?,?)";
	
	public MobilityUploadDao(DataSource datasource) {
		super(datasource);
	}
	
	/**
	 * Attempts to insert mobility DataPackets into the db. If any duplicates are found, they are simply logged. For mobility
	 * uploads, it is possible to receive both types (mode_only and mode_features) within one set of messages, so this method 
	 * handles them both.
	 * 
	 * @throws DataAccessException if any errors other than a duplicate record occur
	 * @throws IllegalArgumentException if a List of DataPackets is not present as an attribute on the AwRequest
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to persist mobility messages");
		
		List<DataPacket> dataPackets = (List<DataPacket>) awRequest.getAttribute("dataPackets");
		
		if(null == dataPackets) {
			throw new IllegalArgumentException("no DataPackets found in the AwRequest");
		}
		
		int userId = awRequest.getUser().getId();
		int index = -1;
				
		for(DataPacket dataPacket : dataPackets) {
			
			boolean isModeFeatures = false;
			
			try {
				index++;
				int numberOfRowsUpdated = 0;
				
				if(dataPacket instanceof MobilityModeFeaturesDataPacket) { // the order of these instanceofs is important because
					                                                       // a MobilityModeFeaturesDataPacket is a 
					                                                       // MobilityModeOnlyDataPacket -- need to check for the 
					                                                       // superclass first -- maybe move away from instanceof?
					isModeFeatures = true;
					numberOfRowsUpdated = insertMobilityModeFeatures((MobilityModeFeaturesDataPacket)dataPacket, userId);
					
				} else if (dataPacket instanceof MobilityModeOnlyDataPacket){
					
					numberOfRowsUpdated = insertMobilityModeOnly((MobilityModeOnlyDataPacket)dataPacket, userId);
					
				} else { // this is a logical error because this class should never be called with non-mobility packets
					
					throw new IllegalArgumentException("invalid data packet found: " + dataPacket.getClass());
				}
				
				if(1 != numberOfRowsUpdated) {
					throw new DataAccessException("inserted multiple rows even though one row was intended. sql: " 
							+  (isModeFeatures ? _insertMobilityModeFeaturesSql : _insertMobilityModeOnlySql)); 
				}
			
			} catch (DataIntegrityViolationException dive) { 
				
				if(isDuplicate(dive)) {
					
					if(_logger.isDebugEnabled()) {
						_logger.info("found a duplicate mobility message");
					}
					handleDuplicate(awRequest, index);
					
				} else {
				
					// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
					// before this DAO runs so there is either missing validation or somehow an auto_incremented key
					// has been duplicated
					
					logError(isModeFeatures, dataPacket, userId);
					throw new DataAccessException(dive);
				}
				
			} catch (org.springframework.dao.DataAccessException dae) { // some other database problem happened that prevented
				                                                        // the SQL from completing normally
				
				
				logError(isModeFeatures, dataPacket, userId);
				throw new DataAccessException(dae);
				
			}
		}
		
		_logger.info("completed mobility message persistence");
	}
	
	/**
	 * The insert is auto_committed, or rather nothing is done in this method to commit the insert so if auto_commit is ever
	 * turned off in the db, this code has to change.  
	 */
	private int insertMobilityModeOnly(final MobilityModeOnlyDataPacket dataPacket, final int userId) { 
		
		return getJdbcTemplate().update( 
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(_insertMobilityModeOnlySql);
					ps.setInt(1, userId);
					ps.setTimestamp(2, Timestamp.valueOf(dataPacket.getUtcDate()));
					ps.setLong(3, dataPacket.getUtcTime());
					ps.setString(4, dataPacket.getTimezone());
					
					if(dataPacket.getLatitude().isNaN()) {
						ps.setNull(5, Types.DOUBLE);
					} else {
						ps.setDouble(5, dataPacket.getLatitude());
					}
					
					if(dataPacket.getLongitude().isNaN()) { 
						ps.setNull(6, Types.DOUBLE);
					} else {
						ps.setDouble(6, dataPacket.getLongitude());
					}
										
					ps.setString(7, dataPacket.getMode());
					
					return ps;
				}
			}
		); 
	}

	/**
	 * The insert is auto_committed, or rather nothing is done in this method to commit the insert so if auto_commit is ever
	 * turned off in the db, this code has to change.  
	 */
	private int insertMobilityModeFeatures(final MobilityModeFeaturesDataPacket dataPacket, final int userId) {
		
		return getJdbcTemplate().update( 
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(_insertMobilityModeFeaturesSql);
					ps.setInt(1, userId);
					ps.setTimestamp(2, Timestamp.valueOf(dataPacket.getUtcDate()));
					ps.setLong(3, dataPacket.getUtcTime());
					ps.setString(4, dataPacket.getTimezone());
					
					if(dataPacket.getLatitude().isNaN()) {
						ps.setNull(5, Types.DOUBLE);
					} else {
						ps.setDouble(5, dataPacket.getLatitude());
					}
					
					if(dataPacket.getLongitude().isNaN()) { 
						ps.setNull(6, Types.DOUBLE);
					} else {
						ps.setDouble(6, dataPacket.getLongitude());
					}
					
					ps.setString(7, dataPacket.getMode());
					ps.setDouble(8, dataPacket.getSpeed());
					ps.setDouble(9, dataPacket.getVariance());
					ps.setDouble(10, dataPacket.getAverage());
					ps.setString(11, dataPacket.getFftArray());
											
					return ps;
				}
			}
		);
	}
	
	private void logError(boolean isModeFeatures, DataPacket dataPacket, int userId) {
		
		if(isModeFeatures) {
			
			MobilityModeFeaturesDataPacket mmfdp = (MobilityModeFeaturesDataPacket) dataPacket;
			
			_logger.error("caught DataAccessException when running SQL '" + _insertMobilityModeFeaturesSql + "' with the following" +
				" parameters: " + userId + ", " + Timestamp.valueOf(mmfdp.getUtcDate()) + ", " + mmfdp.getUtcTime() + ", " +
				mmfdp.getTimezone() + ", " + (mmfdp.getLatitude().equals(Double.NaN) ? "null" : mmfdp.getLatitude()) +  ", " + 
			    (mmfdp.getLongitude().equals(Double.NaN) ? "null" : mmfdp.getLongitude()) + ", " + mmfdp.getMode() + ", " + 
			    mmfdp.getSpeed() + ", " + mmfdp.getVariance() + ", " + mmfdp.getAverage() + ", " + mmfdp.getFftArray());
			 
		} else {
			
			MobilityModeOnlyDataPacket mmodp = (MobilityModeOnlyDataPacket) dataPacket;

			_logger.error("caught DataAccessException when running SQL '" + _insertMobilityModeOnlySql +"' with the following " +
				"parameters: " + userId + ", " + mmodp.getUtcDate() + ", " + mmodp.getUtcTime() + ", " + mmodp.getTimezone() + ", "
				+ (mmodp.getLatitude().equals(Double.NaN) ? "null" : mmodp.getLatitude()) + ", " + 
				(mmodp.getLongitude().equals(Double.NaN) ? "null" : mmodp.getLongitude()) + ", " + mmodp.getMode());
			
		}
	}
}
