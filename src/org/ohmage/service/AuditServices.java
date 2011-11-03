package org.ohmage.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.Audit;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.jee.servlet.RequestServlet;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.query.IAuditQueries;
import org.ohmage.validator.AuditValidators.ResponseType;

/**
 * This class is responsible for all actions taken regarding audits only. This
 * includes creating audit entries and reading their contents.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class AuditServices {
	private static AuditServices instance;
	private IAuditQueries auditQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iAuditQueries is null
	 */
	private AuditServices(IAuditQueries iAuditQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iAuditQueries == null) {
			throw new IllegalArgumentException("An instance of IAuditQueries is required.");
		}
		
		auditQueries = iAuditQueries;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static AuditServices instance() {
		return instance;
	}
	
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
	 * @param parameterMap A map of parameter keys to all of their values. Not
	 * 					   required.
	 * 
	 * @param extras A map of keys to their values for the parameters in the
	 * 				 HTTP header.
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
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createAudit(
			final RequestServlet.RequestType requestType, final String uri, 
			final String client, final String deviceId, final String response,
			final Map<String, String[]> parameterMap, 
			final Map<String, String[]> extras, final long receivedTimestamp, 
			final long respondTimestamp) throws ServiceException {
		
		try {
			auditQueries.createAudit(requestType, uri, client, deviceId, parameterMap, extras, response, receivedTimestamp, respondTimestamp);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the information about all audits that meet the parameterized
	 * criteria. If all of the parameters are null, except 'request' which 
	 * isn't allowed to be null, then all the information about all of the 
	 * audits is returned.
	 *  
	 * @param requestType Limits the results to only those with this 
	 * 					  RequestType. Not required.
	 * 
	 * @param uri Limits the results to only those with this URI. Not required.
	 * 
	 * @param client Limits the results to only those with this client value.
	 * 				 Not required.
	 * 
	 * @param deviceId Limits the results to only those with this device ID.
	 * 				   Not required.
	 * 
	 * @param responseType Limits the results to only those with this 
	 * 					   ResponseType. Not required.
	 * 
	 * @param errorCode If 'responseType' is type
	 * 					{@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE},
	 * 					this can be used to only get those requests that failed
	 * 					with this error code. Not required.
	 * 
	 * @param startDate Limits the results to only those that were recorded on 
	 * 					or after this date. Not required.
	 * 
	 * @param endDate Limits the results to only those that were recorded on or
	 * 				  after this date. Not required.
	 *   
	 * @return A list of information about all of the audits that satisified
	 * 		   all of the requests.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<Audit> getAuditInformation(
			final RequestType requestType, final String uri, 
			final String client, final String deviceId, 
			final ResponseType responseType, final String errorCode, 
			final Date startDate, final Date endDate) throws ServiceException {
		
		try {
			List<Long> auditIds = null;
			
			if(requestType != null) {
				auditIds = auditQueries.getAllAuditsWithRequestType(requestType);
			}
			
			if(uri != null) {
				if(auditIds == null) {
					auditIds = auditQueries.getAllAuditsWithUri(uri);
				}
				else {
					auditIds.retainAll(auditQueries.getAllAuditsWithUri(uri));
				}
			}
			
			if(client != null) {
				if(auditIds == null) {
					auditIds = auditQueries.getAllAuditsWithClient(client);
				}
				else {
					auditIds.retainAll(auditQueries.getAllAuditsWithClient(client));
				}
			}
			
			if(deviceId != null) {
				if(auditIds == null) {
					auditIds = auditQueries.getAllAuditsWithDeviceId(deviceId);
				}
				else {
					auditIds.retainAll(auditQueries.getAllAuditsWithDeviceId(deviceId));
				}
			}
			
			if(responseType != null) {
				if(auditIds == null) {
					auditIds = auditQueries.getAllAuditsWithResponse(responseType, errorCode);
				}
				else {
					auditIds.retainAll(auditQueries.getAllAuditsWithResponse(responseType, errorCode));
				}
			}
			
			if(startDate != null) {
				if(endDate == null) {
					if(auditIds == null) {
						auditIds = auditQueries.getAllAuditsOnOrAfterDate(startDate);
					}
					else {
						auditIds.retainAll(auditQueries.getAllAuditsOnOrAfterDate(startDate));
					}
				}
				else {
					if(auditIds == null) {
						auditIds = auditQueries.getAllAuditsOnOrBetweenDates(startDate, endDate);
					}
					else {
						auditIds.retainAll(auditQueries.getAllAuditsOnOrBetweenDates(startDate, endDate));
					}
				}
			}
			else if(endDate != null) {
				if(auditIds == null) {
					auditIds = auditQueries.getAllAuditsOnOrBeforeDate(endDate);
				}
				else {
					auditIds.retainAll(auditQueries.getAllAuditsOnOrBeforeDate(endDate));
				}
			}
			
			if(auditIds == null) {
				auditIds = auditQueries.getAllAudits();
			}
			
			return auditQueries.readAuditInformation(auditIds);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}