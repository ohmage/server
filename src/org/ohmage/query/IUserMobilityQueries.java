/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.domain.MobilityPoint.Mode;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;

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
	 * Retrieves the UUID for all of the Mobility data points that belong to a 
	 * specific user.
	 *  
	 * @param username The user's username.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsForUser(String username) throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a 
	 * specific user and that were uploaded by a specific client.
	 *  
	 * @param username The user's username.
	 * 
	 * @param client The client value.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsForClient(String username, String client)
			throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a 
	 * specific user and that were created on or after a specified date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate The date.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsCreatedAfterDate(String username, Date startDate)
			throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a 
	 * specific user and that were created on or before a specified date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param endDate The date.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsCreatedBeforeDate(String username, Date endDate)
			throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a
	 * specific user and that were created on or after the start date and on
	 * or before the end date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate The start date.
	 * 
	 * @param endDate The end date.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsCreatedBetweenDates(String username, Date startDate,
			Date endDate) throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a 
	 * specific user and that were uploaded on or after a specified date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate The date.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsUploadedAfterDate(String username, Date startDate)
			throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a  
	 * specific user and that were uploaded on or before a specified date.
	 * 
	 * @param username The user's username.
	 * 
	 * @param endDate The date.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsUploadedBeforeDate(String username, Date endDate)
			throws DataAccessException;

	/**
	 * Retrieves the UUID for all of the Mobility data points that belong to a
	 * specific user and that have a given privacy state.
	 * 
	 * @param username The user's username.
	 * 
	 * @param privacyState The privacy state.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsWithPrivacyState(String username,
			MobilityPoint.PrivacyState privacyState) throws DataAccessException;

	/**
	 * Retrieves the UUID for all Mobility data points that belong to a user 
	 * and have a given location status.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param locationStatus The location status.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the 
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsWithLocationStatus(String username,
			LocationStatus locationStatus) throws DataAccessException;

	/**
	 * Retrieves the UUID for all Mobility data points that belong to a user 
	 * and have a given mode.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param mode The mode.
	 * 
	 * @return A, possibly empty but never null, list of UUIDs for the  
	 * 		   resulting Mobility data points.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getIdsWithMode(String username, Mode mode)
			throws DataAccessException;

	/**
	 * Gathers the Mobility information for all of the UUIDs in the collection.
	 * 
	 * @param ids A collection of UUIDs for Mobility points.
	 * 
	 * @return A, possibly empty but never null, list of MobilityPoint objects 
	 * 		   where each object should correspond to an UUID in the 'ids' 
	 * 		   list.
	 *  
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<MobilityPoint> getMobilityInformationFromIds(Collection<String> ids)
			throws DataAccessException;
	
	/**
	 * Gathers the Mobility information for all of the points that match the
	 * given criteria. The username is required as only one user's information
	 * may be queried at a time. The other parameters are optional and will be
	 * ignored if they are null. Otherwise, they will limit the results to only
	 * those that match the give criteria.
	 * 
	 * @param username The user's username.
	 * 
	 * @param startDate Limits the results to only those on or after this date.
	 * 
	 * @param endDate Limits the results to only those on or before this date.
	 * 
	 * @param privacyState Limits the results to only those with the given
	 * 					   privacy state.
	 * 
	 * @param locationStatus Limits the results to only those with the given
	 * 						 location status.
	 * 
	 * @param mode Limits the results to only those with the given mode.
	 * 
	 * @return A, possibly empty but never null, list of Mobility points that
	 * 		   satisfied the parameters.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<MobilityPoint> getMobilityInformation(
			final String username,
			final Date startDate,
			final Date endDate,
			final MobilityPoint.PrivacyState privacyState,
			final LocationStatus locationStatus,
			final Mode mode) 
			throws DataAccessException;
	
	/**
	 * Retrieves all of the dates on which the user has created a Mobility 
	 * point within the date range.
	 * 
	 * @param startDate The earliest date to check if the user has any Mobility
	 * 					points.
	 * 
	 * @param endDate The latest date to check if the user has any Mobility
	 * 				  points.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A list of all of the dates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Set<Date> getDates(
			final Date startDate,
			final Date endDate,
			final String username)
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
	Date getLastUploadForUser(String username) throws DataAccessException;

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
