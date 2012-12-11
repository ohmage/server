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
package org.ohmage.request.image;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.Image;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ImageServices;
import org.ohmage.service.UserImageServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ImageValidators;

/**
 * <p>Returns an image based on the given ID. The requester must be requesting
 * an image they created, a shared image in a campaign in which they are an
 * author, or a shared image in a shared campaign in which they are an analyst.
 * </p>
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
 *     <td>{@value org.ohmage.request.InputKeys#IMAGE_ID}</td>
 *     <td>The image's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#IMAGE_SIZE}</td>
 *     <td>If omitted, the originally uploaded image will be returned. If 
 *       given, it will alter the image in some way based on the value given. 
 *       It must be one of 
 *       {@link org.ohmage.validator.ImageValidators.ImageSize}.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ImageReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ImageReadRequest.class);
	
	private static final int CHUNK_SIZE = 4096;

	private static final long MILLIS_IN_A_SECOND = 1000;
	
	private final UUID imageId;
	private final Image.Size size;
	
	private Image image;
	
	/**
	 * Creates a new image read request.
	 * 
	 * @param httpRequest The HttpServletRequest with all of the parameters to
	 * 					  build this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ImageReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		UUID tImageId = null;
		Image.Size tSize = Image.Size.ORIGINAL;
		
		if(! isFailed()) {
			LOGGER.info("Creating an image read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.IMAGE_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.IMAGE_INVALID_ID,
							"Multiple image IDs were given: " +
								InputKeys.IMAGE_ID);
				}
				else if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.IMAGE_INVALID_ID,
							"The image ID is missing: " +
								InputKeys.IMAGE_ID);
				}
				else {
					tImageId = ImageValidators.validateId(t[0]);
					
					if(tImageId == null) {
						throw new ValidationException(
								ErrorCode.IMAGE_INVALID_ID,
								"The image ID is missing: " +
									InputKeys.IMAGE_ID);
					}
				}
				
				t = getParameterValues(InputKeys.IMAGE_SIZE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.IMAGE_INVALID_SIZE,
							"Multiple image sizes were given: " +
								InputKeys.IMAGE_SIZE);
				}
				else if(t.length == 1) {
					tSize = ImageValidators.validateImageSize(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		imageId = tImageId;
		size = tSize;
		
		image = null;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing image read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the image exists.");
			ImageServices.instance().verifyImageExistance(imageId, true);
			
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			}
			catch(ServiceException e) {
				LOGGER.info("Verifying that the user can read the image.");
				UserImageServices.instance().verifyUserCanReadImage(getUser().getUsername(), imageId);
			}
			
			LOGGER.info("Retrieving the image.");
			image = ImageServices.instance().getImage(imageId, size);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with an image if it was successful or JSON if it
	 * was not.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the image read response.");
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// Set the CORS headers.
		handleCORS(httpRequest, httpResponse);
		
		// Open the connection to the image if it is not null.
		InputStream imageStream = null;
		try {
			if(image != null) {
				imageStream = image.openStream(size);
			}
		}
		catch(DomainException e) {
			LOGGER.error("Could not connect to the image.", e);
			this.setFailed(ErrorCode.SYSTEM_GENERAL_ERROR, "Image not found.");
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
		// If the request hasn't failed, attempt to write the file to the
		// output stream. 
		try {
			if(isFailed()) {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				super.respond(httpRequest, httpResponse, null);
			}
			else {
				// Set the type of the value.
				// FIXME: This isn't necessarily the case. We might want to do
				// some sort of image inspection to figure out what this should
				// be.
				httpResponse.setContentType("image/png");
				httpResponse.setHeader(
						"Content-Length", 
						new Long(image.getSizeBytes(size)).toString());
				
				// If available, set the token.
				if(getUser() != null) {
					final String token = getUser().getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(
								httpResponse, 
								InputKeys.AUTH_TOKEN, 
								token, 
								(int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
					}
				}

				// Creates the writer that will write the response, success or 
				// fail.
				OutputStream os;
				try {
					os = httpResponse.getOutputStream();
				}
				catch(IOException e) {
					LOGGER.error(
							"Unable to create writer object. Aborting.", 
							e);
					return;
				}
				
				// Set the output stream to the response.
				DataOutputStream dos = new DataOutputStream(os);
				byte[] bytes = new byte[CHUNK_SIZE];
				int currRead;
				try {
					while((currRead = imageStream.read(bytes)) != -1) {
						dos.write(bytes, 0, currRead);
					}
				}
				finally {
					// Close the data output stream to which we were writing.
					try {
						dos.close();
					}
					catch(ClientAbortException e) {
						LOGGER.info("The client hung up unexpectedly.", e);
					}
					catch(IOException e) {
						LOGGER.warn("Error closing the data output stream.", e);
					}
				}
			}
		}
		// If there was an error getting the image's information, abort
		// the whole operation and return an error.
		catch(DomainException e) {
			LOGGER.error(
				"There was a problem reading or writing the image.", 
				e);
			setFailed();
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		// If the client hangs up, just print a warning.
		catch(ClientAbortException e) {
			LOGGER.info("The client hung up unexpectedly.", e);
		}
		// If the error occurred while reading from the input stream or
		// writing to the output stream, abort the whole operation and
		// return an error.
		catch(IOException e) {
			LOGGER.error(
				"The contents of the file could not be read or written to the response.", 
				e);
			setFailed();
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		// No matter what, try to close the input stream if it exists.
		finally {
			try {
				if(imageStream != null) {
					imageStream.close();
				}
			}
			// If the client hangs up, just print a warning.
			catch(ClientAbortException e) {
				LOGGER.info("The client hung up unexpectedly.", e);
			}
			catch(IOException e) {
				LOGGER.warn("Could not close the image stream.", e);
				// We don't care about failing the request, because, either, it
				// has already been failed or we wrote everything already.
			}
		}
	}
}
