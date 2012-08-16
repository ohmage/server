package org.ohmage.domain;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
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
	@Override
	public String getId() {
		return observerId;
	}

	/**
	 * Returns the stream ID.
	 * 
	 * @return The stream ID.
	 */
	@Override
	public String getSubId() {
		return streamId;
	}
}