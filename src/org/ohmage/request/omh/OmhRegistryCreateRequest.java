package org.ohmage.request.omh;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.Request;

/**
 * <p>Creates a new entry in the OMH registry. Currently, this is not 
 * implemented so a HTTP 405 status code will always be returned.</p>
 * 
 * @author John Jenkins
 */
public class OmhRegistryCreateRequest extends Request {
	private static final Logger LOGGER =
		Logger.getLogger(OmhRegistryCreateRequest.class);
	
	/**
	 * Creates an OMH registry creation request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public OmhRegistryCreateRequest(
			final HttpServletRequest httpRequest) 
			throws InvalidRequestException, IOException {
		
		super(httpRequest, null);
		
		LOGGER.info("Creating an OMH registry create request.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		// We are always returning a "not supported" HTTP status code, so we do
		// nothing here.
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

}
