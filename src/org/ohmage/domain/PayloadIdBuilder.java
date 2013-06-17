package org.ohmage.domain;

import org.ohmage.exception.ValidationException;

/**
 * <p>
 * The interface for all payload ID builders.
 * </p>
 *
 * @author John Jenkins
 */
public interface PayloadIdBuilder {
	/**
	 * Builds a new PlayloadId object.
	 * 
	 * @param payloadIdParts
	 *        The parts of the payload ID to use to build the {@link PayloadId}
	 *        object.
	 * 
	 * @return The PayloadId object.
	 * 
	 * @throws ValidationException
	 *         The payload ID does not conform to this builder's schema.
	 */
	public PayloadId build(
		final String[] payloadIdParts)
		throws ValidationException;
}