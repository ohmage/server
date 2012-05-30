package org.ohmage.request.video;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserVideoServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.VideoValidators;

public class VideoReadRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(VideoReadRequest.class);

	private static final int CHUNK_SIZE = 4096;
	
	private final UUID videoId;
	
	private URL videoUrl;
	
	/**
	 * Creates a video read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public VideoReadRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, TokenLocation.EITHER, false);
		
		UUID tVideoId = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a video read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.VIDEO_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.VIDEO_INVALID_ID,
						"Multiple video IDs were given: " +
							InputKeys.VIDEO_ID);
				}
				else if(t.length == 1) {
					tVideoId = VideoValidators.validateId(t[0]);
				}
				if(tVideoId == null) {
					throw new ValidationException(
						ErrorCode.VIDEO_INVALID_ID,
						"The video's ID was missing: " + InputKeys.VIDEO_ID);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		videoId = tVideoId;
		videoUrl = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Validating a video read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to read the video.");
			UserVideoServices.instance().verifyUserCanReadVideo(
				getUser().getUsername(), 
				videoId);
			
			LOGGER.info("Connecting to the video stream.");
			videoUrl = UserVideoServices.instance().getVideoUrl(videoId);
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

		LOGGER.info("Responding to a video read request.");
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// Open the connection to the image if it is not null.
		InputStream videoStream = null;
		try {
			if((! isFailed()) && videoUrl != null) {
				videoStream = videoUrl.openStream();
			}
		}
		catch(IOException e) {
			LOGGER.error("Could not connect to the image.", e);
			this.setFailed();
		}
		
		try {
			if(isFailed()) {
				super.respond(httpRequest, httpResponse, null);
			}
			else {
				// FIXME: This isn't necessarily the case. We might want to do
				// some sort of image inspection to figure out what this should
				// be.
				httpResponse.setHeader(
					"Content-Disposition", 
					"attachment; filename=" + videoId.toString());
				httpResponse.setHeader(
					"Content-Length", 
					new Long(
						videoUrl
							.openConnection()
							.getContentLength()
						).toString());
				
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
				while((currRead = videoStream.read(bytes)) != -1) {
					dos.write(bytes, 0, currRead);
				}
				
				// Close the image's InputStream.
				videoStream.close();
				
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
			if(videoStream != null) {
				try {
					videoStream.close();
				}
				catch(IOException e) {
					LOGGER.info("Could not close the stream.");
				}
			}
		}
	}
}