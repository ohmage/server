package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Abstract helper class for ResponseWriter utilities.
 * 
 * @author selsky
 */
public abstract class AbstractResponseWriter implements ResponseWriter {
	private static Logger _logger = Logger.getLogger(AbstractResponseWriter.class);
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
	}
	
	/**
	 * There is functionality in Tomcat 6 to perform this action, but it is nice to have in explicitly documented in our code.
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

}
