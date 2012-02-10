/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request.audit;

import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audit;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.servlet.RequestServlet;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.AuditServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.AuditValidators;
import org.ohmage.validator.AuditValidators.ResponseType;

/**
 * <p>Reads the audits from the system, based on the given parameters. If no
 * parameters are given, all audits will be returned by the system, so be 
 * careful! In order to read any audits the user must be an admin.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_REQUEST_TYPE}</td>
 *     <td>Limits the audits to only those with a specific request type. This 
 *       must be one of 
 *       {@link org.ohmage.jee.servlet.RequestServlet.RequestType}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_URI}</td>
 *     <td>Limits the audits to only those with the given URI.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_CLIENT}</td>
 *     <td>Limits the audits to only those with the given client value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_DEVICE_ID}</td>
 *     <td>Limits the audits to only those with the given device ID value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_RESPONSE_TYPE}</td>
 *     <td>Limits the audits to only those with the given response type. This
 *       must be one of
 *       {@link org.ohmage.validator.AuditValidators.ResponseType}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUDIT_ERROR_CODE}</td>
 *     <td>Limits the audits to only those with the given error code. This is
 *       ignored unless the 
 *       {@link org.ohmage.request.InputKeys#AUDIT_RESPONSE_TYPE} is 
 *       {@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE}.
 *       </td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>Limits the audits to only those that were recorded on or after this
 *       date. This may be either a date or a date-time.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>Limits the audits to only those that were recorded on or before this
 *       date. This may be either a date or a date-time.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class AuditReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuditReadRequest.class);
	
	private static final String RESULT_KEY = "audits";
	
	private final RequestType requestType;
	private final URI uri;
	private final String client;
	private final String deviceId;
	private final ResponseType responseType;
	private final ErrorCode errorCode;
	
	private final Date startDate;
	private final Date endDate;
	
	private List<Audit> results;
	
	/**
	 * Creates an audit read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 */
	public AuditReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating an audit read request.");
		
		RequestServlet.RequestType tRequestType = null;
		URI tUri = null;
		String tClient = null;
		String tDeviceId = null;
		ResponseType tResponseType = null;
		ErrorCode tErrorCode = null;
		Date tStartDate = null;
		Date tEndDate = null;
		
		if(! isFailed()) {
			try {
				tRequestType = AuditValidators.validateRequestType(httpRequest.getParameter(InputKeys.AUDIT_REQUEST_TYPE));
				if((tRequestType != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_REQUEST_TYPE).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_REQUEST_TYPE, "Multiple " + InputKeys.AUDIT_REQUEST_TYPE + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_REQUEST_TYPE + " parameters were given.");
				}
				
				tUri = AuditValidators.validateUri(httpRequest.getParameter(InputKeys.AUDIT_URI));
				if((tUri != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_URI).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_URI, "Multiple " + InputKeys.AUDIT_URI + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_URI + " parameters were given.");
				}
				
				tClient = AuditValidators.validateClient(httpRequest.getParameter(InputKeys.AUDIT_CLIENT));
				if((tClient != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_CLIENT).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_CLIENT, "Multiple " + InputKeys.AUDIT_CLIENT + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_CLIENT + " parameters were given.");
				}
				
				tDeviceId = AuditValidators.validateDeviceId(httpRequest.getParameter(InputKeys.AUDIT_DEVICE_ID));
				if((tDeviceId != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_DEVICE_ID).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_DEVICE_ID, "Multiple " + InputKeys.AUDIT_DEVICE_ID + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_DEVICE_ID + " parameters were given.");
				}
				
				tResponseType = AuditValidators.validateResponseType(httpRequest.getParameter(InputKeys.AUDIT_RESPONSE_TYPE));
				if((tResponseType != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_RESPONSE_TYPE).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_RESPONSE_TYPE, "Multiple " + InputKeys.AUDIT_RESPONSE_TYPE + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_RESPONSE_TYPE + " parameters were given.");
				}
				
				tErrorCode = AuditValidators.validateErrorCode(httpRequest.getParameter(InputKeys.AUDIT_ERROR_CODE));
				if((tErrorCode != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_ERROR_CODE).length > 1)) {
					setFailed(ErrorCode.AUDIT_INVALID_ERROR_CODE, "Multiple " + InputKeys.AUDIT_ERROR_CODE + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_ERROR_CODE + " parameters were given.");
				}
				
				tStartDate = AuditValidators.validateStartDate(httpRequest.getParameter(InputKeys.AUDIT_START_DATE));
				if((tStartDate != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_START_DATE).length > 1)) {
					setFailed(ErrorCode.SERVER_INVALID_DATE, "Multiple " + InputKeys.AUDIT_START_DATE + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_START_DATE + " parameters were given.");
				}
				
				tEndDate = AuditValidators.validateEndDate(httpRequest.getParameter(InputKeys.AUDIT_END_DATE));
				if((tEndDate != null) && (httpRequest.getParameterValues(InputKeys.AUDIT_END_DATE).length > 1)) {
					setFailed(ErrorCode.SERVER_INVALID_DATE, "Multiple " + InputKeys.AUDIT_END_DATE + " parameters were given.");
					throw new ValidationException("Multiple " + InputKeys.AUDIT_END_DATE + " parameters were given.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		requestType = tRequestType;
		uri = tUri;
		client = tClient;
		deviceId = tDeviceId;
		responseType = tResponseType;
		errorCode = tErrorCode;
		startDate = tStartDate;
		endDate = tEndDate;
		
		results = new LinkedList<Audit>();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the audit read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying the user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Gathering the audit information.");
			results = AuditServices.instance().getAuditInformation(requestType, uri, client, deviceId, responseType, errorCode, startDate, endDate);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Replies to the request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		// Build the result object.
		JSONArray resultJson = new JSONArray();
		for(Audit result : results) {
			resultJson.put(result.toJson());
		}
		
		super.respond(httpRequest, httpResponse, RESULT_KEY, resultJson);
	}
}
