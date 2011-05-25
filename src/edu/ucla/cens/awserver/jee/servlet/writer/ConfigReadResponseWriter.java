package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ConfigReadRequest;

public class ConfigReadResponseWriter extends AbstractResponseWriter {
	public static Logger _logger = Logger.getLogger(ConfigReadResponseWriter.class);
	
	public ConfigReadResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing config read information.");
		
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
		}
		catch(IOException e) {
			_logger.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(response);
		response.setContentType("application/json");
		
		String responseText = "";
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject result = new JSONObject();
				result.put("result", "success");
				result.put("data", awRequest.getToReturnValue(ConfigReadRequest.RESULT));
				
				responseText = result.toString();
			}
			catch(JSONException e) {
				_logger.error("Error building response JSON.", e);
				awRequest.setFailedRequest(true);
			}
		}
		
		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
		}
		
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			_logger.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			_logger.error("Unable to flush or close the writer.", e);
		}
	}
}