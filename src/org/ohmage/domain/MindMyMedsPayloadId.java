package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadMindMyMedsRequest;

/**
 * This class represents a payload ID for Run Keeper.
 *
 * @author John Jenkins
 */
public class MindMyMedsPayloadId implements PayloadId {
	/**
	 * Default constructor that contains no additional parameters.
	 */
	public MindMyMedsPayloadId() {
		// Do nothing.
	}

	/**
	 * Creates a new OMH read call for Mind My Meds.
	 * 
	 * @return An OmhReadMindMyMedsRequest request object.
	 */
	@Override
	public OmhReadMindMyMedsRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final long version,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		try {
			return new OmhReadMindMyMedsRequest(
				httpRequest, 
				parameters, 
				hashPassword,
				tokenLocation, 
				callClientRequester,
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