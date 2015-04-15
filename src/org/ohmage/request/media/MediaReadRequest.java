package org.ohmage.request.media;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audio;
import org.ohmage.domain.Media;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserMediaServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.AudioValidators;

public class MediaReadRequest extends UserRequest {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(MediaReadRequest.class);

	/**
	 * The size of a chunk when reading the media data.
	 */
	private static final int CHUNK_SIZE = 4096;
	
	/**
	 * The ID of the media file in question from the request.
	 */
	private final UUID mediaId;
	
	/**
	 * The 
	 */
	private Media media = null;

	/**
	 * Creates an media read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MediaReadRequest(
		final HttpServletRequest httpRequest)
		throws InvalidRequestException, IOException {
		
		super(httpRequest, false, TokenLocation.EITHER, null);

		UUID tMediaId = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a media read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.MEDIA_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.MEDIA_INVALID_ID,
						"Multiple media IDs were given: " +
							InputKeys.MEDIA_ID);
				}
				else if(t.length == 1) {
					tMediaId = AudioValidators.validateId(t[0]);  // user AudioValidtors for now
				}
				if(tMediaId == null) {
					throw new ValidationException(
						ErrorCode.MEDIA_INVALID_ID,
						"The media's ID was missing: " + InputKeys.MEDIA_ID);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		mediaId = tMediaId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Validating a media read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER
				.info("Verifying that the user is allowed to read the media.");
			UserMediaServices.instance().verifyUserCanReadMedia(
				getUser().getUsername(), 
				mediaId);
			
			LOGGER.info("Connecting to the media stream.");
			media = UserMediaServices.instance().getAudio(mediaId); // use audio for now
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
		final HttpServletRequest httpRequest,
		final HttpServletResponse httpResponse) {

		LOGGER.info("Responding to a media read request.");
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
				
		// Open the connection to the media if it is not null.
		InputStream mediaStream = null;
		if((! isFailed()) && media != null) {
			mediaStream = media.getContentStream();
		}
		
		try {
			if(isFailed()) {
				super.respond(httpRequest, httpResponse, (JSONObject) null);
			}
			else {
				// set content type
				if (media.getMimeType() != null)
					httpResponse.setContentType(media.getMimeType());
				httpResponse.setHeader(
					"Content-Disposition", 
					"attachment; filename=" + media.getFilename());
				httpResponse.setHeader(
					"Content-Length", 
					new Long(media.getSize()).toString());
				
				// If available, set the token.
				if(getUser() != null) {
					final String token = getUser().getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(
								httpResponse, 
								InputKeys.AUTH_TOKEN, 
								token);
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
				while((currRead = mediaStream.read(bytes)) != -1) {
					dos.write(bytes, 0, currRead);
				}
				
				// Close the image's InputStream.
				mediaStream.close();
				
				// Flush and close the data output stream to which we were 
				// writing.
				dos.flush();
				dos.close();
				
				// Flush and close the output stream that was used to generate
				// the data output stream.
				os.flush();
				os.close();
			}
		}
		catch(IOException e) {
			LOGGER.error(
				"The contents of the file could not be read or written to the response.", 
				e);
			setFailed();
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		finally {
			if(mediaStream != null) {
				try {
					mediaStream.close();
				}
				catch(IOException e) {
					LOGGER.info("Could not close the stream.");
				}
			}
		}
	}
}