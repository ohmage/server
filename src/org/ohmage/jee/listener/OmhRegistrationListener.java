package org.ohmage.jee.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ohmage.cache.OmhThirdPartyRegistration;
import org.ohmage.domain.OhmagePayloadId;

/**
 * <p>
 * Registers the campaign and observer payload IDs.
 * </p>
 *
 * @author John Jenkins
 */
public class OmhRegistrationListener implements ServletContextListener {
	/**
	 * Default constructor.
	 */
	public OmhRegistrationListener() {
		// Do nothing.
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		// Register the ohmage payload IDs.
		OmhThirdPartyRegistration
			.registerDomain("ohmage", OhmagePayloadId.class);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		// Do nothing.
	}
}