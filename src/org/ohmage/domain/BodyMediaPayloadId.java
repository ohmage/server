package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadBodyMediaRequest;

/**
 * This class represents a payload ID for Body Media.
 *
 * @author John Jenkins
 */
public class BodyMediaPayloadId implements PayloadId {
	private final String id;
	
	/**
	 * Creates a payload ID that contains no ID or sub-ID.
	 * 
	 * @param path The path to the BodyMedia API.
	 * 
	 * @throws DomainException The path was null.
	 */
	public BodyMediaPayloadId(final String path) throws DomainException {
		if(path == null) {
			throw new DomainException("The path was null.");
		}
		
		id = path;
	}

	/**
	 * @return This is the path for the BodyMedia API.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @return There is no sub-ID for the BodyMedia payload ID.
	 */
	@Override
	public String getSubId() {
		return null;
	}

	/**
	 * Creates a BodyMedia request.
	 * 
	 * @return An OmhReadBodyMediaRequest object.
	 */
	@Override
	public OmhReadBodyMediaRequest generateSubRequest(
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
		
		if(	(startDate != null) && (endDate != null) &&
			endDate.isAfter(startDate.plusYears(1).minusDays(1))) {
			
			throw
				new DomainException(
					ErrorCode.OMH_INVALID_END_TIMESTAMP,
					"The range for BodyMedia requests cannot be greater than a year.");
		}

		try {
			return
				new OmhReadBodyMediaRequest(
					httpRequest, 
					parameters, 
					hashPassword,
					tokenLocation,
					callClientRequester,
					startDate, 
					endDate,
					numToSkip, 
					numToReturn,
					id);
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
