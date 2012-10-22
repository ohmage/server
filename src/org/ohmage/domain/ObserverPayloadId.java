package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.util.StringUtils;

/**
 * The observer's payload ID.
 *
 * @author John Jenkins
 */
public class ObserverPayloadId implements PayloadId {
	private final String observerId;
	private final String streamId;
	
	/**
	 * Creates a new Observer payload ID.
	 * 
	 * @param observerId The observer's ID.
	 * 
	 * @param streamId The stream's ID.
	 * 
	 * @throws DomainException The observer or stream ID is null or only 
	 * 						   whitespace.
	 */
	public ObserverPayloadId(
			final String observerId, 
			final String streamId)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(observerId)) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_ID,
				"The observer ID is null or only whitespace.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(streamId)) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_STREAM_ID,
				"The stream ID is null or only whitespace.");
		}
		
		this.observerId = observerId;
		this.streamId = streamId;
	}

	/**
	 * Returns the observer ID.
	 * 
	 * @return The observer ID.
	 */
	public String getObserverId() {
		return observerId;
	}

	/**
	 * Returns the stream ID.
	 * 
	 * @return The stream ID.
	 */
	public String getStreamId() {
		return streamId;
	}

	/**
	 * Creates a stream/read request.
	 * 
	 * @return A stream read request.
	 */
	@Override
	public StreamReadRequest generateSubRequest(
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
				new StreamReadRequest(
					httpRequest,
					parameters,
					true,
					TokenLocation.EITHER,
					callClientRequester,
					owner,
					observerId,
					null,
					streamId,
					version,
					startDate,
					endDate,
					null,
					false,
					numToSkip,
					numToReturn);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the HTTP request.",
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"Error parsing the parameters.",
				e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
				"One of the parameters was invalid.",
				e);
		}
	}
}