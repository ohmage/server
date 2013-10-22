/*******************************************************************************
 * Copyright 2013 Open mHealth
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
package org.ohmage.servlet.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.HttpStatusCodeExceptionResponder;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.OhmageException;
import org.springframework.web.util.NestedServletException;

/**
 * <p>
 * A filter responsible for catching exceptions thrown by the requests and
 * populating the response accordingly.
 * </p>
 * 
 * <p>
 * For example, HTTP responses have their status code set to
 * {@link HttpServletResponse#SC_BAD_REQUEST} and the body of the response is
 * the error message.
 * </p>
 * 
 * @author John Jenkins
 */
public class ExceptionFilter implements Filter {
	/**
	 * The attribute key used to store any exception that was thrown for this
	 * request.
	 */
	public static final String ATTRIBUTE_KEY_EXCEPTION = "omh.exception";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = Logger
		.getLogger(ExceptionFilter.class.getName());

	/**
	 * Does nothing.
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		// Do nothing.
	}

	/**
	 * <p>
	 * If the request throws an exception, specifically a OmhException,
	 * attempt to respond with that message from the exception.
	 * </p>
	 * 
	 * <p>
	 * For example, HTTP responses have their status codes changed to
	 * {@link HttpServletResponse#SC_BAD_REQUEST} and the body of the response
	 * is the error message.
	 * </p>
	 */
	@Override
	public void doFilter(
		final ServletRequest request,
		final ServletResponse response,
		final FilterChain chain) throws IOException, ServletException {

		// Get a handler for the correct exception type.
		Throwable exception = null;

		// Always let the request continue but setup to catch exceptions.
		try {
			chain.doFilter(request, response);
		}
		// The servlet container may wrap the exception, in which case we
		// must first unwrap it, then delegate it.
		catch(NestedServletException e) {
			// Get the underlying cause.
			Throwable cause = e.getCause();

			// If the underlying exception is one of ours, then store the
			// underlying exception.
			if(cause instanceof OhmageException) {
				exception = cause;
			}
			// Otherwise, store this exception.
			else {
				exception = e;
			}
		}
		// Otherwise, store the exception,
		catch(Throwable e) {
			exception = e;
		}

		// If an exception was thrown, handle it.
		if(exception != null) {
			// Echo the exception to the logs.
			// For an OhmageException defer to its own level reporting.
			if(exception instanceof OhmageException) {
				LOGGER
					.log(
						((OhmageException) exception).getLevel(),
						exception.getMessage(),
						exception);
			}
			// Otherwise, report this exception at the highest level.
			else {
				LOGGER
					.log(
						Level.SEVERE,
						"An unknown exception was thrown.",
						exception);
			}
			
			// Save the exception in the request.
			request.setAttribute(ATTRIBUTE_KEY_EXCEPTION, exception);
			
			// If this is a HTTP request, set the status code.
			if(response instanceof HttpServletResponse) {
				HttpServletResponse httpResponse =
					(HttpServletResponse) response;
				
				// Generate the appropriate status code.
				// If the exception itself would like to inject its own status
				// code, defer to that.
				if(exception instanceof HttpStatusCodeExceptionResponder) {
					httpResponse
						.setStatus(
							((HttpStatusCodeExceptionResponder) exception)
								.getStatusCode());
				}
				// If the user's authentication and/or authorization
				// credentials were missing when required, contradictory, or
				// unknown or the user simply did not have permission to
				// perform the attempted operation, set the status code to some
				// 'unauthorized' status.
				else if(
					(exception instanceof AuthenticationException) ||
					(exception instanceof InsufficientPermissionsException)) {
					
					httpResponse
						.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
				// If we do not have a specific handler this exception but it
				// was one we knew about and handled, set the status code to
				// simply indicate that the request was bad, which should cause
				// the user to defer to the actual response text.
				else if(exception instanceof OhmageException) {
					httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
				// Otherwise, we completely missed this exception and have a
				// bug in our own code.
				else {
					httpResponse
						.setStatus(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
			
			// If it is a known exception set the body of the message based on
			// the exception.
			if(exception instanceof OhmageException) {
				response.getOutputStream().print(exception.getMessage());
			}
			// Otherwise, don't return anything to the user other than the
			// status code.
		}
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void destroy() {
		// Do nothing.
	}
}