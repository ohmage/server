package org.ohmage.jee.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.web.util.WebUtils;

/**
 * Sets up logging when the web app is started.
 *
 * @author John Jenkins
 */
public class LogListener implements ServletContextListener {
	/**
	 * The property key for the log preference file.
	 */
	public static final String PROPERTY_LOG_LOCATION = 
		"ohmage.logPropertiesFile";
	
	/**
	 * The default location within this app for the 
	 */
	public static final String DEFAULT_LOG_LOCATION_IN_APP =
		"/WEB-INF/properties/log4j.properties";
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		// This is required to setup the web app's root for our use.
		WebUtils.setWebAppRootSystemProperty(event.getServletContext());
		
		// Get the system property if it was set.
		String logPropertiesFile = System.getProperty(PROPERTY_LOG_LOCATION);
		
		// If a properties file was not given, use the system default.
		if(logPropertiesFile == null) {
			logPropertiesFile = 
				System.getProperty("webapp.root") +
					DEFAULT_LOG_LOCATION_IN_APP;
		}
		
		// Setup the logging.
		PropertyConfigurator.configure(logPropertiesFile);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		// Does nothing.
	}
}