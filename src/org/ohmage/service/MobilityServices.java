package org.ohmage.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.domain.MobilityPoint.Mode;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.impl.UserMobilityQueries;

import edu.ucla.cens.mobilityclassifier.Classification;
import edu.ucla.cens.mobilityclassifier.MobilityClassifier;

/**
 * This class is responsible for all services pertaining to Mobility points.
 * 
 * @author John Jenkins
 */
public final class MobilityServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private MobilityServices() {}
	
	/**
	 * Adds the Mobility point to the database.
	 * 
	 * @param mobilityPoints A list of Mobility points to be added to the 
	 * 						 database.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void createMobilityPoint(final String username, 
			final String client, final List<MobilityPoint> mobilityPoints) 
			throws ServiceException {
		
		if(username == null) {
			throw new ServiceException("The username cannot be null.");
		}
		else if(client == null) {
			throw new ServiceException("The client cannot be null.");
		}
		
		try {
			for(MobilityPoint mobilityPoint : mobilityPoints) {
				UserMobilityQueries.createMobilityPoint(username, client, mobilityPoint);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Runs the classifier against all of the Mobility points in the list.
	 * 
	 * @param mobilityPoints The Mobility points that are to be classified by
	 * 						 the server.
	 * 
	 * @throws ServiceException Thrown if there is an error with the 
	 * 							classification service.
	 */
	public static void classifyData(final List<MobilityPoint> mobilityPoints) 
			throws ServiceException {
		
		// If the list is empty, just exit.
		if(mobilityPoints == null) {
			return;
		}

		// Create a new classifier.
		MobilityClassifier classifier = new MobilityClassifier();
		
		
		// For each of the Mobility points,
		for(MobilityPoint mobilityPoint : mobilityPoints) {
			// If the SubType is sensor data,
			if(MobilityPoint.SubType.SENSOR_DATA.equals(mobilityPoint.getSubType())) {
				// Classify the data.
				Classification classification =
					classifier.classify(mobilityPoint.getSamples(), mobilityPoint.getSensorData().getSpeed());
				
				// If the classification generated some results, pull them out
				// and store them in the Mobility point.
				if(classification.hasFeatures()) {
					try {
						mobilityPoint.setClassifierData(
								classification.getFft(), 
								classification.getVariance(), 
								//classification.getN95Fft(), 
								classification.getVariance(), 
								classification.getAverage(), 
								MobilityPoint.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(IllegalArgumentException e) {
						throw new ServiceException(
								"There was a problem reading the classification's information.", 
								e);
					}
				}
				// If the features don't exist, then create the classifier data
				// with only the mode.
				else {
					try {
						mobilityPoint.setClassifierModeOnly(MobilityPoint.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(IllegalArgumentException e) {
						throw new ServiceException(
								"There was a problem reading the classification's mode.", 
								e);
					}
				}
			}
		}
	}
	
	/**
	 * Retrieves the information about all of the Mobility points that satisify
	 * the parameters. The username is required as that is how Mobility points
	 * are referenced; however, all other parameters are optional and limit the
	 * results based on their value.<br />
	 * <br />
	 * For example, if only a username is given, the result is all of the
	 * Mobility points for that user. If the username and start date are given,
	 * then all of the Mobility points made by that user after that date are
	 * returned. If the username, start date, and end date are all given, the
	 * result is the list of Mobility points made by that user on or after the
	 * start date and on or before the end date.
	 * 
	 * @param username The username of the user whose points are being queried.
	 * 				   Required.
	 * 
	 * @param client A client value that uploaded the point. Optional.
	 * 
	 * @param startDate A date to which all returned points must be on or 
	 * 					after. Optional.
	 * 
	 * @param endDate A date to which all returned points must be on or before.
	 * 				  Optional.
	 * 
	 * @param privacyState A privacy state to limit the results to only those
	 * 					   with this privacy state. Optional.
	 * 
	 * @param locationStatus A location status to limit the results to only 
	 * 						 those with this location status. Optional.
	 * 
	 * @param mode A mode to limit the results to only those with this mode.
	 * 			   Optional.
	 * 
	 * @return A list of MobilityInformation objects where each object 
	 * 		   represents a single Mobility point that satisfies the  
	 * 		   parameters.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<MobilityPoint> retrieveMobilityData(
			final String username, final String client, 
			final Date startDate, final Date endDate, 
			final MobilityPoint.PrivacyState privacyState,
			final LocationStatus locationStatus, final Mode mode) 
			throws ServiceException {
		
		try {
			// Create the IDs list and set it to null. Once we find a non-null
			// parameter, we will set the list to that parameter's value.
			List<Long> mobilityIds = null;
			
			// If both start and end date are non-null, get the IDs from their
			// intersection; otherwise, try and get the IDs from the one that
			// isn't null if either are non-null.
			if((startDate != null) && (endDate != null)) {
				mobilityIds = UserMobilityQueries.getIdsCreatedBetweenDates(username, startDate, endDate);
			}
			else {
				if(startDate != null) {
					mobilityIds = UserMobilityQueries.getIdsCreatedAfterDate(username, startDate);
				}
				else if(endDate != null) {
					mobilityIds = UserMobilityQueries.getIdsCreatedBeforeDate(username, endDate);
				}
			}
			
			if(client != null) {
				if(mobilityIds == null) {
					mobilityIds = UserMobilityQueries.getIdsForClient(username, client);
				}
				else {
					mobilityIds.retainAll(UserMobilityQueries.getIdsForClient(username, client));
				}
			}
			
			if(privacyState != null) {
				if(mobilityIds == null) {
					mobilityIds = UserMobilityQueries.getIdsWithPrivacyState(username, privacyState);
				}
				else {
					mobilityIds.retainAll(UserMobilityQueries.getIdsWithPrivacyState(username, privacyState));
				}
			}
			
			if(locationStatus != null) {
				if(mobilityIds == null) {
					mobilityIds = UserMobilityQueries.getIdsWithLocationStatus(username, locationStatus);
				}
				else {
					mobilityIds.retainAll(UserMobilityQueries.getIdsWithLocationStatus(username, locationStatus));
				}
			}
			
			if(mode != null) {
				if(mobilityIds == null) {
					mobilityIds = UserMobilityQueries.getIdsWithMode(username, mode);
				}
				else {
					mobilityIds.retainAll(UserMobilityQueries.getIdsWithMode(username, mode));
				}
			}
			
			if((mobilityIds == null) || (mobilityIds.size() == 0)) {
				return Collections.emptyList();
			}
			else {
				return UserMobilityQueries.getMobilityInformationFromIds(mobilityIds);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}