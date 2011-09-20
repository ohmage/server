package org.ohmage.request.mobility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.domain.MobilityInformation.Location;
import org.ohmage.domain.MobilityInformation.LocationStatus;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.MobilityValidators;

/**
 * Gathers the Mobility information about the user and the combines the 
 * information into chunks to reduce its size.<br />
 * <br />
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
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The earliest date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The latest date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 * </table> 
 * 
 * @author John Jenkins
 */
public class MobilityReadChunkedRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadChunkedRequest.class);
	
	private static final String JSON_KEY_MODE_COUNT = "mc";
	private static final String JSON_KEY_DURATION = "d";
	private static final String JSON_KEY_TIMESTAMP = "ts";
	private static final String JSON_KEY_TIMEZONE = "tz";
	private static final String JSON_KEY_LOCATION_STATUS = "ls";
	private static final String JSON_KEY_LOCATION = "l";
	
	private static final int POINTS_PER_CHUNK = 10;
	// 10 days
	private static final long MAX_MILLIS_BETWEEN_START_AND_END_DATES = 1000 * 60 * 60 * 24 * 10; 
	
	private final Date startDate;
	private final Date endDate;
	
	private List<MobilityInformation> result;
	
	/**
	 * Creates a new Mobility read chunked request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 */
	public MobilityReadChunkedRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a Mobility read chunked request.");
		
		Date tStartDate = null;
		Date tEndDate = null;
		
		if(! isFailed()) {
			try {
				String[] startDates = getParameterValues(InputKeys.START_DATE);
				if(startDates.length == 0) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date is missing: " + InputKeys.START_DATE);
					throw new ValidationException("The start date is missing: " + InputKeys.START_DATE);
				}
				else if(startDates.length == 1) {
					tStartDate = MobilityValidators.validateDate(this, startDates[0]);
					
					if(tStartDate == null) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date is missing: " + InputKeys.START_DATE);
						throw new ValidationException("The start date is missing: " + InputKeys.START_DATE);
					}
				}
				else {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Multiple start dates were given: " + InputKeys.START_DATE);
					throw new ValidationException("Multiple start dates were given: " + InputKeys.START_DATE);
				}
				
				String[] endDates = getParameterValues(InputKeys.END_DATE);
				if(endDates.length == 0) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date is missing: " + InputKeys.END_DATE);
					throw new ValidationException("The end date is missing: " + InputKeys.END_DATE);
				}
				else if(endDates.length == 1) {
					tEndDate = MobilityValidators.validateDate(this, endDates[0]);
					
					if(tEndDate == null) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date is missing: " + InputKeys.END_DATE);
						throw new ValidationException("The end date is missing: " + InputKeys.END_DATE);
					}
				}
				else {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Multiple end dates were given: " + InputKeys.END_DATE);
					throw new ValidationException("Multiple end dates were given: " + InputKeys.END_DATE);
				}
				
				Date latestDate = new Date(tStartDate.getTime() + MAX_MILLIS_BETWEEN_START_AND_END_DATES);
				if(tEndDate.after(latestDate)) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The maximum time range between the start and end dates is 10 days.");
					throw new ValidationException("The maximum time range between the start and end dates is 10 days.");
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		startDate = tStartDate;
		endDate = tEndDate;
		
		result = Collections.emptyList();
	}

	/**
	 * Service the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility read chunked request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			result = MobilityServices.retrieveMobilityData(
					this, 
					getUser().getUsername(), 
					null, 
					startDate, 
					endDate, 
					null, 
					null, 
					null);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the Mobility read chunked request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobility read chunked request.");
		
		JSONArray resultJson = new JSONArray();
		
		List<MobilityInformation> chunk = new ArrayList<MobilityInformation>(POINTS_PER_CHUNK);
		ListIterator<MobilityInformation> resultIter = result.listIterator();
		while(resultIter.hasNext()) {
			MobilityInformation mobilityPoint = resultIter.next();
			chunk.add(mobilityPoint);
			if((chunk.size() == POINTS_PER_CHUNK) || (! resultIter.hasNext())) {
				try {
					// Process chunk.
					JSONObject chunkJson = new JSONObject();
					
					// Create the variables that will hold the results.
					Map<String, Integer> modeCount = new HashMap<String, Integer>();
					Date earliestPoint = new Date(Long.MAX_VALUE);
					Date latestPoint = new Date(0);
					TimeZone earliestTimeZone = null;
					LocationStatus earliestLocationStatus = null;
					Location earliestLocation = null;
					
					// Cycle through each of the points in the chunk and gather
					// the appropriate information about each.
					for(MobilityInformation chunkPoint : chunk) {
						// Increase the count for this point's mode.
						String modeString = chunkPoint.getMode().toString().toLowerCase();
						Integer count = modeCount.get(modeString);
						if(count == null) {
							modeCount.put(modeString, 1);
						}
						else {
							modeCount.put(modeString, count + 1);
						}
						
						// Figure out if this is the earliest or latest point.
						Date date = chunkPoint.getDate();
						if(date.before(earliestPoint)) {
							earliestPoint = date;
							earliestTimeZone = chunkPoint.getTimezone();
							earliestLocationStatus = chunkPoint.getLocationStatus();
							earliestLocation = chunkPoint.getLocation();
						}
						if(date.after(latestPoint)) {
							latestPoint = date;
						}
					}
					chunkJson.put(JSON_KEY_MODE_COUNT, modeCount);
					chunkJson.put(JSON_KEY_DURATION, latestPoint.getTime() - earliestPoint.getTime());
					chunkJson.put(JSON_KEY_TIMESTAMP, TimeUtils.getIso8601DateTimeString(earliestPoint));
					chunkJson.put(JSON_KEY_TIMEZONE, earliestTimeZone.getID());
					chunkJson.put(JSON_KEY_LOCATION_STATUS, earliestLocationStatus.toString().toLowerCase());
					chunkJson.put(JSON_KEY_LOCATION, ((earliestLocation == null) ? null : earliestLocation.toJson(true).toString()));
					
					resultJson.put(chunkJson);
				}
				catch(JSONException e) {
					LOGGER.error("Error building resulting JSONObject.");
					setFailed();
					break;
				}
				chunk.clear();
			}
		}
		
		super.respond(httpRequest, httpResponse, JSON_KEY_DATA, resultJson);
	}
}