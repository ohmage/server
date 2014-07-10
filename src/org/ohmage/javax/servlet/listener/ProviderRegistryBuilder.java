package org.ohmage.javax.servlet.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ohmage.auth.provider.GoogleProvider;
import org.ohmage.auth.provider.ProviderRegistry;

/**
 * <p>
 * Builds the provider registry.
 * </p>
 *
 * @author John Jenkins
 */
public class ProviderRegistryBuilder implements ServletContextListener {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(ProviderRegistryBuilder.class.getName());
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		LOGGER.info("Building the entries for the registry.");
		
		LOGGER.debug("Adding the Google provider.");
		ProviderRegistry.register(new GoogleProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Do nothing.
	}
}
