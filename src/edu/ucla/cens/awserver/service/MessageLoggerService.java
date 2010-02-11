package edu.ucla.cens.awserver.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Service for logging details about data uploads.
 * 
 * @author selsky
 */
public class MessageLoggerService implements Service {
	private static Logger _uploadLogger = Logger.getLogger("uploadLogger");
	private static Logger _logger = Logger.getLogger(MessageLoggerService.class);
	
	/**
	 * Performs the following tasks: logs the user's upload to the filesystem, logs the failed response message to the filesystem
	 * (if the request failed), and logs a statistic message to the upload logger. 
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to log files and stats about a device upload");
		logUploadToFilesystem(awRequest);
		logUploadStats(awRequest);
		_logger.info("finished with logging files and stats about a device upload");
	}
	
	private void logUploadStats(AwRequest awRequest) {
		Object data = awRequest.getAttribute("jsonData");
		String totalNumberOfMessages = "unknown";
		String numberOfDuplicates = "unknown";
		
		if(data instanceof JSONArray) {
			totalNumberOfMessages = String.valueOf(((JSONArray) data).length());
			
			List<Integer> duplicateIndexList = (List<Integer>) awRequest.getAttribute("duplicateIndexList");
			
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				numberOfDuplicates = String.valueOf(duplicateIndexList.size()); 
			} else {
				numberOfDuplicates = "0";
			}
		}	
		
		long processingTime = System.currentTimeMillis() - (Long) awRequest.getAttribute("startTime");
		
		StringBuilder builder = new StringBuilder();
		
		if(awRequest.isFailedRequest()) {
			builder.append("failed_upload");
		} else {
			builder.append("successful_upload");
		}
		
		String user = awRequest.getUser().getUserName();
		if(null == user) {
			user = "unknown.user";
		}
		
		builder.append(" user=" + user);
		builder.append(" requestType=" + (String) awRequest.getAttribute("requestType"));
		builder.append(" numberOfRecords=" + totalNumberOfMessages);
		builder.append(" numberOfDuplicates=" + numberOfDuplicates);
		builder.append(" proccessingTimeMillis=" + processingTime);
		
		_uploadLogger.info(builder.toString());
		
	}

	private void logUploadToFilesystem(AwRequest awRequest) {
		// Persist the devices's upload to the filesystem 		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String userName = null == awRequest.getUser().getUserName() ? "unknown.user" : awRequest.getUser().getUserName(); 
		
		String fileName = userName + "-" + sdf.format(new Date());
		
		String catalinaBase = System.getProperty("catalina.base"); // need a system prop called upload-logging-directory or something like that
		
		Object data = awRequest.getAttribute("jsonData"); // could either be a String or a JSONArray depending on where the main
		                                                  // application processing ended
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-upload.json"))));
			if(null != data) {			
				printWriter.write(data.toString());
			} else {
				printWriter.write("no data");
			}
			close(printWriter);
		
			if(awRequest.isFailedRequest()) {
				
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-failed-upload-response.json"))));
				String failedMessage = awRequest.getFailedRequestErrorMessage();
				
				if(null != failedMessage) {
					
					JSONObject jsonObject = null;
					
					try {
						
						jsonObject = new JSONObject(failedMessage);
						// Dump out the request params
						jsonObject.put("requestType", awRequest.getAttribute("requestType"));
						jsonObject.put("user", awRequest.getUser().getUserName());
						jsonObject.put("phoneVersion", awRequest.getAttribute("phoneVersion"));
						jsonObject.put("protocolVersion", awRequest.getAttribute("protocolVersion"));
						
					} catch (JSONException jsone) {
						
						throw new IllegalArgumentException("invalid JSON in failedRequestErrorMessage -- logical error! JSON: " + failedMessage);
					}
										
					printWriter.write(jsonObject.toString());
					
				} else {
					
					printWriter.write("no failed upload message found");
					
				}
				
				close(printWriter);
			}
			
			List<Integer> duplicateIndexList = (List<Integer>) awRequest.getAttribute("duplicateIndexList");
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-upload-duplicates.json"))));
				
				for(Integer index : duplicateIndexList) {
					printWriter.write(JsonUtils.getJsonObjectFromJsonArray((JSONArray) data, index).toString());
				}
				
				close(printWriter);
			}
		}
		catch(IOException ioe) {
	
			_logger.warn("caught IOException when logging upload data to the filesystem. " + ioe.getMessage());
			close(printWriter);
			throw new ServiceException(ioe);
		
		} 
	}

	private void close(PrintWriter writer) {
		if(null != writer) {
			writer.flush();
			writer.close();
			writer = null;
		}
	}
}
