package org.ohmage.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ohmage.dao.AuditDaos;
import org.ohmage.domain.AuditInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.jee.servlet.RequestServlet;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.request.Request;
import org.ohmage.validator.AuditValidators.ResponseType;

/**
 * This class is responsible for all actions taken regarding audits only. This
 * includes creating audit entries and reading their contents.
 * 
 * @author John Jenkins
 */
public class AuditServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private AuditServices() {}
	
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
	public static void createAudit(RequestServlet.RequestType requestType, String uri, String client, String deviceId, String response,
			Map<String, String[]> parameterMap, Map<String, String[]> extras, long receivedTimestamp, long respondTimestamp) throws ServiceException {
		try {
			AuditDaos.createAudit(requestType, uri, client, deviceId, parameterMap, extras, response, receivedTimestamp, respondTimestamp);
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
	 * @param request The Request that is performing this service. Required.
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
	public static List<AuditInformation> getAuditInformation(Request request, RequestType requestType, String uri, String client, 
			String deviceId, ResponseType responseType, String errorCode, Date startDate, Date endDate) throws ServiceException {
		try {
			List<Long> auditIds = null;
			
			if(requestType != null) {
				auditIds = AuditDaos.getAllAuditsWithRequestType(requestType);
			}
			
			if(uri != null) {
				if(auditIds == null) {
					auditIds = AuditDaos.getAllAuditsWithUri(uri);
				}
				else {
					auditIds.retainAll(AuditDaos.getAllAuditsWithUri(uri));
				}
			}
			
			if(client != null) {
				if(auditIds == null) {
					auditIds = AuditDaos.getAllAuditsWithClient(client);
				}
				else {
					auditIds.retainAll(AuditDaos.getAllAuditsWithClient(client));
				}
			}
			
			if(deviceId != null) {
				if(auditIds == null) {
					auditIds = AuditDaos.getAllAuditsWithDeviceId(deviceId);
				}
				else {
					auditIds.retainAll(AuditDaos.getAllAuditsWithDeviceId(deviceId));
				}
			}
			
			if(responseType != null) {
				if(auditIds == null) {
					auditIds = AuditDaos.getAllAuditsWithResponse(responseType, errorCode);
				}
				else {
					auditIds.retainAll(AuditDaos.getAllAuditsWithResponse(responseType, errorCode));
				}
			}
			
			if(startDate != null) {
				if(endDate == null) {
					if(auditIds == null) {
						auditIds = AuditDaos.getAllAuditsOnOrAfterDate(startDate);
					}
					else {
						auditIds.retainAll(AuditDaos.getAllAuditsOnOrAfterDate(startDate));
					}
				}
				else {
					if(auditIds == null) {
						auditIds = AuditDaos.getAllAuditsOnOrBetweenDates(startDate, endDate);
					}
					else {
						auditIds.retainAll(AuditDaos.getAllAuditsOnOrBetweenDates(startDate, endDate));
					}
				}
			}
			else if(endDate != null) {
				if(auditIds == null) {
					auditIds = AuditDaos.getAllAuditsOnOrBeforeDate(endDate);
				}
				else {
					auditIds.retainAll(AuditDaos.getAllAuditsOnOrBeforeDate(endDate));
				}
			}
			
			if(auditIds == null) {
				auditIds = AuditDaos.getAllAudits();
			}
			
			return AuditDaos.readAuditInformation(auditIds);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}