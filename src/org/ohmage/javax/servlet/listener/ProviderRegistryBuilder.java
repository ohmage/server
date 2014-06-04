package org.ohmage.javax.servlet.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

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
		Logger.getLogger(ProviderRegistryBuilder.class.getName());
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		LOGGER.log(Level.INFO, "Building the entries for the registry.");
		
		LOGGER.log(Level.FINE, "Adding the Google provider.");
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