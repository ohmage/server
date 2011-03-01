package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * Writer for image output.
 * 
 * @author selsky
 */
public class ImageQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(ImageQueryResponseWriter.class);
	
	public ImageQueryResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		OutputStream os = null;
		InputStream is = null;
		
		try {
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {

				response.setContentType("image/jpeg");
				
				os = new DataOutputStream(getOutputStream(request, response));
				File imageFile = new File(new URI(((MediaQueryAwRequest)awRequest).getMediaUrl()));
				is = new DataInputStream(new FileInputStream(imageFile));
				
				int length = (int) imageFile.length(); // in general, this app should never have images over 2MB
				int chunkSize = 1024;
				int numberOfBytes = chunkSize;
				byte[] bytes = new byte[chunkSize];
				int start = 0;
				
				while(start < length) {
					
					if(start + chunkSize > length) {
						numberOfBytes = length - start;
					}
					
					is.read(bytes, 0, numberOfBytes);
					os.write(bytes, 0, numberOfBytes);
					
					start += chunkSize;	
				}
				
			} else {
			
				// Prepare for sending the response to the client
				writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
				String responseText = null;
				
				response.setContentType("application/json");
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
				
				_logger.info("about to write JSON output");
				writer.write(responseText);
			}
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("an unrecoverable exception occurred while generating a response", e);
			
			try {
				if(null == writer) {
					writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
				}
				response.setContentType("application/json");
				writer.write(generalJsonErrorMessage());
				
			} catch (Exception ee) {
				
				_logger.error("caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
			
			if(null !=  os) {
				
				try {
					
					os.flush();
					os.close();
					os = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
			
			if(null !=  is) {
				
				try {
					
					is.close();
					is = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}
}
