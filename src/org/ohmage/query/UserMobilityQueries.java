package org.ohmage.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.Location;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.domain.MobilityPoint.Mode;
import org.ohmage.domain.MobilityPoint.SubType;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.mobilityclassifier.MobilityClassifier;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-Mobility relationships. While it may read 
 * information pertaining to other entities, the information it takes and  
 * provides should pertain to user-Mobility relationships only.
 * 
 * @author John Jenkins
 */
public final class UserMobilityQueries extends AbstractUploadQuery {
	// Retrieves the ID for all of the Mobility points that belong to a user.
	private static final String SQL_GET_IDS_FOR_USER = 
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id";
	
	// Retrieves the ID for all of the Mobility points that belong to a user 
	// and were uploaded by a client.
	private static final String SQL_GET_IDS_FOR_CLIENT =
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.client = ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user 
	// and were created on or after a specified date.
	private static final String SQL_GET_IDS_CREATED_AFTER_DATE = 
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.msg_timestamp >= ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user 
	// and were created on or before a specified date.
	private static final String SQL_GET_IDS_CREATED_BEFORE_DATE =
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.msg_timestamp <= ?";
	
	private static final String SQL_GET_IDS_CREATE_BETWEEN_DATES =
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.msg_timestamp >= ? " +
		"AND m.msg_timestamp <= ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user 
	// and were uploaded on or after a specified date.
	private static final String SQL_GET_IDS_UPLOADED_AFTER_DATE = 
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.upload_timestamp >= ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user 
	// and were uploaded on or before a specified date.
	private static final String SQL_GET_IDS_UPLOADED_BEFORE_DATE =
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.upload_timestamp <= ?";
	
	// Retrieves the UD for all of the Mobility points that belong to a user
	// and have a given privacy state.
	private static final String SQL_GET_IDS_WITH_PRIVACY_STATE =
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.privacy_state = ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user
	// and have a given location status.
	private static final String SQL_GET_IDS_WITH_LOCATION_STATUS = 
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.location_status = ?";
	
	// Retrieves the ID for all of the Mobility points that belong to a user
	// and have a given mode.
	private static final String SQL_GET_IDS_WITH_MODE = 
		"SELECT m.id " +
		"FROM user u, mobility m " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND m.mode = ?";

	// Retrieves all of the information pertaining to a single Mobility data 
	// point from a given database ID.
	private static final String SQL_GET_MOBILITY_DATA_FROM_ID =
		"SELECT u.username, m.client, " +
			"m.msg_timestamp, m.epoch_millis, m.upload_timestamp, " +
			"m.phone_timezone, m.location_status, m.location, " +
			"m.mode, mps.privacy_state, " +
			"me.sensor_data, me.features, me.classifier_version " +
		"FROM user u, mobility_privacy_state mps, " +
			"mobility m LEFT JOIN mobility_extended me " +
			"ON m.id = me.mobility_id " +
		"WHERE m.id = ? " +
		"AND u.id = m.user_id " +
		"AND mps.id = m.privacy_state_id";
	
	private static final String SQL_GET_MOBILITY_DATA_FROM_IDS =
		"SELECT u.username, m.client, " +
			"m.msg_timestamp, m.epoch_millis, m.upload_timestamp, " +
			"m.phone_timezone, m.location_status, m.location, " +
			"m.mode, mps.privacy_state, " +
			"me.sensor_data, me.features, me.classifier_version " +
		"FROM user u, mobility_privacy_state mps, " +
			"mobility m LEFT JOIN mobility_extended me " +
			"ON m.id = me.mobility_id " +
		"WHERE u.id = m.user_id " +
		"AND mps.id = m.privacy_state_id " +
		"AND m.id IN ";
	
	// Retrieves all of the information pertaining to a single Mobility data 
	// point for a given username. This will return alot of data and should be
	// used with caution. Instead, it is recommended that a list of ID's be
	// aggregated and 'SQL_GET_MOBILITY_DATA_FROM_ID' be run on each of those
	// IDs.
	private static final String SQL_GET_MOBILITY_DATA_FOR_USER =
		"SELECT u.username, m.client, " +
			"m.msg_timestamp, m.epoch_millis, m.upload_timestamp, " +
			"m.phone_timezone, m.location_status, m.location, " +
			"m.mode, mps.privacy_state, " +
			"me.sensor_data, me.features, me.classifier_version " +
		"FROM user u, mobility_privacy_state mps, " +
			"mobility m LEFT JOIN mobility_extended me " +
			"ON m.id = me.mobility_id " +
		"WHERE u.username = ? " +
		"AND u.id = m.user_id " +
		"AND mps.id = m.privacy_state_id";
	
	// Inserts a mode-only entry into the database.
	private static final String SQL_INSERT =
		"INSERT INTO mobility(user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, mode, upload_timestamp, privacy_state_id) " +
		"VALUES (" +
			"(" +		// user_id
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), " +
			"?, " +		// client
			"?, " +		// msg_timestamp
			"?, " +		// epoch_millis
			"?, " +		// phone_timezone
			"?, " +		// location_status
			"?, " +		// location
			"?, " +		// mode
			"now(), " +	// upload_timestamp
			"(" +		// privacy_state_id
				"SELECT id " +
				"FROM mobility_privacy_state " +
				"WHERE privacy_state = ?" +
			")" +
		")";
	
	// Inserts an extended entry into the database.
	private static final String SQL_INSERT_EXTENDED =
		"INSERT INTO mobility_extended(mobility_id, sensor_data, features, classifier_version) " +
		"VALUES (" +
			"?, " +		// mobility_id
			"?, " +		// sensor_data
			"?, " +		// features
			"?" +		// classifier_version
		")";
	
	private static UserMobilityQueries instance;

	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use when accessing the database.
	 */
	private UserMobilityQueries(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Creates a new Mobility point.
	 * 
	 * @param username The username of the user to which this point belongs.
	 * 
	 * @param client The client value given on upload.
	 * 
	 * @param mobilityPoint The Mobility point to be created.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static void createMobilityPoint(final String username, final String client,
			final MobilityPoint mobilityPoint) throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a Mobility data point.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);

			try {
				KeyHolder mobilityPointDatabaseKeyHolder = new GeneratedKeyHolder();
				instance.getJdbcTemplate().update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[] {"id"});
							
							ps.setString(1, username);
							ps.setString(2, client);
							
							ps.setTimestamp(3, new Timestamp(mobilityPoint.getDate().getTime()));
							ps.setLong(4, mobilityPoint.getTime());
							ps.setString(5, mobilityPoint.getTimezone().getID());
							
							ps.setString(6, mobilityPoint.getLocationStatus().toString().toLowerCase());
							Location location = mobilityPoint.getLocation();
							ps.setString(7, ((location == null) ? null : location.toJson(false).toString()));
							
							ps.setString(8, mobilityPoint.getMode().toString().toLowerCase());
							
							ps.setString(9, mobilityPoint.getPrivacyState().toString());
							
							return ps;
						}
					},
					mobilityPointDatabaseKeyHolder
				);
				
				// If it's an extended entry, add the sensor data.
				if(SubType.SENSOR_DATA.equals(mobilityPoint.getSubType())) {
					try {
						instance.getJdbcTemplate().update(
								SQL_INSERT_EXTENDED,
								mobilityPointDatabaseKeyHolder.getKey().longValue(),
								mobilityPoint.getSensorData().toJson().toString(),
								((mobilityPoint.getClassifierData() == null) ? new JSONObject() : mobilityPoint.getClassifierData().toJson().toString()),
								MobilityClassifier.getVersion());
					}
					catch(org.springframework.dao.DataAccessException e) {
						throw new DataAccessException(
								"Error executing SQL '" + 
										SQL_INSERT_EXTENDED + 
									"' with parameters: " +
										mobilityPointDatabaseKeyHolder.getKey().longValue() + ", " +
										mobilityPoint.getSensorData().toJson().toString() + ", " +
										((mobilityPoint.getClassifierData() == null) ? new JSONObject() : mobilityPoint.getClassifierData().toJson().toString()) + ", " +
										MobilityClassifier.getVersion(),
								e);
					}
				}
			}
			// If this is a duplicate upload, we will ignore it by rolling back
			// to where we were before we started and return.
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				if(! instance.isDuplicate(e)) {
					throw new DataAccessException(
							"Error executing SQL '" + SQL_INSERT_EXTENDED + "' with parameters: " +
								username + ", " +
								client + ", " +
								TimeUtils.getIso8601DateTimeString(mobilityPoint.getDate()) + ", " +
								mobilityPoint.getTime() + ", " +
								mobilityPoint.getTimezone().getID() + ", " +
								mobilityPoint.getLocationStatus().toString().toLowerCase() + ", " +
								((mobilityPoint.getLocation() == null) ? "null" : mobilityPoint.getLocation().toJson(false).toString()) + ", " +
								mobilityPoint.getMode().toString().toLowerCase() + ", " +
								mobilityPoint.getPrivacyState(),
							e);
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				throw new DataAccessException(
						"Error executing SQL '" + SQL_INSERT_EXTENDED + "' with parameters: " +
							username + ", " +
							client + ", " +
							TimeUtils.getIso8601DateTimeString(mobilityPoint.getDate()) + ", " +
							mobilityPoint.getTime() + ", " +
							mobilityPoint.getTimezone().getID() + ", " +
							mobilityPoint.getLocationStatus().toString().toLowerCase() + ", " +
							((mobilityPoint.getLocation() == null) ? "null" : mobilityPoint.getLocation().toJson(false).toString()) + ", " +
							mobilityPoint.getMode().toString().toLowerCase() + ", " +
							mobilityPoint.getPrivacyState(),
						e);
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
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user.
	 *  
	 * @param username The user's username.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsForUser(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_FOR_USER,
					new Object[] { username },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_FOR_USER + 
						"' with parameter: " + 
							username,
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user and that were uploaded by a specific client.
	 *  
	 * @param username The user's username.
	 * 
	 * @param client The client value.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsForClient(String username, String client) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_FOR_CLIENT,
					new Object[] { username, client },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_FOR_CLIENT + 
						"' with parameters: " + 
							username + ", " +
							client,
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user and that were created on or after a specified
	 * date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate The date.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsCreatedAfterDate(String username, Date startDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_CREATED_AFTER_DATE,
					new Object[] { username, TimeUtils.getIso8601DateString(startDate) },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_CREATED_AFTER_DATE + 
						"' with parameters: " + 
							username + ", " +
							TimeUtils.getIso8601DateString(startDate),
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user and that were created on or before a specified
	 * date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param endDate The date.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsCreatedBeforeDate(String username, Date endDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_CREATED_BEFORE_DATE,
					new Object[] { username, TimeUtils.getIso8601DateString(endDate) },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_CREATED_BEFORE_DATE + 
						"' with parameters: " + 
							username + ", " +
							TimeUtils.getIso8601DateString(endDate),
					e);
		}
	}
	
	public static List<Long> getIdsCreatedBetweenDates(String username, Date startDate, Date endDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_CREATE_BETWEEN_DATES,
					new Object[] { username, TimeUtils.getIso8601DateString(startDate), TimeUtils.getIso8601DateString(endDate) },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_CREATED_BEFORE_DATE + 
						"' with parameters: " + 
							username + ", " +
							TimeUtils.getIso8601DateString(endDate),
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user and that were uploaded on or after a specified
	 * date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate The date.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsUploadedAfterDate(String username, Date startDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_UPLOADED_AFTER_DATE,
					new Object[] { username, TimeUtils.getIso8601DateString(startDate) },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_UPLOADED_AFTER_DATE + 
						"' with parameters: " + 
							username + ", " +
							TimeUtils.getIso8601DateString(startDate),
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that 
	 * belong to a specific user and that were uploaded on or before a 
	 * specified date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param endDate The date.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsUploadedBeforeDate(String username, Date endDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_UPLOADED_BEFORE_DATE,
					new Object[] { username, TimeUtils.getIso8601DateString(endDate) },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_UPLOADED_BEFORE_DATE + 
						"' with parameters: " + 
							username + ", " +
							TimeUtils.getIso8601DateString(endDate),
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all of the Mobility data points that
	 * belong to a specific user and that have a given privacy state.
	 * 
	 * @param username The user's username.
	 * 
	 * @param privacyState The privacy state.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for each
	 * 		   Mobility data point belonging to some user.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsWithPrivacyState(String username, MobilityPoint.PrivacyState privacyState) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_WITH_PRIVACY_STATE,
					new Object[] { username, privacyState.toString() },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_WITH_PRIVACY_STATE + 
						"' with parameters: " + 
							username + ", " +
							privacyState,
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all Mobility data points that belong to a
	 * user and have a given location status.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param locationStatus The location status.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsWithLocationStatus(String username, LocationStatus locationStatus) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_WITH_LOCATION_STATUS,
					new Object[] { username, locationStatus.toString().toLowerCase() },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_WITH_LOCATION_STATUS + 
						"' with parameters: " + 
							username + ", " +
							locationStatus.toString().toLowerCase(),
					e);
		}
	}
	
	/**
	 * Retrieves the database ID for all Mobiltiy data points that belong to a
	 * user and have a given mode.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param mode The mode.
	 * 
	 * @return A, possibly empty but never null, list of database IDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<Long> getIdsWithMode(String username, Mode mode) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_IDS_WITH_MODE,
					new Object[] { username, mode.toString().toLowerCase() },
					new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_IDS_WITH_MODE + 
						"' with parameters: " + 
							username + ", " +
							mode.toString().toLowerCase(),
					e);
		}
	}
	
	/**
	 * Retrieves a MobilityInformation object representing the Mobility data
	 * point whose database ID is 'id' or null if no such database ID exists.
	 * 
	 * @param id The Mobility data point's database ID.
	 * 
	 * @return A MobilityInformation object representing this Mobility data
	 * 		   point or null if no such point exists.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static MobilityPoint getMobilityInformationFromId(Long id) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_MOBILITY_DATA_FROM_ID,
					new Object[] { id },
					new RowMapper<MobilityPoint>() {
						@Override
						public MobilityPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
							try {
								JSONObject location = null;
								String locationString = rs.getString("location");
								if(locationString != null) {
									location = new JSONObject(locationString);
								}
								
								JSONObject sensorData = null;
								String sensorDataString = rs.getString("sensor_data");
								if(sensorDataString != null) {
									sensorData = new JSONObject(sensorDataString);
								}
								
								JSONObject features = null;
								String featuresString = rs.getString("features");
								if(featuresString != null) {
									features = new JSONObject(featuresString);
								}
								
								return new MobilityPoint(
										rs.getTimestamp("msg_timestamp"),
										rs.getLong("epoch_millis"),
										TimeZone.getTimeZone(rs.getString("phone_timezone")),
										LocationStatus.valueOf(rs.getString("location_status").toUpperCase()),
										location,
										Mode.valueOf(rs.getString("mode").toUpperCase()),
										MobilityPoint.PrivacyState.getValue(rs.getString("privacy_state")),
										sensorData,
										features,
										rs.getString("classifier_version"));
							}
							catch(JSONException e) {
								throw new SQLException("Error building a JSONObject.", e);
							}
							catch(ErrorCodeException e) {
								throw new SQLException("Error building the MobilityInformation object. This suggests malformed data in the database.", e);
							}
							catch(IllegalArgumentException e) {
								throw new SQLException("Error building the MobilityInformation object. This suggests malformed data in the database.", e);
							}
						}
					}
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple Mobility data points have the same database ID.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_MOBILITY_DATA_FROM_ID + 
						"' with parameter: " + 
							id,
					e);
		}
	}
	
	/**
	 * Gathers the MobilitInformation for all of the IDs in the collection.
	 * 
	 * @param ids A collection of database IDs for Mobility points.
	 * 
	 * @return A, possibly empty but never null, list of MobilityInformation 
	 * 		   objects where each object should correspond to an ID in the 
	 * 		   'ids' list.
	 *  
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static List<MobilityPoint> getMobilityInformationFromIds(Collection<Long> ids) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_MOBILITY_DATA_FROM_IDS + StringUtils.generateStatementPList(ids.size()),
					ids.toArray(),
					new RowMapper<MobilityPoint>() {
						@Override
						public MobilityPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
							try {
								JSONObject location = null;
								String locationString = rs.getString("location");
								if(locationString != null) {
									location = new JSONObject(locationString);
								}
								
								JSONObject sensorData = null;
								String sensorDataString = rs.getString("sensor_data");
								if(sensorDataString != null) {
									sensorData = new JSONObject(sensorDataString);
								}
								
								JSONObject features = null;
								String featuresString = rs.getString("features");
								if(featuresString != null) {
									features = new JSONObject(featuresString);
								}
								
								return new MobilityPoint(
										rs.getTimestamp("msg_timestamp"),
										rs.getLong("epoch_millis"),
										TimeZone.getTimeZone(rs.getString("phone_timezone")),
										LocationStatus.valueOf(rs.getString("location_status").toUpperCase()),
										location,
										Mode.valueOf(rs.getString("mode").toUpperCase()),
										MobilityPoint.PrivacyState.getValue(rs.getString("privacy_state")),
										sensorData,
										features,
										rs.getString("classifier_version"));
							}
							catch(JSONException e) {
								throw new SQLException("Error building a JSONObject.", e);
							}
							catch(ErrorCodeException e) {
								throw new SQLException("Error building the MobilityInformation object. This suggests malformed data in the database.", e);
							}
							catch(IllegalArgumentException e) {
								throw new SQLException("Error building the MobilityInformation object. This suggests malformed data in the database.", e);
							}
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
							SQL_GET_MOBILITY_DATA_FROM_ID + 
						"' with parameter: " + 
							ids,
					e);
		}
	}
	
	/**
	 * Retrieves the timestamp of last Mobility upload from a user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return Returns a Timestamp representing the date and time that the last
	 * 		   Mobility upload from a user took place. If no Mobility data was
	 * 		   ever uploaded, null is returned.
	 */
	public static Timestamp getLastUploadForUser(String username) throws DataAccessException {
		try {
			List<Timestamp> timestamps = instance.getJdbcTemplate().query(
					SQL_GET_MOBILITY_DATA_FOR_USER, 
					new Object[] { username }, 
					new RowMapper<Timestamp>() {
						@Override
						public Timestamp mapRow(ResultSet rs, int rowNum) throws SQLException {
							// First, create a TimeZone object with the time 
							// zone from the database.
							TimeZone timezone = TimeZone.getDefault();
							String timezoneString = rs.getString("phone_timezone");
							if(timezoneString != null) {
								timezone = TimeZone.getTimeZone(timezoneString);
							}
							
							// Next, create a Calendar object and updated it 
							// with the time zone.
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeZone(timezone);
							
							// Finally, grab the time from the database and
							// update it with the time zone stored in the
							// database.
							return rs.getTimestamp("msg_timestamp", calendar);
						}
					}
				);
			
			if(timestamps.size() > 0) {
				Collections.sort(timestamps);
				return timestamps.get(timestamps.size() - 1);
			}
			else {
				return null;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error while executing '" + 
							SQL_GET_MOBILITY_DATA_FOR_USER + 
						"' with parameters: " + 
							username, 
					e);
		}
	}
	
	/**
	 * Returns the percentage of non-null location values that were uploaded in
	 * the last 'hours'.
	 * 
	 * @param username The user's username.
	 * 
	 * @param hours The number of hours before now to find applicable uploads.
	 * 
	 * @return The percentage of non-null Mobility uploads or null if there
	 * 		   were none. 
	 */
	public static Double getPercentageOfNonNullLocations(String username, int hours) 
		throws DataAccessException {
		
		try {
			// Get a time stamp from 'hours' ago.
			Calendar dayAgo = Calendar.getInstance();
			dayAgo.add(Calendar.HOUR_OF_DAY, -hours);
			final Timestamp dayAgoTimestamp = new Timestamp(dayAgo.getTimeInMillis());
			
			final List<String> nonNullLocations = new LinkedList<String>();
			final List<String> allLocations = new LinkedList<String>();
			
			instance.getJdbcTemplate().query(
					SQL_GET_MOBILITY_DATA_FOR_USER, 
					new Object[] { username }, 
					new RowMapper<Object>() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							// First, create a TimeZone object with the time 
							// zone from the database.
							TimeZone timezone = TimeZone.getDefault();
							String timezoneString = rs.getString("phone_timezone");
							if(timezoneString != null) {
								timezone = TimeZone.getTimeZone(timezoneString);
							}
							
							// Next, create a Calendar object and updated it 
							// with the time zone.
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeZone(timezone);
							
							// Now, generate a timestamp with the accurate
							// date and time.
							Timestamp generatedTimestamp = rs.getTimestamp("msg_timestamp", calendar);
							
							// If it was uploaded within the last 'hours' it is
							// valid.
							if(! generatedTimestamp.before(dayAgoTimestamp)) {
								String location = rs.getString("location");
								if(location != null) {
									nonNullLocations.add(location);
								}
								allLocations.add(location);
							}
							
							return null;
						}
					}
				);
			
			if(allLocations.size() == 0) {
				return null;
			}
			else {
				return new Double(nonNullLocations.size()) / new Double(allLocations.size());
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error while executing '" + 
							SQL_GET_MOBILITY_DATA_FOR_USER + 
						"' with parameters: " + 
							username, 
					e);
		}
	}
}