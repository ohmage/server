/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DocumentReadContentsAwRequest;


/**
 * Writes the document to the response or writes an error message if there is a
 * failure.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(DocumentReadContentsResponseWriter.class);
	
	private static final int CHUNK_SIZE = 1024;
	
	/**
	 * Builds a response writer for this request with a default error response
	 * should none other be present.
	 */
	public DocumentReadContentsResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the file to the response output stream. If there is a failure at
	 * any point before writing to the output stream it will return some error
	 * message; if there is an error while writing to the output stream, an
	 * exception is thrown.
	 * 
	 * @throws IllegalStateException Thrown if there is an error while writing
	 * 								 to the output stream.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) throws IllegalStateException {
		_logger.info("Writing read document contents response.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		OutputStream os;
		try {
			os = getOutputStream(request, response);
			writer = new BufferedWriter(new OutputStreamWriter(os));
		}
		catch(IOException e) {
			_logger.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(response);
		
		// If the request hasn't failed, attempt to write the file to the
		// output stream. 
		if(! awRequest.isFailedRequest()) {
			try {
				response.setContentType("ohmage/document");
				response.setHeader("Content-Disposition", "attachment; filename=" + ((String) awRequest.getToReturnValue(DocumentReadContentsAwRequest.KEY_DOCUMENT_FILENAME)));
				
				// Get an input stream for the file.
				File documentFile = new File(new URI((String) awRequest.getToReturnValue(org.ohmage.request.DocumentReadContentsAwRequest.KEY_DOCUMENT_FILE)));
				DataInputStream is = new DataInputStream(new FileInputStream(documentFile));
				
				// Set the output stream to the response.
				DataOutputStream dos = new DataOutputStream(os);
				
				// Read the file in chuncks and write it to the output stream.
				byte[] bytes = new byte[CHUNK_SIZE];
				int read = 0;
				int currRead = is.read(bytes);
				while(currRead != -1) {
					dos.write(bytes, 0, currRead);
					read += currRead;
					
					currRead = is.read(bytes);
				}
				
				is.close();
				dos.flush();
				dos.close();
			}
			// If the URI doesn't adhere to the correct syntax as a URI just 
			// set the request as failed.
			catch(URISyntaxException e) {
				_logger.error("Error parsing the syntax of the URL-based resource descriptor.", e);
				awRequest.setFailedRequest(true);
			}
			// If the file doesn't exist, then set the request as failed.
			catch(FileNotFoundException e) {
				_logger.error("The file to be returned was not found on the disk.", e);
				awRequest.setFailedRequest(true);
			}
			// If the error occured while reading from the input stream or
			// writing to the output stream, abort the whole operation and
			// return an error.
			catch(IOException e) {
				_logger.error("The contents of the file could not be read or written to the response.", e);
				throw new IllegalStateException("Aborting the response and erroring out.", e);
			}
		}
		
		// If the request ever failed, write an error message.
		if(awRequest.isFailedRequest()) {
			response.setContentType("text/html");
			String responseText;
			
			// If a specific error message was annotated, use that. 
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			// Otherwise, just use the default one with which we were built.
			else {
				responseText = generalJsonErrorMessage();
			}
			
			// Write the error response.
			try {
				writer.write(responseText); 
			}
			catch(IOException e) {
				_logger.error("Unable to write failed response message. Aborting.", e);
				return;
			}
			
			// Flush it and close.
			try {
				writer.flush();
				writer.close();
			}
			catch(IOException e) {
				_logger.error("Unable to flush or close the writer.", e);
			}
		}
	}
}
