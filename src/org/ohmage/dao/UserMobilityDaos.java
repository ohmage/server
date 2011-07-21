package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.MobilityPrivacyStateCache;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-Mobility relationships. While it may read 
 * information pertaining to other entities, the information it takes and  
 * provides should pertain to user-Mobility relationships only.
 * 
 * @author John Jenkins
 */
public class UserMobilityDaos extends Dao {
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
	public static Timestamp getLastUploadForUser(String requestersUsername, String usersUsername) {
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
	public static Double getPercentageOfNonNullLocations(String requestersUsername, String usersUsername, int hours) {
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
			dayAgo.add(Calendar.HOUR_OF_DAY, -24);
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