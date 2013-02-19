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
package org.ohmage.request.clazz;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Generates a class roster for an existing class. The user must be an admin
 * or they must be privileged in each of the classes that are requested to be
 * part of the roster.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes identifiers (URNs) separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassRosterReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassRosterReadRequest.class);
	
	private final Collection<String> classIds;
	
	private Map<String, Map<String, Clazz.Role>> roster;
	
	/**
	 * Creates a new class roster read request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters
	 * 					  from the requester.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ClassRosterReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.EITHER, null);
		
		LOGGER.info("Creating a class roster read request.");
		
		Set<String> tClassIds = null;
		
		try {
			tClassIds = ClassValidators.validateClassIdList(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if(tClassIds == null) {
				setFailed(ErrorCode.CLASS_INVALID_ID, "Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
				throw new ValidationException("Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
			}
			else if(httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1) {
				setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists were found.");
				throw new ValidationException("Multiple class ID lists were found.");
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			LOGGER.info(e.toString());
		}
		
		classIds = tClassIds;
		roster = null;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the class roster read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the classes in the class list exist.");
			ClassServices.instance().checkClassesExistence(classIds, true);
			
			LOGGER.info("Verify that the user is an admin or that they are privileged in each of the classes in a list.");
			UserClassServices.instance().userIsAdminOrPrivilegedInAllClasses(getUser().getUsername(), classIds);
			
			LOGGER.info("Generating the class roster.");
			roster = ClassServices.instance().generateClassRoster(getUser().getUsername(), classIds);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Generates the class roster and returns it to the user as an attachment
	 * unless it fails in which case a JSON message will be returned not as an
	 * attachment.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the class roster read response.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// Write an error message or the roster depending on if it failed or 
		// not.
		String responseText = "";
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			
			// Use the annotator's message to build the response.
			responseText = getFailureMessage();
		}
		else {
			// Set the type and force the browser to download it as the 
			// last step before beginning to stream the response.
			httpResponse.setContentType("ohmage/roster");
			httpResponse.setHeader("Content-Disposition", "attachment; filename=roster.csv");
			
			// If available, set the token.
			if(getUser() != null) {
				final String token = getUser().getToken(); 
				if(token != null) {
					CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token);
				}
			}

			// Build the class roster as a CSV file.
			StringBuilder resultBuilder = new StringBuilder();
			for(String classId : roster.keySet()) {
				Map<String, Clazz.Role> userAndClassRoles =
						roster.get(classId);
				
				for(String username : userAndClassRoles.keySet()) {
					Clazz.Role role = userAndClassRoles.get(username);
					
					resultBuilder
						.append(classId)
						.append(",")
						.append(username)
						.append(",")
						.append((role == null) ? "" : role.toString())
						.append("\n");
				}
			}
			
			responseText = resultBuilder.toString();
		}
			
		// Write the error response.
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			LOGGER.warn("Unable to write failed response message. Aborting.", e);
		}
		
		// Close it.
		try {
			writer.close();
		}
		catch(IOException e) {
			LOGGER.warn("Unable to close the writer.", e);
		}
	}
	
	/**
	 * Returns the list of classes from the parameters.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		
		if(classIds != null) {
			result.put(InputKeys.CLASS_URN, classIds.toArray(new String[0]));
		}
		
		return result;
	}
}
