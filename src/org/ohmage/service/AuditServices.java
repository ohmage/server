package org.ohmage.service;

import java.util.Map;

import org.ohmage.dao.AuditDaos;
import org.ohmage.dao.DataAccessException;
import org.ohmage.jee.servlet.RequestServlet;

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
}
