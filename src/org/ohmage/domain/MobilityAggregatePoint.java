package org.ohmage.domain;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;

/**
 * This class represents a single Mobility point, but only contains the 
 * information specifically needed when aggregating Mobility information.
 *
 * @author John Jenkins
 */
public class MobilityAggregatePoint implements Comparable<MobilityAggregatePoint> {
	private final DateTime date;
	private final MobilityPoint.Mode mode;
	
	/**
	 * Creates a new point with the given date-time and mode.
	 * 
	 * @param date The date this Mobility point was created.
	 * 
	 * @param mode The mode of this Mobility point.
	 * 
	 * @throws DomainException The date and/or mode were null.
	 */
	public MobilityAggregatePoint(
			final DateTime date, 
			final MobilityPoint.Mode mode) 
			throws DomainException {
		
		if(date == null) {
			throw new DomainException("The date is null.");
		}
		if(mode == null) {
			throw new DomainException("The mode is null.");
		}
		
		this.date = date;
		this.mode = mode;
	}
	
	/**
	 * Returns the date as the milliseconds since epoch.
	 * 
	 * @return The date this Mobility point was made as a long value.
	 */
	public DateTime getDateTime() {
		return date;
	}
	
	/**
	 * Returns the mode.
	 * 
	 * @return The mode of this Mobility point.
	 */
	public MobilityPoint.Mode getMode() {
		return mode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MobilityAggregatePoint other) {
		long difference = date.getMillis() - other.date.getMillis();
		
		return (difference < 0) ? -1 : (difference > 0) ? 1 : 0;
	}
}
