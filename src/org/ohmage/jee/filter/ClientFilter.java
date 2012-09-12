package org.ohmage.jee.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

public class ClientFilter implements Filter {
	/**
	 * The logger for the filter.
	 */
	private static final Logger LOGGER = Logger.getLogger(ClientFilter.class);
	
	/**
	 * The maximum allowed length for the client parameter.
	 */
	private static final int MAX_CLIENT_LENGTH = 255;
	
	/**
	 * The attribute key used to indicate whether or not the client value has
	 * been pushed to the NDC.
	 */
	public static final String ATTRIBUTE_KEY_CLIENT = "_client_";
	
	/**
	 * A class for representing invalid client exceptions.
	 *
	 * @author John Jenkins
	 */
	private static final class InvalidClientException extends Exception {
		/**
		 * The serial version UID as required by Extension.
		 */
		private static final long serialVersionUID = 1253794856093055279L;
		
		/**
		 * An exception with only a reason.
		 * 
		 * @param reason The reason the client value was invalid.
		 */
		public InvalidClientException(final String reason) {
			super(reason);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// Do nothing.
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// Do nothing.
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void doFilter(
			final ServletRequest request,
			final ServletResponse response,
			final FilterChain chain)
			throws IOException, ServletException {
		
		String client = null;
		boolean rejectedClient = false;
		
		// This is only applicable if this is a HTTP request.
		if(	(request instanceof HttpServletRequest) &&
			(response instanceof HttpServletResponse)) {
		
			// Cast the HTTP servlet response object.
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			// We make the assumption that we are the only one setting this
			// value, so it must be a map. If it is not, we will just let the  
			// request continue.
			Object parametersObject = 
				request.getAttribute(GzipFilter.ATTRIBUTE_KEY_PARAMETERS);
			
			// If this value is null, then there were no parameters and if it
			// is not a map, then we are in big trouble.
			if(parametersObject instanceof Map) {
				// First, try to do it using ohmage's "client" parameter.
				try {
					client =
						retrieveClient(
							(Map<String, String[]>) parametersObject);
				}
				catch(InvalidClientException e) {
					// Log the problem.
					LOGGER
						.info(
							"Rejecting the invalid client value: " +
								e.getMessage());
					
					// Report the error to the user.
					httpResponse
						.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							e.getMessage());
					
					// Set the flag so the rest of the request is ignored.
					rejectedClient = true;
				}
				
				// If that doesn't work, try using OMH's "requester" parameter.
				if(client == null) {
					try {
						client =
							retrieveRequester(
								(Map<String, String[]>) parametersObject);
					}
					catch(InvalidClientException e) {
						// Log the problem.
						LOGGER
							.info(
								"Rejecting the invalid requester value: " +
									e.getMessage());
						
						// Report the error to the user.
						httpResponse
							.sendError(
								HttpServletResponse.SC_BAD_REQUEST,
								e.getMessage());
						
						// Set the flag so the rest of the request is ignored.
						rejectedClient = true;
					}
				}
			}
		}
		
		// If we successfully retrieved a client value, push it to the NDC and
		// add it as an attribute for the request.
		if(client != null) {
			NDC.push("client=" + client);
			request.setAttribute(ATTRIBUTE_KEY_CLIENT, client);
		}
		
		// If the client value was not rejected, pass the request onto the rest
		// of the chain.
		if(! rejectedClient) {
			chain.doFilter(request, response);
		}
		
		// If the client was pushed onto the stack, be sure to pop it off.
		if(client != null) {
			NDC.pop();
		}
	}
	
	/**
	 * Validates the client value from the parameters.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @return The client value.
	 * 
	 * @throws InvalidClientException Multiple client values were given or the
	 * 								  one given client value was invalid.
	 */
	private final String retrieveClient(
			final Map<String, String[]> parameters)
			throws InvalidClientException {
		
		// Get the list of clients.
		String[] clients = parameters.get(InputKeys.CLIENT);
		
		// If there were no client parameters, return null.
		if(clients == null) {
			return null;
		}
		
		// If there are multiple clients, throw an error.
		if(clients.length > 1) {
			throw
				new InvalidClientException(
					"More than one client value was given.");
		}
		else if(clients.length == 1) {
			// Get the client.
			return validateClient(clients[0]);
		}
		
		// If there was no client value, return null.
		return null;
	}
	
	/**
	 * Validates the requester value from the parameters.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @return The requester value.
	 * 
	 * @throws InvalidClientException Multiple requester values were given or 
	 * 								  the one given requester value was
	 * 								  invalid.
	 */
	private final String retrieveRequester(
			final Map<String, String[]> parameters)
			throws InvalidClientException {
		
		// Get the list of requesters.
		String[] requesters = parameters.get(InputKeys.OMH_REQUESTER);
		
		// If there were no requester values, return null.
		if(requesters == null) {
			return null;
		}
		
		// If there are multiple requesters, throw an error.
		if(requesters.length > 1) {
			throw
				new InvalidClientException(
					"More than one requester value was given.");
		}
		else if(requesters.length == 1) {
			// Get the requester.
			return validateClient(requesters[0]);
		}
		
		// If there was no requester value, return null.
		return null;
	}
	
	/**
	 * Validates that a client value is valid.
	 * 
	 * @param client The client value to be validated.
	 * 
	 * @return Returns the client value.
	 * 
	 * @throws InvalidClientException The client value was invalid.
	 */
	private final String validateClient(
			final String client)
			throws InvalidClientException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return null;
		}
		
		if(client.length() > MAX_CLIENT_LENGTH) {
			throw new InvalidClientException("The client value is too long.");
		}
		
		return client;
	}
}