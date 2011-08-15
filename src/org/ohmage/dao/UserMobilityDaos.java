package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.MobilityPrivacyStateCache;
import org.ohmage.domain.MobilityInformation.Mode;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-Mobility relationships. While it may read 
 * information pertaining to other entities, the information it takes and  
 * provides should pertain to user-Mobility relationships only.
 * 
 * @author John Jenkins
 */
public final class UserMobilityDaos extends Dao {
	private static final int HOURS_IN_A_DAY = 24;
	
	// Inserts a mode-only entry into the database.
	private static final String SQL_INSERT_MODE_ONLY =
		"INSERT INTO mobility_mode_only(user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, mode, upload_timestamp, privacy_state_id) " +
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
		"INSERT INTO mobility_extended(user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, sensor_data, features, classifier_version, mode, upload_timestamp, privacy_state_id " +
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
			"?, " +		// sensor_data
			"?, " +		// features
			"?, " +		// classifier_version
			"?, " +		// mode
			"now(), " +	// upload_timestamp
			"(" +		// privacy_state_id
				"SELECT id " +
				"FROM mobility_privacy_state " +
				"WHERE privacy_state = ?" +
			")" +
		")";
	
	/**
	 * Creates a new Mobility mode-only entry.<br />
	 * <br />
	 * None of the parameters should be null as of 2.6 except location if the
	 * 'locationStatus' allows for it.
	 * 
	 * @param username The username of the user who is creating this Mobility
	 * 				   point.
	 * 
	 * @param client The client parameter that was passed in with this upload.
	 * 
	 * @param timestamp The timestamp on the user's device that created this
	 * 					point. This should correlate with 'timezone'.
	 * 
	 * @param epochMillis The number of milliseconds since epoch according to
	 * 					  the device when this point was created.
	 * 
	 * @param timezone The timezone of the device that created this point. This
	 * 				   should correlate with 'timestamp'.
	 * 
	 * @param locationStatus The status of the location data. This should
	 * 						 correlate with 'location'.
	 * 
	 * @param location A JSONObject with the location information that was 
	 * 				   collected when the Mobility point was made.
	 * 
	 * @param mode The user's mode when this Mobility point was made.
	 * 
	 * @throws DataAccessException Thrown if there is an error, e.g. one of the
	 * 							   parameters is null that isn't allowed to be
	 * 							   null.
	 */
	public static void createModeOnlyEntry(String username, String client, 
			Date timestamp, long epochMillis, TimeZone timezone, 
			String locationStatus, JSONObject location, Mode mode) throws DataAccessException {
		
		try {
			instance.jdbcTemplate.update(
					SQL_INSERT_MODE_ONLY, 
					new Object[] {
							username,
							client,
							timestamp,
							epochMillis,
							(timezone == null) ? null : timezone.getDisplayName(),
							locationStatus,
							location,
							(mode == null) ? null : mode.name().toLowerCase(),
							MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + SQL_INSERT_MODE_ONLY + "' with parameters: " +
					username + ", " +
					client + ", " +
					timestamp + ", " +
					epochMillis + ", " +
					timezone.getDisplayName() + ", " +
					locationStatus + ", " +
					location + ", " +
					mode.name().toLowerCase() + ", " +
					MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE,
				e);
		}
	}
	
	/**
	 * Creates a new Mobility extended entry.<br />
	 * <br />
	 * None of the parameters should be null as of 2.6 except location if the
	 * 'locationStatus' allows for it.
	 * 
	 * @param username The username of the user who is creating this Mobility
	 * 				   point.
	 * 
	 * @param client The client parameter that was passed in with this upload.
	 * 
	 * @param timestamp The timestamp on the user's device that created this
	 * 					point. This should correlate with 'timezone'.
	 * 
	 * @param epochMillis The number of milliseconds since epoch according to
	 * 					  the device when this point was created.
	 * 
	 * @param timezone The timezone of the device that created this point. This
	 * 				   should correlate with 'timestamp'.
	 * 
	 * @param locationStatus The status of the location data. This should
	 * 						 correlate with 'location'.
	 * 
	 * @param location A JSONObject with the location information that was 
	 * 				   collected when the Mobility point was made.
	 * 
	 * @param mode The user's mode as calculated by the server.
	 * 
	 * @param sensorData A JSONObject representing the sensor data collected on
	 * 					 the user's device that was used to generate the mode.
	 * 
	 * @param features A JSONObject representing the features that were 
	 * 				   calculated on the server side to better estimate the 
	 * 				   mode.
	 * 
	 * @param classifierVersion The version of the classifier that was used to
	 * 							calculate the mode on the server side.
	 * 
	 * @throws DataAccessException Thrown if there is an error, e.g. one of the
	 * 							   parameters is null that isn't allowed to be
	 * 							   null.
	 */
	public static void createExtendedEntry(String username, String client, 
			Date timestamp, long epochMillis, TimeZone timezone, 
			String locationStatus, JSONObject location, Mode mode,
			JSONObject sensorData, JSONObject features, String classifierVersion) throws DataAccessException {
		
		try {
			instance.jdbcTemplate.update(
					SQL_INSERT_EXTENDED, 
					new Object[] {
							username,
							client,
							timestamp,
							epochMillis,
							(timezone == null) ? null : timezone.getDisplayName(),
							locationStatus,
							location,
							sensorData,
							features,
							classifierVersion,
							(mode == null) ? null : mode.name().toLowerCase(),
							MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + SQL_INSERT_EXTENDED + "' with parameters: " +
					username + ", " +
					client + ", " +
					timestamp + ", " +
					epochMillis + ", " +
					timezone.getDisplayName() + ", " +
					locationStatus + ", " +
					location + ", " +
					sensorData + ", " +
					features + ", " +
					classifierVersion + ", " +
					mode.name().toLowerCase() + ", " +
					MobilityPrivacyStateCache.PRIVACY_STATE_PRIVATE,
				e);
		}
	}
	
	// Retrieves all of the information about all of the mode-only Mobility 
	// points that are visible to a requester about a user.
	// Note: This query is slow (~50 ms for 1000 records) and large. However,
	// it is large because it contains all of the required ACL rules for 
	// Mobility points, and creating multiple specific queries would result in
	// better performance at the cost of more code. While that may end up being
	// the optimal way to go, for the time being this query will be used to
	// gather all mode-only Mobility data and the RowMapper will be responsible
	// for filtering out what is desired.
	@SuppressWarnings("deprecation")
	private static final String SQL_GET_MODE_ONLY_FOR_USER_FOR_REQUESTER = 
		"SELECT mmo.client, mmo.msg_timestamp, mmo.epoch_millis, mmo.phone_timezone, mmo.location_status, mmo.location, " +
			"mmo.mode, mmo.upload_timestamp, mmo.audit_timestamp, mps.privacy_state " +
		"FROM user u, user ru, mobility_mode_only mmo, mobility_privacy_state mps " +
		// Get the user and their mode-only records.
		"WHERE u.username = ? " +
		"AND u.id = mmo.user_id " +
		// Get the requesting user.
		"AND ru.username = ? " +
		// Align the privacy state.
		"AND mmo.privacy_state_id = mps.id " +
		"AND (" +
			"(" +
				// The requesting user is the same as the desired user.
				"ru.id = u.id" +
			")" +
			" OR " +
			"(" +
				// There exists a campaign which has one of the following 
				// rules:
				"SELECT EXISTS(" +
					"SELECT ru.id " +
					"FROM user_role ur, user_role_campaign urc, user_role_campaign rurc " +
					"WHERE u.id = urc.user_id " +
					"AND ru.id = rurc.user_id " +
					"AND urc.campaign_id = rurc.campaign_id " +
					"AND rurc.user_role_id = ur.id " +
					"AND (" +
						"(" +
							// The requesting user is a supervisor.
							"ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "'" +
						")" +
						" OR " +
						"(" +
							// The requesting user is an analyst and
							"ur.role = '" + CampaignRoleCache.ROLE_ANALYST + "' " +
							"AND (" +
								// the campaign and Mobility point are shared.
								"SELECT EXISTS(" +
									"SELECT c.id " +
									"FROM campaign c, campaign_privacy_state cps " +
									"WHERE c.id = urc.campaign_id " +
									"AND c.privacy_state_id = cps.id " +
									"AND cps.privacy_state = '" + CampaignPrivacyStateCache.PRIVACY_STATE_SHARED + "' " +
									"AND mps.privacy_state = '" + MobilityPrivacyStateCache.PRIVACY_STATE_SHARED + "'" +
								")" +
							")" +
						")" +
					")" +
				")" +
			")" +
		")";
	
	// Retrieves all of the information about all of the extended Mobility 
	// points that are visible to a requester about a user.
	// Note: This query is slow (~50 ms for 1000 records) and large. However,
	// it is large because it contains all of the required ACL rules for 
	// Mobility points, and creating multiple specific queries would result in
	// better performance at the cost of more code. While that may end up being
	// the optimal way to go, for the time being this query will be used to
	// gather all extended Mobility data and the RowMapper will be responsible
	// for filtering out what is desired.
	@SuppressWarnings("deprecation")
	private static final String SQL_GET_EXTENDED_FOR_USER_FOR_REQUESTER = 
		"SELECT me.client, me.msg_timestamp, me.epoch_millis, me.phone_timezone, me.location_status, me.location, me.sensor_data, me.features," +
			"me.classifier_version, me.mode, me.upload_timestamp, me.audit_timestamp, mps.privacy_state " +
		"FROM user u, user ru, mobility_extended me, mobility_privacy_state mps " +
		// Get the user and their mode-only records.
		"WHERE u.username = ? " +
		"AND u.id = me.user_id " +
		// Get the requesting user.
		"AND ru.username = ? " +
		// Align the privacy state.
		"AND me.privacy_state_id = mps.id " +
		"AND (" +
			"(" +
				// The requesting user is the same as the desired user.
				"ru.id = u.id" +
			")" +
			" OR " +
			"(" +
				// There exists a campaign which has one of the following 
				// rules:
				"SELECT EXISTS(" +
					"SELECT ru.id " +
					"FROM user_role ur, user_role_campaign urc, user_role_campaign rurc " +
					"WHERE u.id = urc.user_id " +
					"AND ru.id = rurc.user_id " +
					"AND urc.campaign_id = rurc.campaign_id " +
					"AND rurc.user_role_id = ur.id " +
					"AND (" +
						"(" +
							// The requesting user is a supervisor.
							"ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "'" +
						")" +
						" OR " +
						"(" +
							// The requesting user is an analyst and
							"ur.role = '" + CampaignRoleCache.ROLE_ANALYST + "' " +
							"AND (" +
								// the campaign and Mobility point are shared.
								"SELECT EXISTS(" +
									"SELECT c.id " +
									"FROM campaign c, campaign_privacy_state cps " +
									"WHERE c.id = urc.campaign_id " +
									"AND c.privacy_state_id = cps.id " +
									"AND cps.privacy_state = '" + CampaignPrivacyStateCache.PRIVACY_STATE_SHARED + "' " +
									"AND mps.privacy_state = '" + MobilityPrivacyStateCache.PRIVACY_STATE_SHARED + "'" +
								")" +
							")" +
						")" +
					")" +
				")" +
			")" +
		")";
	
	private static UserMobilityDaos instance;

	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when accessing the database.
	 */
	private UserMobilityDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves the last Mobility upload from a user based on what is visible
	 * to a requesting user.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information about the user.
	 * 
	 * @param usersUsername The username of the user.
	 * 
	 * @return Returns a Timestamp representing the date and time that the last
	 * 		   Mobility upload from a user took place. If no Mobility data was
	 * 		   ever uploaded, null is returned.
	 */
	public static Timestamp getLastUploadForUser(String requestersUsername, String usersUsername) throws DataAccessException {
		// Retrieve a Timestamp of the most recent mode-only upload.
		Timestamp lastModeOnlyUpload = null;
		try {
			List<Timestamp> timestamps = instance.jdbcTemplate.query(
					SQL_GET_MODE_ONLY_FOR_USER_FOR_REQUESTER, 
					new Object[] { usersUsername, requestersUsername }, 
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
				lastModeOnlyUpload = timestamps.get(timestamps.size() - 1);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error while executing '" + SQL_GET_MODE_ONLY_FOR_USER_FOR_REQUESTER + "' with parameters: " + 
					usersUsername + ", " + requestersUsername, e);
		}
		
		// Retrieve a Timestamp of the most recent extended upload.
		Timestamp lastExtendedUpload = null;
		try {
			List<Timestamp> timestamps = instance.jdbcTemplate.query(
					SQL_GET_EXTENDED_FOR_USER_FOR_REQUESTER, 
					new Object[] { usersUsername, requestersUsername }, 
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
				lastExtendedUpload = timestamps.get(timestamps.size() - 1);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error while executing '" + SQL_GET_EXTENDED_FOR_USER_FOR_REQUESTER + "' with parameters: " + 
					usersUsername + ", " + requestersUsername, e);
		}
		
		// Return null if both are null; otherwise, return the most recent of
		// the two.
		if((lastModeOnlyUpload == null) && (lastExtendedUpload == null)) {
			return null;
		}
		else if(lastModeOnlyUpload == null) {
			return lastExtendedUpload;
		}
		else if(lastExtendedUpload == null) {
			return lastModeOnlyUpload;
		}
		else if(lastModeOnlyUpload.after(lastExtendedUpload)) {
			return lastModeOnlyUpload;
		}
		else {
			return lastExtendedUpload;
		}
	}
	
	/**
	 * Returns the percentage of non-null location values that were uploaded in
	 * the last 'hours'.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 *							 this information.
	 *
	 * @param usersUsername The username of the user.
	 * 
	 * @param hours The number of hours before now to find applicable uploads.
	 * 
	 * @return The percentage of non-null Mobility uploads or null if there
	 * 		   were none. 
	 */
	public static Double getPercentageOfNonNullLocations(String requestersUsername, String usersUsername, int hours) 
		throws DataAccessException {
		
		long nonNullLocationsCount = 0;
		long totalLocationsCount = 0;
		
		try {
			// Get a time stamp from 24 hours ago.
			Calendar dayAgo = Calendar.getInstance();
			dayAgo.add(Calendar.HOUR_OF_DAY, -hours);
			final Timestamp dayAgoTimestamp = new Timestamp(dayAgo.getTimeInMillis());
			
			final List<String> nonNullLocations = new LinkedList<String>();
			final List<String> allLocations = new LinkedList<String>();
			
			instance.jdbcTemplate.query(
					SQL_GET_MODE_ONLY_FOR_USER_FOR_REQUESTER, 
					new Object[] { usersUsername, requestersUsername }, 
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							// Get the time the Mobility point was uploaded.
							Timestamp generatedTimestamp = rs.getTimestamp("upload_timestamp");
							
							// If it was uploaded within the last 24 hours it
							// is valid.
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
			
			nonNullLocationsCount += nonNullLocations.size();
			totalLocationsCount += allLocations.size();
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error while executing '" + SQL_GET_MODE_ONLY_FOR_USER_FOR_REQUESTER + "' with parameters: " + 
					usersUsername + ", " + requestersUsername, e);
		}
		
		try {
			// Get a time stamp from 24 hours ago.
			Calendar dayAgo = Calendar.getInstance();
			dayAgo.add(Calendar.HOUR_OF_DAY, -HOURS_IN_A_DAY);
			final Timestamp dayAgoTimestamp = new Timestamp(dayAgo.getTimeInMillis());
			
			final List<String> nonNullLocations = new LinkedList<String>();
			final List<String> allLocations = new LinkedList<String>();
			
			instance.jdbcTemplate.query(
					SQL_GET_EXTENDED_FOR_USER_FOR_REQUESTER, 
					new Object[] { usersUsername, requestersUsername }, 
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							// Get the time the Mobility point was uploaded.
							Timestamp generatedTimestamp = rs.getTimestamp("upload_timestamp");
							
							// If it was uploaded within the last 24 hours it
							// is valid.
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
			
			nonNullLocationsCount += nonNullLocations.size();
			totalLocationsCount += allLocations.size();
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error while executing '" + SQL_GET_EXTENDED_FOR_USER_FOR_REQUESTER + "' with parameters: " + 
					usersUsername + ", " + requestersUsername, e);
		}
		
		if(totalLocationsCount == 0) {
			return null;
		}
		else {
			return new Double(nonNullLocationsCount) / new Double(totalLocationsCount);
		}
	}
}