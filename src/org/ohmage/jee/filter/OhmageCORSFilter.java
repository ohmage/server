/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.request.RequestBuilder;
import org.ohmage.util.StringUtils;

import com.thetransactioncompany.cors.CORSFilter;

/**
 * Wrapper around the parent Filter to disallow Cross-Origin Resource Sharing
 * for URIs given as a comma-separated list init-param to this Filter.
 * 
 * @author Joshua Selsky
 */
public class OhmageCORSFilter extends CORSFilter {
	private static Logger LOGGER = Logger.getLogger(OhmageCORSFilter.class);
	private List<String> disallowedURIs = null;
	
	private static final String ORIGIN_REQUEST_HEADER_NAME = "origin";
	private static final String HOST_REQUEST_HEADER_NAME = "host";
	
	/**
	 * Called on web application start up. Performs initialization based on
	 * web.xml and stores any disallowed URIs locally. The disallowed URI list
	 * may be overriden by setting the value for the cors-lenient-mode key to
	 * true in the database's preference table. If lenient mode is enabled, all
	 * URIs allow Cross-Origin requests.
	 */
	@Override
	public void init(final FilterConfig filterConfig)
		throws ServletException {
		
		super.init(filterConfig);
		
		// Check the preference cache to see if CORS lenient mode is enabled
		
		boolean strict = true;
		
		try {
			Boolean lenientMode = StringUtils.decodeBoolean(PreferenceCache.instance().lookup(PreferenceCache.KEY_CORS_LENIENT_MODE));
			if(lenientMode != null && lenientMode) {
				LOGGER.info(PreferenceCache.KEY_CORS_LENIENT_MODE + " enabled");
				strict = false;
			}
		}
		catch(CacheMissException cacheMiss) {
			LOGGER.info(PreferenceCache.KEY_CORS_LENIENT_MODE + " not found: defaulting to strict.");
		}
		
		if(strict) {
			Enumeration<?> en = filterConfig.getInitParameterNames();
			
			while(en.hasMoreElements()) {
				
				String key = (String) en.nextElement();
				
				if(key.equals("disallowedURIs")) {
					
					String value = filterConfig.getInitParameter(key);
					String[] uris = value.split(",");
					disallowedURIs = new ArrayList<String>();
					
					for(String uri : uris) {
						disallowedURIs.add(RequestBuilder.getInstance().getRoot() + uri);
					}
					
					break;
				}
			}
		}
		
		if(disallowedURIs == null) {
			LOGGER.info("All URIs allowed for CORS");
			disallowedURIs = Collections.emptyList();
		}
		else {
			LOGGER.info("The following URIs have been disallowed for CORS: " + disallowedURIs);
		}
	}
	
	/**
	 * For any disallowed URI, restricts the request to follow a Same-Origin
	 * policy. If an origin-host mismatch is detected for a disallowed URI, 
	 * a HTTP 403 Forbidden is returned and execution of the request is
	 * aborted.
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {
		
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			
			if(disallowedURIs.contains(httpServletRequest.getRequestURI())) {
				
				String origin = httpServletRequest.getHeader(ORIGIN_REQUEST_HEADER_NAME);
				String host = httpServletRequest.getHeader(HOST_REQUEST_HEADER_NAME);
				
				if(origin != null) {
				
					if(host == null) {
						LOGGER.info("Could not detect " + HOST_REQUEST_HEADER_NAME + " HTTP header." +
								" Aborting request for restricted URI: " + httpServletRequest.getRequestURI());
						httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					
					if(origin.startsWith("http://")) {
						
						origin = origin.substring(7, origin.length());
						
					} else if(origin.startsWith("https://")) {
						
						origin = origin.substring(8, origin.length());
					}
					
					// Reject the request because the origin does not match
					// the host 
					if(! origin.equals(host)) { 
						
						LOGGER.info("The origin HTTP header does not match the host HTTP header." +
								" Aborting request for restricted URI: " + httpServletRequest.getRequestURI() + " origin="
								+ origin + " host=" + host);
						httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					
					doFilter(httpServletRequest, httpServletResponse, chain);
					
				} 
				else {
				
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("No Origin header found; treating request for URI " + httpServletRequest.getRequestURI() + 
								" as a non-CORS request");
					}
					
					chain.doFilter(request, response);
				}
				
			} else {
				
				chain.doFilter(request, response);
			}
			
		}
		
		else {
			
			throw new ServletException("Cannot filter non-HTTP requests/responses");	
		}
	}
}
