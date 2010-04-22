package edu.ucla.cens.awserver.jee.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Abstract base class containing utility methods for servlets.
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public abstract class AbstractAwHttpServlet extends HttpServlet {
	private static Logger _logger = Logger.getLogger(AbstractAwHttpServlet.class);
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
	}
	
	/**
	 * @return an OutputStream appropriate for the headers found in the request.
	 */
	protected OutputStream getOutputStream(HttpServletRequest request, HttpServletResponse response) 
		throws IOException {
		
		OutputStream os = null; 
		
		// Determine if the response can be gzipped
		String encoding = request.getHeader("Accept-Encoding");
		if (encoding != null && encoding.indexOf("gzip") >= 0) {
            
			if(_logger.isDebugEnabled()) {
				_logger.debug("returning a GZIPOutputStream");
			}
			
            response.setHeader("Content-Encoding","gzip");
            response.setHeader("Vary", "Accept-Encoding");
            os = new GZIPOutputStream(response.getOutputStream());
		
		} else {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("returning the default OutputStream");
			}
			
			os = response.getOutputStream();
		}
		
		return os;
	}
	
	/**
	 * @return true if the provided value is longer than the provided length and print an informative message to the log 
	 */
	protected boolean greaterThanLength(String longName, String name, String value, int length) {
		
		if(null != value && value.length() > length) {
			
			_logger.warn("a " + longName + "(request parameter " + name + ") of " + value.length() + " characters was found");
			return true;
		}
		
		return false;
	}
}
