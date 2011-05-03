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

import edu.ucla.cens.awserver.domain.ClassInfo;
import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Writes the information about the classes being read unless there are errors
 * in which case it will respond with either an annotated, more-specific,
 * response set by the request or a generic response set when this object is
 * created.
 * 
 * @author John Jenkins
 */
public class ClassReadResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(ClassReadResponseWriter.class);
	
	/**
	 * Constructor for setting up this response writer with a default error
	 * response in the event that the request fails without annotating a
	 * reason.
	 * 
	 * @param errorResponse The ErrorResponse to use in the event that the
	 * 						request fails without annotating a more-specific
	 * 						response.
	 */
	public ClassReadResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the response to this request. If the request has failed, it
	 * checks if a more-specific error message is present. If so, it uses
	 * that; if not, it will use the default message with which it was built.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing class read response.");
		
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
		
		String responseText;
		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
		}
		else {
			// Get the return values from the request.
			String classUrnList;
			try {
				JSONObject requestResponse = new JSONObject();
				requestResponse.put("result", "success");
				
				JSONObject dataResponse = new JSONObject();
				classUrnList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
				String[] classUrnArray = classUrnList.split(",");
				for(int i = 0; i < classUrnArray.length; i++) {
					try {
						ClassInfo classInfo = (ClassInfo) awRequest.getToReturnValue(classUrnArray[i]);
						
						dataResponse.put(classUrnArray[i], classInfo.getJsonRepresentation(false));
					}
					catch(JSONException e) {
						_logger.error("Error while building class information JSONObject.", e);
						responseText = generalJsonErrorMessage();
					}
					catch(IllegalArgumentException e) {
						_logger.error("Missing return value that is the URN for the class " + classUrnArray[i], e);
						responseText = generalJsonErrorMessage();
					}
				}
				requestResponse.put("data", dataResponse);
				
				responseText = requestResponse.toString();
			}
			catch(JSONException e) {
				_logger.error("JSONException while putting response.", e);
				responseText = generalJsonErrorMessage();
			}
			catch(IllegalArgumentException e) {
				_logger.error("Missing class URN list from toProcess map.", e);
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
