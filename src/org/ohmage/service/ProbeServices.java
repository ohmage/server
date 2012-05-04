package org.ohmage.service;


public class ProbeServices {
	private static final ProbeServices instance;
	private static final IProbeQueries probeQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iProbeQueries is null
	 */
	private ProbeServices(IProbeQueries iProbeQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iProbeQueries == null) {
			throw new IllegalArgumentException("An instance of IAuditQueries is required.");
		}
		
		auditQueries = iAuditQueries;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static AuditServices instance() {
		return instance;
	}
}
