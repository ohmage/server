package org.ohmage.request.document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.DocumentServices;
import org.ohmage.service.UserDocumentServices;
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
	 */
	public DocumentDeletionRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		String tempDocumentId = null;
		
		try {
			tempDocumentId = DocumentValidators.validateDocumentId(this, httpRequest.getParameter(InputKeys.DOCUMENT_ID));
			if(tempDocumentId == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "The document ID is missing.");
				throw new ValidationException("The document ID is missing.");
			}
			else if(httpRequest.getParameterValues(InputKeys.DOCUMENT_ID).length > 1) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "Multiple document IDs were given.");
				throw new ValidationException("Multiple document IDs were given.");
			}
		}
		catch(ValidationException e) {
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
			DocumentServices.ensureDocumentExistence(this, documentId);
			
			LOGGER.info("Verifying that the requesting user can delete this document.");
			UserDocumentServices.userCanDeleteDocument(this, getUser().getUsername(), documentId);
			
			LOGGER.info("Deleting the document.");
			DocumentServices.deleteDocument(this, documentId);
		}
		catch(ServiceException e) {
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