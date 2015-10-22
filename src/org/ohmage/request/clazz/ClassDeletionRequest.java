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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.ClassValidators;

/**
 * <p>A request to delete a class. The requester must be an admin.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN}</td>
 *     <td>The unique identifier for the class to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 * @author Hongsuda T. 
 */
public class ClassDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassDeletionRequest.class);
	
	private final String classId;
	
	// a flag for the server to check whether the class deletion will lead 
	// to orphan campaigns (i.e. campaign associated with no class)
	private final Boolean checkOrphanCampaigns; 
	
	/**
	 * Builds this request based on the information in the HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required
	 * 					  parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ClassDeletionRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {	
		super(httpRequest, null, TokenLocation.PARAMETER, null);

		String tempClassId = null;
		Boolean tCheckOrphanCampaigns = null;
		
		if(! isFailed()) {
			try {
				LOGGER.info("Creating a class deletion request.");
				String t[];

				tempClassId = ClassValidators.validateClassId(httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Missing the required class ID: " + InputKeys.CLASS_URN);
					throw new ValidationException("Missing the required class ID: " + InputKeys.CLASS_URN);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN).length > 1) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID parameters were found.");
					throw new ValidationException("Multiple class ID parameters were found.");
				}
				
				// check_orphan_campaigns (optional)
				t = getParameterValues(InputKeys.CLASS_NO_ORPHAN_CAMPAIGNS);
				if (t.length > 1) {
					setFailed(ErrorCode.CLASS_INVALID_FLAGS, "Multiple no_orphan_campaigns flags were found");
					throw new ValidationException("Multiple no_orphan_campaigns flags were found.");
				} else if (t.length == 1) {
					tCheckOrphanCampaigns = StringUtils.decodeBoolean(t[0]);
					if (tCheckOrphanCampaigns == null) {
						setFailed(ErrorCode.CLASS_INVALID_FLAGS, 
								"Invalid " + InputKeys.CLASS_NO_ORPHAN_CAMPAIGNS + " boolean value.");
						throw new ValidationException("Invalid " + InputKeys.CLASS_NO_ORPHAN_CAMPAIGNS + " boolean value.");						
					}
				} else {
					// default value
					tCheckOrphanCampaigns = false;
				}
		
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		} 
		
		classId = tempClassId;
		checkOrphanCampaigns = tCheckOrphanCampaigns;		
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class deletion request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER
				.info(
					"Checking that the user is allowed to delete the class.");
			UserServices
				.instance()
				.verifyUserCanDeleteClasses(getUser().getUsername(), classId);
			
			LOGGER.info("Checking that the class exists.");
			ClassServices.instance().checkClassExistence(classId, true);
			
			if (checkOrphanCampaigns) {
				LOGGER.info(("Check if orphan campaigns exist if class is deleted"));
				ClassServices.instance().checkDeleteClassCauseOrphanCampaigns(classId);
			}
			
			LOGGER.info("Deleting the class.");
			ClassServices.instance().deleteClass(classId);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
			LOGGER.debug("failure message: " +  getFailureMessage());
		}
	}

	/**
	 * Responds to the deletion request. Returns success if it successfully 
	 * deleted the class or an error code and explanation if anything went
	 * wrong.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}
