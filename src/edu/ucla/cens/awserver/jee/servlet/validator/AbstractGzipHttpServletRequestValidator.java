package edu.ucla.cens.awserver.jee.servlet.validator;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public abstract class AbstractGzipHttpServletRequestValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(AbstractGzipHttpServletRequestValidator.class);
	
	/**
	 * If the request contains a gzipped input stream, converts it to a parameter map with String keys and String[] values (as in 
	 * the standard Servlet API). If the request is not gzipped, returns request.getParameterMap().  
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String[]> requestToMap(HttpServletRequest request) {
		
		if(! isGzipped(request)) {
			
			return (Map<String, String[]>) request.getParameterMap();
			
		} else {
			
			return map(request);
		}
	}
		
	private boolean isGzipped(HttpServletRequest request) {
		Enumeration<?> e = request.getHeaders("Content-Encoding");
		
		while(e.hasMoreElements()) {
			String s = (String) e.nextElement();
			if(s.equals("gzip")) {
				return true;
			}
		}
		
		return false;
	}
	
	private Map<String, String[]> map(HttpServletRequest request) {
		
		InputStream is = null;
		
		try {
			
			int chunkSize = 1024;
			byte[] input = new byte[chunkSize];
			StringBuilder builder = new StringBuilder();
			int readLen = 0;
			
			is = new BufferedInputStream(new GZIPInputStream(request.getInputStream()));
			
			while((readLen = is.read(input, 0, chunkSize)) != -1) { // -1 is EOF
					
				builder.append(new String(input, 0, readLen));
				
			}
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("ungzipped input post data: " + builder);
			}
			
			Map<String, String[]> parameterMap = new HashMap<String, String[]>();
			
			if(0 != builder.toString().length()) {
				
				// The Map key values are String arrays in order to match HttpServletRequest.getParameterMap()
				
				String[] keyValuePairs = builder.toString().split("&");
				
				for(String keyValuePair : keyValuePairs) {
					String[] splitPair = keyValuePair.split("=");
					String key = splitPair[0]; // _logger.info(key);
					// This only works for keys that map to one value which is ok for the current survey and mobility APIs,  
					// but it may break in the future because HTTP allows multiple values for a single key (that's why 
					// there is a values array)
					String[] value = new String[]{StringUtils.urlDecode(splitPair[1])}; // _logger.info(value[0]);
					
					parameterMap.put(key, value);
				}
			}
			
			return parameterMap;
			
		} catch (EOFException eofe) {
			
			_logger.error("found empty gzipped upload", eofe);
			throw new IllegalStateException(eofe);
			
		} catch (IOException ioe) {
			
			_logger.error("error reading input stream", ioe);
			throw new IllegalStateException(ioe);
			
		} finally {
			
			if(null != is) {
				
				try {
					
					is.close();
					is = null;
					
				} catch (IOException ioe) {
					
					_logger.error("could not successfully close input stream: " + ioe.getMessage());
				}
			}
		}
	}
}
