/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public class Log4jNdcFilter implements Filter {
	private static Logger _logger = Logger.getLogger(Log4jNdcFilter.class);
	
	/**
	 * Default no-arg constructor.
	 */
	public Log4jNdcFilter() {
		
	}
	
	/**
	 * Does nothing.
	 */
	public void destroy() {
		
	}
	
	/**
	 * Does nothing.
	 */
	public void init(FilterConfig config) throws ServletException {
		
	}
	
	/**
	 * Pushes the current thread's session id into the Log4J NDC.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {	
		
		String sessionId = ((HttpServletRequest) request).getSession().getId(); // The getSession call will create a session
		                                                                        // if one does not exist for the current thread.
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("found sessionId: " + sessionId);
		}
		
		NDC.push(sessionId);
		
		// Execute the rest of the filters in the chain and then whatever Servlet is bound to the current request URI
		chain.doFilter(request, response);
		
		// cleanup -- remove the NDC for the current thread
		NDC.remove();
	}
}
