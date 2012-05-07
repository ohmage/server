package org.ohmage.query;

import org.ohmage.domain.Probe;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;

public interface IProbeQueries {
	/**
	 * Creates a new probe in the system.
	 * 
	 * @param probe The probe.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void createProbe(final Probe probe) throws DataAccessException;
}
