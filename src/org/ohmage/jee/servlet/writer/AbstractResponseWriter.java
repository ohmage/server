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
package org.ohmage.jee.servlet.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;


/**
 * Abstract helper class for ResponseWriter utilities.
 * 
 * @author selsky
 */
public abstract class AbstractResponseWriter implements ResponseWriter {
	private static Logger _logger = Logger.getLogger(AbstractResponseWriter.class);
	private ErrorResponse _errorResponse;
	private String _successJson;
	
	/**
	 * @param errorResponse the general JSON error response
	 * @throws IllegalArgumentException if errorResponse is null
	 */
	public AbstractResponseWriter(ErrorResponse errorResponse) {
		if(null == errorResponse) {
			throw new IllegalArgumentException("an ErrorResponse is required");
		}
		_errorResponse = errorResponse;
		_successJson = "{\"result\":\"success\"}";
	}
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        
        // this is done to allow client content to be served up from from different domains than the server data
        // e.g., when you want to run a client in a local sandbox, but retrieve data from a remote server
        response.setHeader("Access-Control-Allow-Origin","*");
	}
	
	/**
	 * There is functionality in Tomcat 6 to perform this action, but it is also nice to have it controlled programmatically.
	 * 
	 * @return an OutputStream appropriate for the headers found in the request.
	 */
	protected OutputStream getOutputStream(HttpServletRequest request, HttpServletResponse response) 
		throws IOException {
		
		OutputStream os = null; 
		
		// Determine if the response can be gzipped
		String encoding = request.getHeader("Accept-Encoding");
		if (encoding != null && encoding.indexOf("gzip") >= 0) {
            
			if(_logger.isDebugEnabled()) {
				_logger.debug("Returning a GZIPOutputStream");
			}
			
            response.setHeader("Content-Encoding","gzip");
            response.setHeader("Vary", "Accept-Encoding");
            os = new GZIPOutputStream(response.getOutputStream());
		
		} else {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("Returning the default OutputStream");
			}
			
			os = response.getOutputStream();
		}
		
		return os;
	}
	
	protected String generalJsonErrorMessage() {
		try {
			JSONObject root = new JSONObject();
			JSONObject msg = new JSONObject();
			JSONArray a = new JSONArray();
			root.put("result", "failure");
			msg.put("code", _errorResponse.getCode());
			msg.put("text", _errorResponse.getText());
			a.put(msg);
			root.put("errors", a);
			return root.toString();
		} catch (JSONException jsone) { // if this occurs there is a logical error
			_logger.error(jsone);
			throw new IllegalStateException(jsone);
		}
	}

	protected String generalJsonSuccessMessage() {
		return _successJson;
	}
}
