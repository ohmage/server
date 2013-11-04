package org.ohmage.bin;

import java.util.List;

import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.StreamData;

/**
 * <p>
 * The interface to the database-backed stream data repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class StreamDataBin {
	/**
	 * The singular instance of this class.
	 */
	private static StreamDataBin instance;

	/**
	 * Initializes the singleton instance to this.
	 */
	protected StreamDataBin() {
		instance = this;
	}

	/**
	 * Retrieves the singleton instance of this class.
	 * 
	 * @return The singleton instance of this class.
	 */
	public static final StreamDataBin getInstance() {
		return instance;
	}

	/**
	 * Adds new stream data to the repository.
	 * 
	 * @param streamData
	 *        The stream data to add.
	 * 
	 * @throws IllegalArgumentException
	 *         The stream data is null.
	 */
	public abstract void addStreamData(
		final List<StreamData> streamData)
		throws IllegalArgumentException, InvalidArgumentException;
	
	/**
	 * Retrieves data specific to a user and a stream.
	 * 
	 * @param username
	 *        The user's username.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamVersion
	 *        The version of the stream.
	 * 
	 * @return The data that matches the parameters.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter was null.
	 */
	public abstract MultiValueResult<? extends StreamData> getStreamData(
		final String username,
		final String streamId,
		final long streamVersion)
		throws IllegalArgumentException;
	
	/**
	 * Retrieves data specific to a user and a stream.
	 * 
	 * @param username
	 *        The user's username.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamVersion
	 *        The version of the stream.
	 * 
	 * @param pointId
	 *        The unique identifier of the specific point.
	 * 
	 * @return The data that matches the parameters.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter was null.
	 */
	public abstract StreamData getStreamData(
		final String username,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException;
	
	/**
	 * Retrieves data specific to a user and a stream.
	 * 
	 * @param username
	 *        The user's username.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamVersion
	 *        The version of the stream.
	 * 
	 * @param pointId
	 *        The unique identifier of the specific point.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter was null.
	 */
	public abstract void deleteStreamData(
		final String username,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException;
}