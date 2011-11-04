package org.ohmage.query;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audit;
import org.ohmage.exception.DataAccessException;
import org.ohmage.jee.servlet.RequestServlet;
import org.ohmage.validator.AuditValidators.ResponseType;

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public interface IAuditQueries {

	/**
	 * Creates an audit entry with the parameterized information. Not all 
	 * information is required; see the specific parameters for details.
	 * 
	 * @param requestType The RequestType of the request. Required.
	 * 
	 * @param uri The URI of the request. Required.
	 * 
	 * @param client The value of the client parameter. Not required.
	 * 
	 * @param deviceId An unique identifier for each device. Not required.
	 * 
	 * @param parameters A map of parameter keys to all of their values. Not
	 * 					 required.
	 * 
	 * @param extras A map of keys from the HTTP request header to their 
	 * 				 values.
	 * 
	 * @param response A string that should have the format of a JSONObject
	 * 				   indicating whether or not the request succeed or failed.
	 * 				   If the request succeed, that is all that needs to be
	 * 				   passed; passing the data that was returned to the 
	 * 				   requesting user would create too much duplicate data and
	 * 				   may leak private information. If the request failed, the
	 * 				   error code and error text should be included in this 
	 * 				   JSONObject string.
	 * 
	 * @param receivedMillis A millisecond-level epoch-based time at which the
	 * 						 request was received. This should be obtained by
	 * 						 the same mechanism as 'respondMillis'. Required.
	 *  
	 * @param respondMillis A millisecond-level epoch-based time at which the
	 * 						request was received. This should be obtained by
	 * 						the same mechanism as 'receivedMillis'. Required.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are null.
	 */
	void createAudit(
			RequestServlet.RequestType requestType, String uri,
			String client, String deviceId,
			Map<String, String[]> parameters,
			Map<String, String[]> extras, String response,
			long receivedMillis, long respondMillis)
			throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAudits() throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits with a specific HTTP request 
	 * type.
	 * 
	 * @param requestType The HTTP request type. One of
	 * 					  {@link org.ohmage.jee.servlet.RequestServlet.RequestType}.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAuditsWithRequestType(RequestServlet.RequestType requestType) throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits with a specific URI.
	 * 
	 * @param uri The URI.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAuditsWithUri(URI uri) throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits with a specific client.
	 * 
	 * @param client The client.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAuditsWithClient(String client) throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits with a specific device ID.
	 * 
	 * @param deviceId The device's ID.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAuditsWithDeviceId(String deviceId) throws DataAccessException;

	/**
	 * Retrieves the unique ID for all audits whose response was one of 
	 * {@link org.ohmage.validator.AuditValidators.ResponseType}. If the 
	 * response type is 
	 * {@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE}, then
	 * it can be further limited by only those audits with a specific error
	 * code. If 'errorCode' is null all audits of failed requests will be 
	 * returned.
	 * 
	 * @param responseTypes The resulting response returned to the user, one of
	 * 						{@link org.ohmage.validator.AuditValidators.ResponseType}.
	 * 
	 * @param errorCode The error code to further limit the results if the 
	 * 					'responseTypes' is
	 * 					{@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE}.
	 * 					If this is null, all failed request audits will be
	 * 					returned.
	 * 
	 * @return A list of audit IDs.
	 */
	List<Long> getAllAuditsWithResponse(ResponseType responseType, ErrorCode errorCode) throws DataAccessException;

	/**
	 * Retrieves the unique IDs for all audits that were recorded on or after
	 * some date.
	 * 
	 * @param date The date.
	 * 
	 * @return A list of unique audit IDs.
	 */
	 List<Long> getAllAuditsOnOrAfterDate(Date date) throws DataAccessException;

	/**
	 * Retrieves the unique IDs for all audits that were recorded on or before
	 * some date.
	 * 
	 * @param date The date.
	 * 
	 * @return A list of unique audit IDs.
	 */
	List<Long> getAllAuditsOnOrBeforeDate(Date date) throws DataAccessException;

	/**
	 * Retrieves the unique IDs for all audits that were recorded on or between
	 * two dates.
	 * 
	 * @param startDate The earlier of the two date to which the audits will be
	 * 					searched. 
	 * 
	 * @param endDate The latter of the two dates to which the audits will be
	 * 				  searched.
	 * 
	 * @return A list of unique audit IDs.
	 */
	List<Long> getAllAuditsOnOrBetweenDates(Date startDate, Date endDate) throws DataAccessException;

	/**
	 * Retrieves all the information about a list of audit IDs.
	 * 
	 * @param auditIds The unique identifiers for some audits.
	 * 
	 * @return Returns a list of Audit objects, one for each audit
	 * 		   ID.
	 */
	List<Audit> readAuditInformation(List<Long> auditIds) throws DataAccessException;
}