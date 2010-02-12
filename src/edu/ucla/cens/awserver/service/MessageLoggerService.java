package edu.ucla.cens.awserver.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
			
			String uploadFileName = catalinaBase + "/logs/uploads/" + fileName  + "-upload.json";
			printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(uploadFileName))));
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
						jsonObject.put("request_type", awRequest.getAttribute("requestType"));
						jsonObject.put("user", awRequest.getUser().getUserName());
						jsonObject.put("phone_version", awRequest.getAttribute("phoneVersion"));
						jsonObject.put("protocol_version", awRequest.getAttribute("protocolVersion"));
						jsonObject.put("upload_file_name", uploadFileName);
						
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
			Map<Integer, List<Integer>> duplicatePromptResponseMap 
				= (Map<Integer, List<Integer>>) awRequest.getAttribute("duplicatePromptResponseMap");
			
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				
				int size = duplicateIndexList.size();
				int lastDuplicateIndex = -1;
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-upload-duplicates.json"))));
				
				for(int i = 0; i < size; i++) {
					
					int duplicateIndex = duplicateIndexList.get(i);
					
					if("prompt".equals((String)awRequest.getAttribute("requestType"))) {

						if(lastDuplicateIndex != duplicateIndex) {
							List<Integer> list = duplicatePromptResponseMap.get(duplicateIndex);
							JSONObject outputObject = new JSONObject();
							JSONArray jsonArray = new JSONArray(list);
							JSONObject dataPacket = JsonUtils.getJsonObjectFromJsonArray((JSONArray) data, duplicateIndex);
							
							try {
								outputObject.put("duplicatePromptIds", jsonArray);
								outputObject.put("dataPacket", dataPacket);
							} catch (JSONException jsone) {
								throw new IllegalStateException("attempt to add duplicateConfigIds array to data JSON Object for" +
										" duplicate logging caused JSONException: " + jsone.getMessage());
							}
							lastDuplicateIndex = duplicateIndex;
							printWriter.write(outputObject.toString());
					    }
					} else {
						
						printWriter.write(JsonUtils.getJsonObjectFromJsonArray((JSONArray) data, duplicateIndex).toString());
					}
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
