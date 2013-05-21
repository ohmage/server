package org.ohmage.jee.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.util.WebUtils;

/**
 * <p>
 * Sets the web application root property.
 * </p>
 *
 * @author John Jenkins
 */
public class WebAppRootSetter implements ServletContextListener {
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		// This is required to setup the web app's root for our use.
		WebUtils.setWebAppRootSystemProperty(event.getServletContext());
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
