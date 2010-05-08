package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Writer for feature-specific output. 
 * 
 * @author selsky
 */
public interface ResponseWriter {

	/**
	 * Writes the AwRequest to the HttpServletResponse.
	 */
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest);
	
}
