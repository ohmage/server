package org.ohmage.service;

import org.ohmage.domain.Probe;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IProbeQueries;

public class ProbeServices {
	private static ProbeServices instance;
	private IProbeQueries probeQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iProbeQueries is null
	 */
	private ProbeServices(final IProbeQueries iProbeQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iProbeQueries == null) {
			throw new IllegalArgumentException("An instance of IProbeQueries is required.");
		}
		
		probeQueries = iProbeQueries;
		instance = this;
	}
	
	/**
	 * The instance of this service.
	 * 
	 * @return  Returns the singleton instance of this class.
	 */
	public static ProbeServices instance() {
		return instance;
	}
	
	/**
	 * Creates a new probe in the system.
	 * 
	 * @param probe The probe.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void createProbe(final Probe probe) throws ServiceException {
		try {
			probeQueries.createProbe(probe);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
