package org.ohmage.request.image;

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
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.UserBin;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ImageServices;
import org.ohmage.service.UserImageServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ImageValidators;
import org.ohmage.validator.ImageValidators.ImageSize;

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
	
	private final String imageId;
	private final ImageSize size;
	
	private InputStream imageStream;
	
	/**
	 * Creates a new image read request.
	 * 
	 * @param httpRequest The HttpServletRequest with all of the parameters to
	 * 					  build this request.
	 */
	public ImageReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating an image read request.");
		
		String tImageId = null;
		ImageSize tSize = null;
		
		if(! isFailed()) {
			try {
				tImageId = ImageValidators.validateId(this, httpRequest.getParameter(InputKeys.IMAGE_ID));
				if(tImageId == null) {
					setFailed(ErrorCodes.IMAGE_INVALID_ID, "The image ID is missing: " + InputKeys.IMAGE_ID);
					throw new ValidationException("The image ID is missing: " + InputKeys.IMAGE_ID);
				}
				else if(httpRequest.getParameterValues(InputKeys.IMAGE_ID).length > 1) {
					setFailed(ErrorCodes.IMAGE_INVALID_ID, "Multiple owner values were given: " + InputKeys.IMAGE_ID);
					throw new ValidationException("Multiple owner values were given: " + InputKeys.IMAGE_ID);
				}
				
				tSize = ImageValidators.validateImageSize(this, httpRequest.getParameter(InputKeys.IMAGE_SIZE));
				if((tSize != null) && (httpRequest.getParameterValues(InputKeys.IMAGE_SIZE).length > 1)) {
					setFailed(ErrorCodes.IMAGE_INVALID_SIZE, "Multiple image sizes were given: " + InputKeys.IMAGE_SIZE);
					throw new ValidationException("Multiple image sizes were given: " + InputKeys.IMAGE_SIZE);
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		imageId = tImageId;
		size = tSize;
		
		imageStream = null;
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
			ImageServices.verifyImageExistance(this, imageId, true);
			
			LOGGER.info("Verifying that the user can read the image.");
			UserImageServices.verifyUserCanReadImage(this, getUser().getUsername(), imageId);
			
			LOGGER.info("Retreiving the image.");
			imageStream = ImageServices.getImage(this, imageId, size);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with an image if it was successful or JSON if it
	 * was not.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing image read response.");
		
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
				// Set the type as a JPEG.
				// FIXME: This isn't necessarily the case. We might want to do
				// some sort of image inspection to figure out what this should
				// be.
				httpResponse.setContentType("image/png");
				httpResponse.setHeader("Content-Disposition", "filename=image");

				// If available, set the token.
				if(getUser() != null) {
					final String token = getUser().getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
					}
				}
				
				// Set the output stream to the response.
				DataOutputStream dos = new DataOutputStream(os);
				
				// Read the file in chunks and write it to the output stream.
				byte[] bytes = new byte[CHUNK_SIZE];
				int read = 0;
				int currRead = imageStream.read(bytes);
				while(currRead != -1) {
					dos.write(bytes, 0, currRead);
					read += currRead;
					
					currRead = imageStream.read(bytes);
				}
				
				// Close the image's InputStream.
				imageStream.close();
				
				// Flush and close the data output stream to which we were 
				// writing.
				dos.flush();
				dos.close();
				
				// Flush and close the output stream that was used to generate
				// the data output stream.
				os.flush();
				os.close();
			}
			// If the error occurred while reading from the input stream or
			// writing to the output stream, abort the whole operation and
			// return an error.
			catch(IOException e) {
				LOGGER.error("The contents of the file could not be read or written to the response.", e);
				setFailed();
			}
		}
		
		// If the request ever failed, write an error message.
		// FIXME: This should probably check if it's a GET and send a 404.
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			
			// Write the error response.
			try {
				writer.write(getFailureMessage()); 
			}
			catch(IOException e) {
				LOGGER.error("Unable to write failed response message. Aborting.", e);
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