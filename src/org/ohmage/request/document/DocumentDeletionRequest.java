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
package org.ohmage.request.document;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.DocumentServices;
import org.ohmage.service.UserDocumentServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.DocumentValidators;

/**
 * <p>Creates a document deletion request. To delete a document the requester
 * must be an owner of the document through a personal association with the
 * document or through a campaign or class association.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_ID}</td>
 *     <td>The unique identifier for the document whose contents is 
 *       desired.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class DocumentDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentDeletionRequest.class);
	
	private final String documentId;
	
	/**
	 * Creates a new request for deleting a document's contents.
	 * 
	 * @param httpRequest The HttpServletRequest with the request's parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public DocumentDeletionRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		String tempDocumentId = null;
		
		try {
			tempDocumentId = DocumentValidators.validateDocumentId(httpRequest.getParameter(InputKeys.DOCUMENT_ID));
			if(tempDocumentId == null) {
				setFailed(ErrorCode.DOCUMENT_INVALID_ID, "The document ID is missing.");
				throw new ValidationException("The document ID is missing.");
			}
			else if(httpRequest.getParameterValues(InputKeys.DOCUMENT_ID).length > 1) {
				setFailed(ErrorCode.DOCUMENT_INVALID_ID, "Multiple document IDs were given.");
				throw new ValidationException("Multiple document IDs were given.");
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			LOGGER.info(e.toString());
		}
		
		documentId = tempDocumentId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the document read contents request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the document exists.");
			DocumentServices.instance().ensureDocumentExistence(documentId);
			
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			}
			catch(ServiceException e) {
				LOGGER.info("The user is not an admin.");
				LOGGER.info("Verifying that the requesting user can delete this document.");
				UserDocumentServices.instance().userCanDeleteDocument(getUser().getUsername(), documentId);
			}
			
			LOGGER.info("Deleting the document.");
			DocumentServices.instance().deleteDocument(documentId);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Returns a success JSON message if it was successfully deleted or a 
	 * failure JSON message with an explanation if available.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}
