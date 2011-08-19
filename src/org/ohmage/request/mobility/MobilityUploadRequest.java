package org.ohmage.request.mobility;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.validator.MobilityValidators;

/**
 * <p>Creates a new Mobility data point. There are no restrictions on who can
 * upload data points only that they have an active account.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the user that is uploading this point and for whom
 *       the point applies.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The user's hashed password.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DATA}</td>
 *     <td>A JSONArray of JSONObjects where each JSONObject is an individual
 *       data point. Each data point must contain the following keys and 
 *       values:
 *       <ul>
 *         <li><b>date</b>: A date and time in the ISO 8601 format representing
 *           the time at which this Mobility data point was created.</li>
 *         <li><b>time</b>: The number of milliseconds since the epoch 
 *           representing the time at which this Mobility data point was 
 *           created.</li>
 *         <li><b>timezone</b>: The time zone of the device when it created 
 *           this Mobility data point.</li>
 *         <li><b>subtype</b>: The type of data point, which defines other
 *           aspects of this Mobility data point; one of
 *           {@link org.ohmage.domain.MobilityInformation.SubType}.</li>
 *         <li><b>location_status</b>: The status of the location information;
 *           one of
 *           {@link org.ohmage.domain.MobilityInformation.LocationStatus}.</li>
 *         <li><b>location</b>: The location information obtained when this
 *           Mobility data point was created. It may be absent if the
 *           'location_status' is
 *           {@value org.ohmage.domain.MobilityInformation.LocationStatus#UNAVAILABLE}.
 *           If present, it must contain the following information:
 *           <ul>
 *             <li><b>latitude</b>: The latitude of the device.</li>
 *             <li><b>longitude</b>: The longitude of the device.</li>
 *             <li><b>accuracy</b>: A double value representing the radius of
 *               the area in which the device most-likely was from the center
 *               as defined by the 'latitude' and 'longitude'.</li>
 *             <li><b>provider</b>: A string representing who supplied the GPS
 *               coordinates and accuracy.</li>
 *             <li><b>timestamp</b>: A date and time in the ISO 8601 format
 *               representing the date and time when this specific location 
 *               value was collected.</li>
 *           </ul></li>
 *       </ul>
 *       SubType: 
 *       {@value org.ohmage.domain.MobilityInformation.SubType#MODE_ONLY}
 *       <ul>
 *         <li><b>mode</b>: The device-calculated mode of the user; one of
 *           {@link org.ohmage.domain.MobilityInformation.Mode}</li>
 *       </ul>
 *       SubType:
 *       {@value org.ohmage.domain.MobilityInformation.SubType#SENSOR_DATA}
 *       <ul>
 *         <li><b>data</b>: The collected sensor data used to calculate the 
 *           user's mode. This must contain the following:
 *           <ul>
 *             <li><b>mode</b>: The device-calculated mode of the user; one of
 *               {@link org.ohmage.domain.MobilityInformation.Mode}</li>
 *             <li><b>speed</b>: A double value approximating the speed of the
 *               user.</li>
 *             <li><b>accel_data</b>: A JSONArray of JSONObjects representing
 *               the accelerometer data collected and used to determine this
 *               mode. Each JSONObject (accelerometer reading) must have the
 *               following format:
 *               <ul>
 *                 <li><b>x</b>: The 'x'-acceleration of the device.</li>
 *                 <li><b>y</b>: The 'y'-acceleration of the device.</li>
 *                 <li><b>z</b>: The 'z'-acceleration of the device.</li>
 *               </ul></li>
 *             <li><b>wifi_data</b>: A JSONObject explaining the WiFi data that
 *               was collected and used to calculate this mode. Each JSONObject
 *               must have the following format:
 *               <ul>
 *                 <li><b>scan</b>: A JSONArray of JSONObjects where each 
 *                   JSONObject represents the information about a single 
 *                   access point that whose information was gathered during 
 *                   the scan. Each JSONObject must have the following format:
 *                   <ul>
 *                     <li><b>ssid</b>: The SSID of the access point.</li>
 *                     <li><b>strength</b>: The strength as measured by radios
 *                       representing the strength of the signal from this
 *                       access point.</li>
 *                   </ul></li>
 *                 <li><b>timestamp</b>: A date and time in the ISO8601 format
 *                   representing the date and time when this WiFi scan took
 *                   place.</li>
 *               </ul></li>
 *           </ul></li>
 *       </ul></td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityUploadRequest.class);
	
	private final List<MobilityInformation> data;
	
	/**
	 * Creates a Mobility upload request.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the parameters for
	 * 					  this request.
	 */
	public MobilityUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, false);
		
		LOGGER.info("Creating a Mobility upload request.");
		
		List<MobilityInformation> tData = null;
		
		if(! isFailed()) {
			try {
				String[] dataArray = getParameterValues(InputKeys.DATA);
				if(dataArray.length == 0) {
					setFailed(ErrorCodes.MOBILITY_INVALID_DATA, "The upload data is missing: " + ErrorCodes.MOBILITY_INVALID_DATA);
					throw new ValidationException("The upload data is missing: " + ErrorCodes.MOBILITY_INVALID_DATA);
				}
				else if(dataArray.length > 1) {
					setFailed(ErrorCodes.MOBILITY_INVALID_DATA, "Multiple data parameters were given: " + ErrorCodes.MOBILITY_INVALID_DATA);
					throw new ValidationException("Multiple data parameters were given: " + ErrorCodes.MOBILITY_INVALID_DATA);
				}
				else {
					tData = MobilityValidators.validateDataAsJsonArray(this, dataArray[0]);
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		data = tData;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility upload request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Running the server-side classifier.");
			MobilityServices.classifyData(this, data);
			
			LOGGER.info("Storing the Mobility data points.");
			MobilityServices.createMobilityPoint(this, getUser().getUsername(), getClient(), data);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with either a success message or a failure 
	 * message that contains an error code and an error text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobility upload request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}