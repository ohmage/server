package org.ohmage.cache;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.ohmage.exception.ServiceException;
import org.ohmage.service.UserServices;
import org.springframework.beans.factory.DisposableBean;

public final class RegistrationCleanup extends TimerTask implements DisposableBean {
	/**
	 * The logger.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(RegistrationCleanup.class);
	
	/**
	 * The cleanup task that is periodically run to clean up expired 
	 * registration requests.
	 */
	private static final Timer CLEANUP = 
		new Timer(
			"RegistrationCleanup - Removing expired registration requests.",
			true);
	
	/**
	 * The number of milliseconds between each cleanup.
	 */
	private static final long MILLISECONDS_BETWEEN_CLEANUPS = 1000 * 60 * 5;
	
	/**
	 * Default constructor that will be called by Spring via reflection.
	 */
	private RegistrationCleanup() {
		LOGGER.info("Creating the registration cleanup, periodic task.");
		
		// Create the task that will be run periodically.
		CLEANUP.schedule(
			this, 
			MILLISECONDS_BETWEEN_CLEANUPS, 
			MILLISECONDS_BETWEEN_CLEANUPS);
	}

	/**
	 * Calls to the user services layer that cleans up the expired 
	 * registrations.
	 */
	@Override
	public void run() {
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.info("Cleaning up expired registrations.");
			}
			UserServices.instance().deleteExpiredRegistration();
		}
		catch(ServiceException e) {
			LOGGER.error("Failed to clean up the exired registrations.", e);
		}
	}

	/**
	 * Stops the cleanup task.
	 */
	@Override
	public void destroy() throws Exception {
		CLEANUP.cancel();
	}
}
