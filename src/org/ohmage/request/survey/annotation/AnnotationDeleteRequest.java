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
package org.ohmage.request.survey.annotation;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserAnnotationServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Allows admins or annotation owners to delete annotations.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token.</td>
 *     <td>true</td>
 *   </tr>   
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ANNOTATION_ID}</td>
 *     <td>An id (a UUID) indicating which annotation to delete.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public class AnnotationDeleteRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AnnotationDeleteRequest.class);
	
	private final UUID annotationId;
	
	/**
	 * Creates a new survey annotation delete request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AnnotationDeleteRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating an annotation delete request.");
		
		UUID tAnnotationId = null;
				
		if(! isFailed()) {
			try {
				Map<String, String[]> parameters = getParameters();
				
				// Validate the annotation ID
				String[] t = parameters.get(InputKeys.ANNOTATION_ID);
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.ANNOTATION_INVALID_ID, "annotation_id is missing or there is more than one.");
					throw new ValidationException("annotation_id is missing or there is more than one.");
				} else {
					// FIXME We need a generic way to validate UUIDs
					tAnnotationId = SurveyResponseValidators.validateSurveyResponseId(t[0]);
					
					if(tAnnotationId == null) {
						setFailed(ErrorCode.ANNOTATION_INVALID_ID, "The annoatation ID is invalid.");
						throw new ValidationException("The annotation ID is invalid.");
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		this.annotationId = tAnnotationId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey annotation delete request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			
			if(! UserServices.instance().isUserAnAdmin(this.getUser().getUsername())) {
				
				// TODO discuss with team: should users be able to update 
				// annotations if they no longer belong to the campaign
				// to which the annotation is attached?
				
				LOGGER.info("Verifying that the requester is attempting to delete an annotation that they created.");
				UserAnnotationServices.instance().verifyUserOwnsAnnotation(this.getUser().getUsername(), annotationId);
			}
			
			LOGGER.info("Deleting the annotation");
			UserAnnotationServices.instance().deleteAnnotation(annotationId);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the this request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the annotation delete request.");
		super.respond(httpRequest, httpResponse, null);
	}
}
