package org.ohmage.query;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.domain.MobilityPoint.Mode;
import org.ohmage.exception.DataAccessException;

public interface IUserMobilityQueries {

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
	void createMobilityPoint(final String username, final String client,
			final MobilityPoint mobilityPoint) throws DataAccessException;

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
	List<Long> getIdsForUser(String username) throws DataAccessException;

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
	List<Long> getIdsForClient(String username, String client)
			throws DataAccessException;

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
	List<Long> getIdsCreatedAfterDate(String username, Date startDate)
			throws DataAccessException;

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
	List<Long> getIdsCreatedBeforeDate(String username, Date endDate)
			throws DataAccessException;

	List<Long> getIdsCreatedBetweenDates(String username, Date startDate,
			Date endDate) throws DataAccessException;

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
	List<Long> getIdsUploadedAfterDate(String username, Date startDate)
			throws DataAccessException;

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
	List<Long> getIdsUploadedBeforeDate(String username, Date endDate)
			throws DataAccessException;

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
	List<Long> getIdsWithPrivacyState(String username,
			MobilityPoint.PrivacyState privacyState) throws DataAccessException;

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
	List<Long> getIdsWithLocationStatus(String username,
			LocationStatus locationStatus) throws DataAccessException;

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
	List<Long> getIdsWithMode(String username, Mode mode)
			throws DataAccessException;

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
	MobilityPoint getMobilityInformationFromId(Long id)
			throws DataAccessException;

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
	List<MobilityPoint> getMobilityInformationFromIds(Collection<Long> ids)
			throws DataAccessException;

	/**
	 * Retrieves the timestamp of last Mobility upload from a user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return Returns a Timestamp representing the date and time that the last
	 * 		   Mobility upload from a user took place. If no Mobility data was
	 * 		   ever uploaded, null is returned.
	 */
	Timestamp getLastUploadForUser(String username) throws DataAccessException;

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
	Double getPercentageOfNonNullLocations(String username, int hours)
			throws DataAccessException;

}