package org.ohmage.request.audio;

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
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MediaServices;
import org.ohmage.service.UserMediaServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.AudioValidators;

// HT: Deprecated
public class AudioReadRequest extends UserRequest {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(AudioReadRequest.class);

	/**
	 * The size of a chunk when reading the audio data.
	 */
	private static final int CHUNK_SIZE = 4096;
	
	/**
	 * The ID of the audio file in question from the request.
	 */
	private final UUID audioId;
	
	/**
	 * The 
	 */
	private Audio audio = null;

	/**
	 * Creates an audio read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AudioReadRequest(
		final HttpServletRequest httpRequest)
		throws InvalidRequestException, IOException {
		
		super(httpRequest, false, TokenLocation.EITHER, null);

		UUID tAudioId = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating an audio read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.AUDIO_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.AUDIO_INVALID_ID,
						"Multiple audio IDs were given: " +
							InputKeys.AUDIO_ID);
				}
				else if(t.length == 1) {
					tAudioId = AudioValidators.validateId(t[0]);
				}
				if(tAudioId == null) {
					throw new ValidationException(
						ErrorCode.AUDIO_INVALID_ID,
						"The audio's ID was missing: " + InputKeys.AUDIO_ID);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		audioId = tAudioId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Validating an audio read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER
				.info("Verifying that the user is allowed to read the audio.");
			UserMediaServices.instance().verifyUserCanReadMedia(
				getUser().getUsername(), 
				audioId);
			
			LOGGER.info("Connecting to the audio stream.");
			audio = MediaServices.instance().getAudio(audioId);
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
		if((! isFailed()) && audio != null) {
			videoStream = audio.getContentStream();
		}
		
		try {
			if(isFailed()) {
				super.respond(httpRequest, httpResponse, (JSONObject) null);
			}
			else {
				httpResponse.setHeader(
					"Content-Disposition", 
					"attachment; filename=" + audio.getFileName());
				httpResponse.setHeader(
					"Content-Length", 
					new Long(audio.getSize()).toString());
				
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