package org.ohmage.request.mobility;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.MobilityValidators;

/**
 * Reads the Mobility information about a user during a single day.<br />
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
 *     <td>{@value org.ohmage.request.InputKeys#DATE}</td>
 *     <td>The date for which the data is desired.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadRequest.class);
	
	private final Date date;
	
	private List<MobilityInformation> result;
	
	/**
	 * Creates a Mobility read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public MobilityReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a Mobility read request.");
		
		Date tDate = null;
		
		if(! isFailed()) {
			try {
				String[] dates = getParameterValues(InputKeys.DATE);
				if(dates.length == 0) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The date value is missing: " + InputKeys.DATE);
					throw new ValidationException("The date value is missing: " + InputKeys.DATE);
				}
				else if(dates.length == 1) {
					tDate = MobilityValidators.validateDate(this, dates[0]);
					
					if(tDate == null) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "The date value is missing: " + InputKeys.DATE);
						throw new ValidationException("The date value is missing: " + InputKeys.DATE);
					}
				}
				else {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Multiple date values were given: " + InputKeys.DATE);
					throw new ValidationException("Multiple date values were given: " + InputKeys.DATE);
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		date = tDate;
		
		result = Collections.emptyList();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			Calendar startDate = TimeUtils.convertDateToCalendar(date);
			startDate.set(Calendar.MILLISECOND, 0);
			startDate.set(Calendar.SECOND, 0);
			startDate.set(Calendar.MINUTE, 0);
			startDate.set(Calendar.HOUR_OF_DAY, 0);
			Calendar endDate = new GregorianCalendar();
			endDate.setTimeInMillis(startDate.getTimeInMillis());
			endDate.add(Calendar.DAY_OF_YEAR, 1);
			
			result = MobilityServices.retrieveMobilityData(
					this, 
					getUser().getUsername(), 
					null, 
					new Date(startDate.getTimeInMillis()), 
					new Date(endDate.getTimeInMillis()), 
					null, 
					null, 
					null);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobiltiy read request.");

		JSONArray resultJson = new JSONArray();
		
		for(MobilityInformation mobilityPoint : result) {
			resultJson.put(mobilityPoint.toJson(true));
		}
			
		respond(httpRequest, httpResponse, JSON_KEY_DATA, resultJson);
	}
}