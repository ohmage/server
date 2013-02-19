package org.ohmage.request.mobility;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.ColumnKey;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.MobilityValidators;
import org.ohmage.validator.UserValidators;

/**
 * Reads the Mobility information about a user during a specified period of 
 * time.<br />
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
 *     <td>Limits the results to only those on or after this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>Limits the results to only those on or before this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user for whom the data is desired. If omitted,
 *       the requesting user is used.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#COLUMN_LIST}
 *       </td>
 *     <td>A list of the columns to return data. The order in this list will be
 *       reflected in the resulting list. If omitted, the result will be all of
 *       the columns available.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class MobilityReadCsvRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityReadCsvRequest.class);

	private final DateTime startDate;
	private final DateTime endDate;
	private final String username;
	private final List<ColumnKey> columns;
	
	private List<MobilityPoint> points;
	
	/**
	 * Creates a Mobility read request where the result is a CSV file 
	 * attachment on success and JSON when it fails.
	 * 
	 * @param httpRequest The HttpServletRequest for this information.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityReadCsvRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		String tUsername = null;
		List<ColumnKey> tColumns = MobilityColumnKey.ALL_COLUMNS;
		
		if(! isFailed()) {
			try {
				String[] t;
				
				// Get the start date.
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start dates were given: " + 
									InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = MobilityValidators.validateDate(t[0]);
					
					if(tStartDate == null) {
						tStartDate = new DateTime(0);
					}
				}
				
				// Get the end date.
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple end dates were given: " + 
									InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = MobilityValidators.validateDate(t[0]);
					
					if(tEndDate == null) {
						tEndDate = new DateTime();
					}
				}
				
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
				
				t = getParameterValues(InputKeys.COLUMN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.MOBILITY_INVALID_COLUMN_LIST,
							"Multiple column lists were given: " +
									InputKeys.COLUMN_LIST);
				}
				else if(t.length == 1) {
					tColumns = MobilityValidators.validateColumns(t[0], false);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		startDate = tStartDate;
		endDate = tEndDate;
		username = tUsername;
		columns = tColumns;
		
		points = new ArrayList<MobilityPoint>(0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if((username != null) && (! username.equals(getUser().getUsername()))) {
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
					UserServices.instance().verifyUserIsAdmin(
							getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					if(isPlausible) {
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
			
			points = MobilityServices.instance().retrieveMobilityData(
					(username == null) ? getUser().getUsername() : username,
					startDate, 
					endDate, 
					null, 
					null, 
					null);
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
		
		LOGGER.info("Responding to a Mobility CSV read call.");
		
		// If we have failed, let the parent take care of that.
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
			return;
		}
		
		// Creates the writer that will write the response, success or fail.
		OutputStream os;
		try {
			os = getOutputStream(httpRequest, httpResponse);
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
				
		try {
			// Set the type and force the browser to download it as the 
			// last step before beginning to stream the response.
			httpResponse.setContentType("text/csv");
			httpResponse.setHeader("Content-Disposition", "attachment; filename=Mobility.csv");
			
			// If available, set the token.
			if(getUser() != null) {
				final String token = getUser().getToken(); 
				if(token != null) {
					CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token);
				}
			}
			
			boolean firstPass = true;
			int numColumns = 0;
			for(ColumnKey column : columns) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					os.write(',');
				}
				
				os.write(column.toString().getBytes());
				numColumns++;
			}
			os.write('\n');
			
			List<Object> emptyList = new ArrayList<Object>(numColumns);
			for(int i = 0; i < numColumns; i++) {
				emptyList.add(null);
			}
			
			for(MobilityPoint point : points) {
				List<Object> currResult = new ArrayList<Object>(emptyList);
				point.toCsvRow(columns, currResult);
				
				firstPass = true;
				for(Object currColumn : currResult) {
					if(firstPass) {
						firstPass = false;
					}
					else {
						os.write(',');
					}
					
					if(currColumn == null) {
						continue;
					}
					
					if(currColumn instanceof Number) {
						os.write(((Number) currColumn).toString().getBytes());
					}
					else {
						os.write('"');
						
						if(currColumn instanceof Collection) {
							boolean innerFirstPass = true;
							for(Object currItem : (Collection<?>) currColumn) {
								if(innerFirstPass) {
									innerFirstPass = false;
								}
								else {
									os.write(',');
								}
								
								os.write(currItem.toString().getBytes());
							}
						}
						else {
							os.write(currColumn.toString().getBytes());
						}
						
						os.write('"');
					}
				}
				
				os.write('\n');
			}
			
			// Flush and close the output stream that was used to generate
			// the data output stream.
			os.flush();
			os.close();
		}
		// If we fail while writing to the output stream, then the connection 
		// was broken and there is nothing we can do.
		catch(IOException e) {
			LOGGER.error("The contents of the file could not be read or written to the response.", e);
			setFailed();
		}
		// If we fail while creating a result, we are mid writing to the output
		// stream, so we are simply in trouble.
		catch(DomainException e) {
			LOGGER.error("Could not create a CSV row.", e);
			setFailed();
			httpResponse.setStatus(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}