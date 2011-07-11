package org.ohmage.request.document;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.UserBin;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.DocumentServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserDocumentServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.DocumentValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>Creates a new class. The requester must be an admin.</p>
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
public class DocumentReadContentsRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentReadContentsRequest.class);
	
	private static final int CHUNK_SIZE = 4096;
	
	private final String documentId;
	
	private String documentName;
	private InputStream contentsStream;
	
	/**
	 * Creates a new request for reading a document's contents.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters necessary
	 * 					  to build this request.
	 */
	public DocumentReadContentsRequest(HttpServletRequest httpRequest) {
		super(getToken(httpRequest), httpRequest.getParameter(InputKeys.CLIENT));
		
		String tempDocumentId = null;
		
		try {
			tempDocumentId = DocumentValidators.validateDocumentId(this, httpRequest.getParameter(InputKeys.DOCUMENT_ID));
			if(tempDocumentId == null) {
				setFailed(ErrorCodes.DOCUMENT_MISSING_ID, "The document ID is missing.");
				throw new ValidationException("The document ID is missing.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		documentId = tempDocumentId;
		
		contentsStream = null;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the document read contents request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the document exists.");
			DocumentServices.ensureDocumentExistence(this, documentId);
			
			LOGGER.info("Verifying that the requesting user can read the contents of this document.");
			UserDocumentServices.userCanReadDocument(this, user.getUsername(), documentId);
			
			LOGGER.info("Retrieving the document's name.");
			documentName = DocumentServices.getDocumentName(this, documentId);
			
			LOGGER.info("Retrieving the document's contents.");
			contentsStream = DocumentServices.getDocumentInputStream(this, documentId);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * If the request has succeeded, it attempts to create an OutputStream to
	 * the response and pipe the contents of the document from the InputStream
	 * to the OutputStream. If the request fails at any point, it will attempt
	 * to return a JSON error message. If writing the response fails, an error
	 * message is printed.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing read document contents response.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		OutputStream os;
		try {
			os = getOutputStream(httpRequest, httpResponse);
			writer = new BufferedWriter(new OutputStreamWriter(os));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// If the request hasn't failed, attempt to write the file to the
		// output stream. 
		if(! isFailed()) {
			try {
				// Set the type and force the browser to download it as the 
				// last step before beginning to stream the response.
				httpResponse.setContentType("ohmage/document");
				httpResponse.setHeader("Content-Disposition", "attachment; filename=" + documentName);
				
				// If available, set the token.
				if(user != null) {
					final String token = user.getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
					}
				}
				
				// Set the output stream to the response.
				DataOutputStream dos = new DataOutputStream(os);
				
				// Read the file in chuncks and write it to the output stream.
				byte[] bytes = new byte[CHUNK_SIZE];
				int read = 0;
				int currRead = contentsStream.read(bytes);
				while(currRead != -1) {
					dos.write(bytes, 0, currRead);
					read += currRead;
					
					currRead = contentsStream.read(bytes);
				}
				
				// Close the document's InputStream.
				contentsStream.close();
				
				// Flush and close the data output stream to which we were 
				// writing.
				dos.flush();
				dos.close();
				
				// Flush and close the output stream that was used to generate
				// the data output stream.
				os.flush();
				os.close();
			}
			// If the error occured while reading from the input stream or
			// writing to the output stream, abort the whole operation and
			// return an error.
			catch(IOException e) {
				LOGGER.error("The contents of the file could not be read or written to the response.", e);
				setFailed();
			}
		}
		
		// If the request ever failed, write an error message.
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			String responseText;
			
			try {
				// Use the annotator's message to build the response.
				responseText = annotator.toJsonObject().toString();
			}
			catch(JSONException e) {
				// If we can't even build the failure message, write a hand-
				// written message as the response.
				LOGGER.error("An error occurred while building the failure JSON response.", e);
				responseText = RESPONSE_ERROR_JSON_TEXT;
			}
			
			// Write the error response.
			try {
				writer.write(responseText); 
			}
			catch(IOException e) {
				LOGGER.error("Unable to write failed response message. Aborting.", e);
				return;
			}
			
			// Flush it and close.
			try {
				writer.flush();
				writer.close();
			}
			catch(IOException e) {
				LOGGER.error("Unable to flush or close the writer.", e);
			}
		}
	}
}
