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
 * Wrapper to disallow Cross-Origin-Resource-Sharing for URIs given as
 * a comma-separated list init-param to this Filter.
 * 
 * @author Joshua Selsky
 */
public class OhmageCORSFilter extends CORSFilter {
	private static Logger LOGGER = Logger.getLogger(OhmageCORSFilter.class);
	private List<String> disallowedURIs = null;
	
	/**
	 * Called on web application start up. Initializes the CORSFilter and 
	 * stores any disallowed URIs locally.
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
	 * Dispatches to the parent Filter if the URI on the request is not present
	 * in the disallowedURIs list.
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {
		
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			
			if(disallowedURIs.contains(((HttpServletRequest) request).getRequestURI())) {
				// Forward to the next filter in the chain
				chain.doFilter(request, response);
				
			} else {
				// Cast to HTTP
				doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
			}
		}
		else {
			throw new ServletException("Cannot filter non-HTTP requests/responses");	
		}
	}
}
