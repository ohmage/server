package org.ohmage.request.omh;

import org.joda.time.DateTime;
import org.ohmage.exception.ServiceException;

/**
 * <p>
 * An interface that signals that an Open mHealth read request can be serviced
 * by the implementing class.
 * </p>
 *
 * @author John Jenkins
 */
public interface OmhReadServicer {
	/**
	 * Services the request with the Open mHealth read parameters.
	 * 
	 * @param owner
	 *        The username of user about whom data is being read. This may be
	 *        null if the requesting user is requesting data about themselves.
	 * 
	 * @param startDate
	 *        The earliest timestamp for a data point. If null, the earliest
	 *        existing point should be used.
	 * 
	 * @param endDate
	 *        The latest timestamp for a data point. If null, there is no
	 *        timestamp limit.
	 * 
	 * @param numToSkip
	 *        The number of points to skip based on the ordering guaranteed by
	 *        the server.
	 * 
	 * @param numToReturn
	 *        The number of points to return.
	 * 
	 * @throws ServiceException
	 *         There was a problem servicing the request.
	 */
	public abstract void service(
		final String owner,
		final DateTime startDate,
		final DateTime endDate,
		final long numToSkip,
		final long numToReturn);
}