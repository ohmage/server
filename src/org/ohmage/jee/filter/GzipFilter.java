package org.ohmage.jee.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.util.StringUtils;

/**
 * Filters the requests that have a "Content-Encoding" parameter set to "gzip".
 * 
 * While the HTTP specification specifically states that this should not be
 * allowed, we have been letting it happen. Therefore, to keep 
 * backwards-compatability, we are going to continue to allow it; however, the
 * 3.0 version should remove this and replace it with a filter that rejects
 * such requests. 
 *
 * @author John Jenkins
 */
public class GzipFilter implements Filter {
	/**
	 * The logger for the filter.
	 */
	private static final Logger LOGGER = Logger.getLogger(GzipFilter.class);
	
	/**
	 * The name of the content-encoding header.
	 */
	private static final String KEY_CONTENT_ENCODING = "Content-Encoding";
	/**
	 * The name of the GZIP
	 */
	private static final String VALUE_GZIP = "gzip";
	
	/**
	 * The string used to separate parameters based on the "Content-Type"
	 * "x-www-form-urlencoded" specification.
	 */
	private static final String PARAMETER_SEPARATOR = "&";
	/**
	 * The string used to separate keys from their values for each parameter
	 * based on the "Content-Type" "x-www-form-urlencoded" specification.
	 */
	private static final String PARAMETER_VALUE_SEPARATOR = "=";
	
	/**
	 * A chunk size we use when reading the input stream.
	 */
	private static final int CHUNK_SIZE = 4096;
	
	/**
	 * The attribute key used to store the parameters with the request.
	 */
	public static final String ATTRIBUTE_KEY_PARAMETERS = "_parameters_";
	
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
	public void doFilter(
			final ServletRequest request,
			final ServletResponse response,
			final FilterChain chain)
			throws IOException, ServletException {
		
		// If this is an HTTP request and response, then do our filter.
		if( (request instanceof HttpServletRequest) &&
			(response instanceof HttpServletResponse)) {
			
			// If we successfully retrieved the parameters, continue the chain.
			if(	doFilter(
					(HttpServletRequest) request, 
					(HttpServletResponse) response)) {
				
				chain.doFilter(request, response);
			}
		}
		// Otherwise, continue the chain.
		else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * Retrieves the parameters from the request and saves them as a property
	 * with the request. The property's key is
	 * {@value #ATTRIBUTE_KEY_PARAMETERS}.
	 * 
	 * @param httpRequest The HTTP request that needs to check if the
	 * 					  {@value #KEY_CONTENT_ENCODING} header is
	 * 					  {@value #VALUE_GZIP}.
	 * 
	 * @param httpResponse The HTTP response, which is not used.
	 * 
	 * @return Returns true if a parameter map was successfully 
	 * 		   created / retrieved and saved as part of the request; false,
	 * 		   otherwise. If false is returned, the request should no longer be
	 * 		   processed as an error code should have been set that failed it.
	 */
	private boolean doFilter(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		// Create a reference to the result.
		Map<String, String[]> result = null;
		
		// Get the "Content-Encoding" headers.
		Enumeration<String> contentEncodingHeaders = 
				httpRequest.getHeaders(KEY_CONTENT_ENCODING);
		
		// Look for a GZIP content encoding header.
		boolean containsGzipContentEncoding = false;
		while(contentEncodingHeaders.hasMoreElements()) {
			// If one is found, gunzip the request.
			if(VALUE_GZIP.equals(contentEncodingHeaders.nextElement())) {
				containsGzipContentEncoding = true;
				break;
			}
		}
		
		// If the "Content-Encoding" header was given and its value was "gzip",
		// then we decode the parameters.
		if(containsGzipContentEncoding) {
			result = gunzipRequest(httpRequest, httpResponse);
		}
		// If no "Content-Encoding" header was given, then use the parameters
		// that our servlet container decoded for us.
		else {
			result = httpRequest.getParameterMap();
			
			if(result == null) {
				LOGGER
					.warn(
						"The servlet container failed to create a parameter map.");
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		
		// If our GZIP decoding failed or the HTTP servlet request couldn't
		// generate a map, as opposed to just generating an empty map, we 
		// should return that we failed.
		if(result == null) {
			return false;
		}
		// Otherwise, we have successfully retrieved the parameter map, and
		// we should save it to the request and return success.
		else {
			// Save the parameter map as an element of the request.
			httpRequest.setAttribute(ATTRIBUTE_KEY_PARAMETERS, result);
			return true;
		}
	}
	
	/**
	 * Unzips the parameters using the GZIP encoding. Breaks up the parameters
	 * based on the "Content-Type" "application/x-www-form-urlencoded" 
	 * specification. Creates a map of parameter keys to their URL-decoded
	 * values.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @return The parameter map of parameter keys to their URL-decoded values.
	 */
	private Map<String, String[]> gunzipRequest(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {
		
		// Get the request's InputStream.
		InputStream requestInputStream;
		try {
			requestInputStream = httpRequest.getInputStream();
		}
		catch(IOException e) {
			LOGGER.info("Could not connect to the request's input stream.", e);
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		// Pass it through the GZIP input stream.
		GZIPInputStream gzipInputStream;
		try {
			gzipInputStream = new GZIPInputStream(requestInputStream);
		}
		catch(IOException e) {
			LOGGER.info("The content was not valid GZIP content.", e);
			
			try {
				requestInputStream.close();
			}
			catch(IOException requestIs) {
				LOGGER
					.info(
						"Could not close the request's input stream.", 
						requestIs);
			}
			
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		// Retrieve the parameter list as a string.
		String parameterString;
		try {
			// This will build the parameter string.
			StringBuilder builder = new StringBuilder();
			
			// These will store the information for the current chunk.
			byte[] chunk = new byte[CHUNK_SIZE];
			int readLen = 0;
			
			while((readLen = gzipInputStream.read(chunk)) != -1) {
				builder.append(new String(chunk, 0, readLen));
			}
			
			parameterString = builder.toString();
		}
		catch(IOException e) {
			LOGGER
				.info(
					"The stream was cut off before reading was finished.",
					e);
			return null;
		}
		finally {
			try {
				gzipInputStream.close();
				gzipInputStream = null;
			}
			catch(IOException e) {
				LOGGER.info("Error closing the GZIP input stream.", e);
			}

			try {
				requestInputStream.close();
				requestInputStream = null;
			}
			catch(IOException e) {
				LOGGER.info("Error closing the request's input stream.", e);
			}
		}
		
		// Create the resulting object so that we will never return null.
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		
		// If the parameters string is not empty, parse it for the parameters.
		if(! StringUtils.isEmptyOrWhitespaceOnly(parameterString)) {
			Map<String, List<String>> parameters = 
				new HashMap<String, List<String>>();
			
			// First, split all of the parameters apart.
			String[] keyValuePairs = 
				parameterString.split(PARAMETER_SEPARATOR);
			
			// For each of the pairs, split their key and value and store them.
			for(String keyValuePair : keyValuePairs) {
				// If the pair is empty or null, ignore it.
				if(StringUtils.isEmptyOrWhitespaceOnly(keyValuePair.trim())) {
					continue;
				}
				
				// Split the key from the value.
				String[] splitPair = 
					keyValuePair.split(PARAMETER_VALUE_SEPARATOR);
				
				// If there isn't exactly one key to one value, then there is a
				// problem, and we need to abort.
				if(splitPair.length <= 1) {
					LOGGER
						.info(
							"One of the parameter's 'pairs' did not contain a '" + 
								PARAMETER_VALUE_SEPARATOR + 
								"': " + 
								keyValuePair);
					httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
					return null;
				}
				else if(splitPair.length > 2) {
					LOGGER
						.info(
							"One of the parameter's 'pairs' contained multiple '" + 
								PARAMETER_VALUE_SEPARATOR + 
								"'s: " + 
								keyValuePair);
					httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
					return null;
				}
				
				// The key is the first part of the pair.
				String key = StringUtils.urlDecode(splitPair[0]);
				
				// The first or next value for the key is the second part of 
				// the pair.
				List<String> values = parameters.get(key);
				if(values == null) {
					values = new LinkedList<String>();
					parameters.put(key, values);
				}
				values.add(StringUtils.urlDecode(splitPair[1]));
			}
			
			// Finally, convert the values from Lists to arrays. If we are 
			// going to put a parameter parsing filter in here, we might as
			// well leave them as lists.
			for(String key : parameters.keySet()) {
				parameterMap.put(key, parameters.get(key).toArray(new String[0]));
			}
		}
		
		return parameterMap;
	}
}