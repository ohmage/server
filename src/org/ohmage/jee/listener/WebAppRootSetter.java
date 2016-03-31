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
		// SN: Some piece of the class loading here assumes webapp.root has a trailing slash
		// I don't know who is at fault (probably tomcat. ha!) but let's just hack in the trailing
		// slash to get an easy fix. Note that tomcat breaks if this ends in two slashes, so only
		// add if it doesn't already have the trailing slash.
		if(!System.getProperty("webapp.root").endsWith("/")) {
			System.setProperty("webapp.root", System.getProperty("webapp.root") + "/");
	    }
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
