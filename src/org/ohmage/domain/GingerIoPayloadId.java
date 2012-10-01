package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadGingerIoRequest;

/**
 * This class represents a payload ID for GingerIO.
 *
 * @author John Jenkins
 */
public class GingerIoPayloadId implements PayloadId {
	/**
	 * Default constructor that contains no additional parameters.
	 */
	public GingerIoPayloadId() {
		// Do nothing.
	}

	/**
	 * Creates a new OMH read call for GingerIO.
	 * 
	 * @return An OmhReadGingerIoRequest object.
	 */
	@Override
	public OmhReadGingerIoRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final long version,
			final String owner,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {

		try {
			return
				new OmhReadGingerIoRequest(
					httpRequest, 
					parameters, 
					hashPassword,
					tokenLocation, 
					callClientRequester,
					owner,
					startDate,
					endDate,
					numToSkip,
					numToReturn);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the parameters.", 
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"There was a problem building the request.",
				e);
		}
	}

}
