package org.ohmage.service;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.dao.UserMobilityDaos;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.domain.MobilityInformation.SubType;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

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
	 * @param request The Request that is performing this service.
	 * 
	 * @param mobilityPoints A list of Mobility points to be added to the 
	 * 						 database.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void createMobilityPoint(Request request, String username, String client,
			List<MobilityInformation> mobilityPoints) throws ServiceException {
		if(username == null) {
			throw new ServiceException("The username cannot be null.");
		}
		else if(client == null) {
			throw new ServiceException("The client cannot be null.");
		}
		
		try {
			for(MobilityInformation mobilityPoint : mobilityPoints) {
				if(SubType.MODE_ONLY.equals(mobilityPoint.getSubType())) {
					UserMobilityDaos.createModeOnlyEntry(
							username, 
							client, 
							mobilityPoint.getDate(), 
							mobilityPoint.getTime(), 
							mobilityPoint.getTimezone(), 
							mobilityPoint.getLocationStatus(), 
							(mobilityPoint.getLocation() == null) ? null : mobilityPoint.getLocation().toJson(), 
							mobilityPoint.getMode());
				}
				else if(SubType.SENSOR_DATA.equals(mobilityPoint.getSubType())) {
					UserMobilityDaos.createExtendedEntry(
							username,
							client,
							mobilityPoint.getDate(), 
							mobilityPoint.getTime(), 
							mobilityPoint.getTimezone(), 
							mobilityPoint.getLocationStatus(), 
							(mobilityPoint.getLocation() == null) ? null : mobilityPoint.getLocation().toJson(), 
							mobilityPoint.getMode(), 
							mobilityPoint.getSensorData().toJson(), 
							(mobilityPoint.getClassifierData() == null) ? new JSONObject("{}") : mobilityPoint.getClassifierData().toJson(), 
							MobilityClassifier.getVersion());
				}
			}
		}
		catch(JSONException e) {
			request.setFailed();
			throw new ServiceException("There was an error building the empty JSONObject used to replace the missing classifier data.", e);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Runs the classifier against all of the Mobility points in the list.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param mobilityPoints The Mobility points that are to be classified by
	 * 						 the server.
	 * 
	 * @throws ServiceException Thrown if there is an error with the 
	 * 							classification service.
	 */
	public static void classifyData(Request request, List<MobilityInformation> mobilityPoints) throws ServiceException {
		// If the list is empty, just exit.
		if(mobilityPoints == null) {
			return;
		}

		// Create a new classifier.
		MobilityClassifier classifier = new MobilityClassifier();
		
		
		// For each of the Mobility points,
		for(MobilityInformation mobilityPoint : mobilityPoints) {
			// If the SubType is sensor data,
			if(MobilityInformation.SubType.SENSOR_DATA.equals(mobilityPoint.getSubType())) {
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
								MobilityInformation.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(IllegalArgumentException e) {
						request.setFailed();
						throw new ServiceException("There was a problem reading the classification's information.", e);
					}
				}
				// If the features don't exist, then create the classifier data
				// with only the mode.
				else {
					try {
						mobilityPoint.setClassifierModeOnly(MobilityInformation.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(IllegalArgumentException e) {
						request.setFailed();
						throw new ServiceException("There was a problem reading the classification's mode.", e);
					}
				}
			}
		}
	}
}