package org.ohmage.bin;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.domain.ColumnList;
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
     * Determines which stream data point response IDs already exist for the
     * given owner and stream.
     *
     * @param owner
     *        The owner's unique identifier.
     *
     * @param streamId
     *        the survey response's unique identifier.
     *
     * @param streamVersion
     *        The stream's version.
     *
     * @param candidateIds
     *        The stream data point IDs to check against.
     *
     * @return The duplicate stream data point IDs.
     *
     * @throws IllegalArgumentException
     *         A required parameter was null.
     */
    public abstract List<String> getDuplicateIds(
            final String owner,
            final String streamId,
            final long streamVersion,
            final Set<String> candidateIds)
            throws IllegalArgumentException;

	/**
     * Retrieves data specific to a user and a stream.
     *
     * @param streamId
     *        The stream's unique identifier.
     *
     * @param streamVersion
     *        The version of the stream.
     *
     * @param userIds
     *        The users' unique identifier whose data should be returned.
     *
     * @param startDate
     *        The earliest time for any given point. Null indicates that there
     *        is no earliest time.
     *
     * @param endDate
     *        The latest time for any given point. Null indicates that there is
     *        no latest time.
     *
     * @param columnList
     *        The projection of columns that should be returned. Null indicates
     *        that all columns should be returned.
     *
     * @param chronological
     *        Whether or not the data should be sorted in chronological order
     *        (as opposed to reverse-chronological order).
     *
     * @param numToSkip
     *        The number of stream data points to skip.
     *
     * @param numToReturn
     *        The number of stream data points to return.
     *
     * @return The data that matches the parameters.
     *
     * @throws IllegalArgumentException
     *         A required parameter was null.
     */
	public abstract MultiValueResult<? extends StreamData> getStreamData(
		final String streamId,
		final long streamVersion,
        final Set<String> userIds,
        final DateTime startDate,
        final DateTime endDate,
        final ColumnList columnList,
        final boolean chronological,
        final long numToSkip,
        final long numToReturn)
		throws IllegalArgumentException;

	/**
	 * Retrieves a specific stream data point.
	 *
	 * @param userId
	 *        The user's unique identifier.
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
	 * @return The specific stream data point that matches the parameters.
	 *
	 * @throws IllegalArgumentException
	 *         A required parameter was null.
	 */
	public abstract StreamData getStreamData(
		final String userId,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException;

    /**
	 * Deletes a specific stream data point.
	 *
	 * @param userId
	 *        The user's unique identifier.
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
		final String userId,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException;
}