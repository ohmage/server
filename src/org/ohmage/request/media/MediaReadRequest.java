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
import org.ohmage.domain.Image;
import org.ohmage.domain.Media;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ImageServices;
import org.ohmage.service.MediaServices;
import org.ohmage.service.UserMediaServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ImageValidators;
import org.ohmage.validator.MediaValidators;

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
	private Image.Size imageSize = null;
	private Image image = null;
	
	/**
	 * The media object
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
					tMediaId = MediaValidators.validateId(t[0]);  
				}
				if(tMediaId == null) {
					throw new ValidationException(
						ErrorCode.MEDIA_INVALID_ID,
						"The media's ID was missing: " + InputKeys.MEDIA_ID);
				}
				
				// add support for different image_size
				t = getParameterValues(InputKeys.IMAGE_SIZE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.IMAGE_INVALID_SIZE,
							"Multiple image sizes were given: " +
								InputKeys.IMAGE_SIZE);
				}
				else if(t.length == 1) {
					imageSize = ImageValidators.validateImageSize(t[0]);
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
			if (imageSize == null)
				media = MediaServices.instance().getMedia(mediaId);
			else image = ImageServices.instance().getImage(mediaId, imageSize);
			
			if (media == null && image == null)
				throw new ServiceException("Can't locate the media file");
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
			
		try {
			if(isFailed()) {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				super.respond(httpRequest, httpResponse, (JSONObject) null);
			}
			else {
				
				if (imageSize == null) {
					
					mediaStream = media.getContentStream();
					String contentType = media.getContentType();
					
					// set content type
					if (contentType != null)
						httpResponse.setContentType(contentType);
					// only set content-disposition if media is not video/image/audio
					if (contentType.startsWith("application") || contentType.startsWith("text"))
						httpResponse.setHeader("Content-Disposition", 
								"attachment; filename=\"" + media.getFileName() + "\"");
					
					httpResponse.setHeader("Content-Length", 
						new Long(media.getFileSize()).toString());

				} else { // it is an image/read request
					mediaStream =  image.getInputStream(imageSize);
					httpResponse.setContentType(image.getContentType(imageSize));
					httpResponse.setHeader("Content-Length", 
							new Long(image.getSizeBytes(imageSize)).toString());
					
				}
				
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
		catch(DomainException e) {
			LOGGER.error("Could not connect to the media file.", e);
			this.setFailed(ErrorCode.SYSTEM_GENERAL_ERROR, "File not found.");
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			super.respond(httpRequest, httpResponse, (JSONObject) null);
			return;
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
