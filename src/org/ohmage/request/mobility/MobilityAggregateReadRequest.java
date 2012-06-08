package org.ohmage.request.mobility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.SubType;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.MobilityValidators;

/**
 * Gathers the Mobility information between the given dates and aggregates the
 * time for each mode in each chunk where a chunk's length is the given
 * duration.<br />
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
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#MOBILITY_AGGREGATE_DURATION}</td>
 *     <td>The number of milliseconds which defines the size of the buckets.
 *       </td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The user on which to gather the data.</td>
 *     <td>true</td>
 *   </tr>
 * </table> 
 * 
 * @author John Jenkins
 */
public class MobilityAggregateReadRequest extends UserRequest {
	private static final String JSON_KEY_TIMESTAMP = "timestamp";
	private static final String JSON_KEY_DATA = "data";
	private static final String JSON_KEY_MODE = "mode";
	private static final String JSON_KEY_DURATION = "duration";
	
	private static final Logger LOGGER = 
			Logger.getLogger(MobilityAggregateReadRequest.class);
	
	private final DateTime startDate;
	//private final DateTime endDate;
	private final Long duration;
	//private final String username;
	
	private final StreamReadRequest regularReadRequest;
	private final StreamReadRequest extendedReadRequest;
	
	private List<MobilityPoint> points;
	
	/**
	 * Creates a new Mobility aggregate read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityAggregateReadRequest(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		DateTime tStartDate = null;
		//DateTime tEndDate = null;
		Long tDuration = null;
		//String tUsername = null;
		
		StreamReadRequest tRegularReadRequest = null;
		StreamReadRequest tExtendedReadRequest = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a Mobility aggregate read request.");
			String[] t;
			
			try {
				// Get the start date.
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The start date is missing: " + 
									InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = MobilityValidators.validateDate(t[0]);
				}
				if(tStartDate == null) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start date was not given: " + 
									InputKeys.START_DATE);
				}
				
				// Get the end date.
				DateTime endDate = null;
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"The end date is missing: " + 
									InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					endDate = MobilityValidators.validateDate(t[0]);
				}
				if(endDate == null) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple end dates were given: " + 
									InputKeys.END_DATE);
				}
				
				// Get the duration.
				t = getParameterValues(InputKeys.MOBILITY_AGGREGATE_DURATION);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_AGGREGATE_DURATION,
							"Multiple aggregate durations were given: " +
								InputKeys.MOBILITY_AGGREGATE_DURATION);
				}
				else if(t.length == 1) {
					tDuration = 
							MobilityValidators.validateAggregateDuration(t[0]);
				}
				if(tDuration == null) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_AGGREGATE_DURATION,
							"A duration was not given: " +
								InputKeys.MOBILITY_AGGREGATE_DURATION);
				}
				
				/*
				// Get the user.
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME, 
							"Multiple usernames to query were given: " + 
									InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tUsername = UserValidators.validateUsername(t[0]);
				}
				*/
				
				// Always get all of the columns.
				tRegularReadRequest = 
					new StreamReadRequest(
						httpRequest,
						getParameterMap(),
						"edu.ucla.cens.Mobility",
						null,
						"regular",
						2012050700,
						tStartDate,
						endDate,
						null,
						null,
						null);
				
				tExtendedReadRequest = 
					new StreamReadRequest(
						httpRequest,
						getParameterMap(),
						"edu.ucla.cens.Mobility",
						null,
						"extended",
						2012050700,
						tStartDate,
						endDate,
						null,
						null,
						null);
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		startDate = tStartDate;
		//endDate = tEndDate;
		duration = tDuration;
		//username = tUsername;
		
		regularReadRequest = tRegularReadRequest;
		extendedReadRequest = tExtendedReadRequest;
		
		points = new ArrayList<MobilityPoint>(); 
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		/*
		LOGGER.info("Servicing the Mobility aggregate read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if((username != null) && (! username.equals(getUser().getUsername()))) {
				LOGGER.info("Checking if reading Mobility points about another user is even allowed.");
				boolean isPlausible;
				try {
					isPlausible = 
							StringUtils.decodeBoolean(
									PreferenceCache.instance().lookup(
											PreferenceCache.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
				}
				catch(CacheMissException e) {
					throw new ServiceException(e);
				}
				
				try {
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
							getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");
					if(isPlausible) {
						LOGGER.info("Checking if the requester is allowed to read Mobility points about the user.");
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
									getUser().getUsername(), 
									username);
					}
					else {
						throw new ServiceException(
								ErrorCode.MOBILITY_INSUFFICIENT_PERMISSIONS,
								"A user is not allowed to query Mobility information about another user.");
					}
				}
				
				UserServices.instance().checkUserExistance(username, true);
			}
			
			LOGGER.info("Gathering the Mobility points.");
			points = MobilityServices.instance().retrieveMobilityAggregateData(
					(username == null) ? getUser().getUsername() : username,
					startDate, 
					endDate);
			LOGGER.info("Found " + points.size() + " results.");
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		*/
		
		// If any of the sub-requests have failed, then return.
		if(regularReadRequest.isFailed() || extendedReadRequest.isFailed()) {
			return;
		}
		
		LOGGER.info("Servicing the Mobility read request.");
		
		try {
			/*
			if((username != null) && (! username.equals(getUser().getUsername()))) {
				LOGGER.info("Checking if reading Mobility points about another user is even allowed.");
				boolean isPlausible;
				try {
					isPlausible = 
							StringUtils.decodeBoolean(
									PreferenceCache.instance().lookup(
											PreferenceCache.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
				}
				catch(CacheMissException e) {
					throw new ServiceException(e);
				}
				
				try {
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
							getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");
					if(isPlausible) {
						LOGGER.info("Checking if the requester is allowed to read Mobility points about the user.");
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
									getUser().getUsername(), 
									username);
					}
					else {
						throw new ServiceException(
								ErrorCode.MOBILITY_INSUFFICIENT_PERMISSIONS,
								"A user is not allowed to query Mobility information about another user.");
					}
				}
				
				UserServices.instance().checkUserExistance(username, true);
			}
			*/
			
			// Service the read requests.
			regularReadRequest.service();
			if(regularReadRequest.isFailed()) {
				return;
			}
			extendedReadRequest.service();
			if(extendedReadRequest.isFailed()) {
				return;
			}
			
			LOGGER.info("Aggregating the resulting points.");
			Collection<DataStream> regularResults = 
				regularReadRequest.getResults();
			for(DataStream dataStream : regularResults) {
				try {
					points.add(
						new MobilityPoint(
							dataStream, 
							SubType.MODE_ONLY,
							MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException e) {
					throw new ServiceException(
						"One of the points was invalid.",
						e);
				}
			}

			Collection<DataStream> extendedResults = 
				extendedReadRequest.getResults();
			for(DataStream dataStream : extendedResults) {
				try {
					points.add(
						new MobilityPoint(
							dataStream, 
							SubType.SENSOR_DATA,
							MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException e) {
					throw new ServiceException(
						"One of the points was invalid.",
						e);
				}
			}
			
			LOGGER.info("Sorting the aggregated points.");
			Collections.sort(points);
			
			// Run them through the classifier.
			LOGGER.info("Classifying the points.");
			MobilityServices.instance().classifyData(
				regularReadRequest.getUser().getUsername(),
				points);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {
		
		LOGGER.info("Responding to the Mobility aggregate read request.");
		
		if(! isFailed()) {
			try {
				// Get the "day value" or "bucket number" for the start date.
				int startYear = startDate.getYear() - 1;
				int startBucketNum =
					(startYear * 365) + (startYear / 4) - (startYear / 100) + (startYear / 400);
				startBucketNum += startDate.getDayOfYear();
				
				// Bucket the data based on its duration. The first bucket begins at 
				// the start date not at the earliest point.
				Map<Long, List<MobilityPoint>> buckets = 
						new HashMap<Long, List<MobilityPoint>>();
				for(MobilityPoint mobilityPoint : points) {
					// Get the point's date and time.
					DateTime pointDateTime = mobilityPoint.getDate();
					
					// Calculate this points "day value".
					int year = pointDateTime.getYear() - 1;
					long bucketNum =
						(year * 365) + (year / 4) - (year / 100) + (year / 400);
					bucketNum += pointDateTime.getDayOfYear();
					
					// Subtract the starting bucket number to get a  
					// zero-indexed bucket number.
					bucketNum -= startBucketNum;
					
					// Figure out the actual bucket number based on the 
					// duration.
					bucketNum /= duration;
					
					// Add this point to its appropriate bucket.
					List<MobilityPoint> bucket = 
						buckets.get(bucketNum);
					if(bucket == null) {
						bucket = new LinkedList<MobilityPoint>();
						buckets.put(bucketNum, bucket);
					}
					bucket.add(mobilityPoint);
				}
				
				JSONArray result = new JSONArray();
				
				// Parse each bucket.
				for(Long bucketNum : buckets.keySet()) {
					// Get the buckets.
					List<MobilityPoint> mobilityPoints = 
							buckets.get(bucketNum);
					
					// Create a map to hold the mode to duration times.
					JSONObject currResult = new JSONObject();
					result.put(currResult);
					
					// Calculate the start of the chunk.
					DateTime timestamp = 
						startDate.plusDays((int) (bucketNum * duration));
					
					// Compute the starting time stamp for this chunk.
					currResult.put(
						JSON_KEY_TIMESTAMP, 
						TimeUtils.getIso8601DateString(timestamp, false));
					
					// Create the data array.
					JSONArray data = new JSONArray();
					currResult.put(JSON_KEY_DATA, data);
					
					Map<MobilityPoint.Mode, JSONObject> modeToObjectMap =
							new HashMap<MobilityPoint.Mode, JSONObject>();
					
					// Go from point to point looking backwards to determine 
					// how much time should be added to this mode.
					MobilityPoint previousPoint = null;
					for(MobilityPoint mobilityPoint : mobilityPoints) {
						// Get this point's mode.
						MobilityPoint.Mode mode = mobilityPoint.getMode();
						
						// Retrieve the mode/duration JSONObject for this chunk
						// and mode or create it with an initial duration of 0 
						// if it doesn't exist.
						JSONObject modeDurationObject = 
								modeToObjectMap.get(mode);
						if(modeDurationObject == null) {
							modeDurationObject = new JSONObject();
							modeDurationObject.put(
									JSON_KEY_MODE, 
									mode.toString().toLowerCase());
							modeDurationObject.put(JSON_KEY_DURATION, 0);
							
							data.put(modeDurationObject);
							modeToObjectMap.put(mode, modeDurationObject);
						}
						
						// Get the current duration.
						long duration = 
								modeDurationObject.getLong(JSON_KEY_DURATION);
						
						// Compute the additional duration for this mode.
						long additionalDuration;
						if(previousPoint == null) {
							additionalDuration = 60000;
						}
						else {
							long difference = 
									mobilityPoint.getDate().getMillis() - 
									previousPoint.getDate().getMillis();
							
							additionalDuration = 
								(difference <= 3600000) ? difference : 60000;
						}
						
						// Update the duration with the additional duration.
						modeDurationObject.put(
								JSON_KEY_DURATION, 
								duration + additionalDuration);
						
						previousPoint = mobilityPoint;
					}
				}
				
				super.respond(
						httpRequest, 
						httpResponse, 
						UserRequest.JSON_KEY_DATA, 
						result);
			}
			catch(JSONException e) {
				setFailed();
				LOGGER.error("There was a problem building the result.", e);
			}
		}
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
	}
}