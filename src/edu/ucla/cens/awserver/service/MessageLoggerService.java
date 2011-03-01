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

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Service for logging details about data uploads.
 * 
 * TODO - update when all new upload types are finished
 * 
 * @author selsky
 */
public class MessageLoggerService implements Service {
	private static Logger _uploadLogger = Logger.getLogger("uploadLogger");
	private static Logger _logger = Logger.getLogger(MessageLoggerService.class);
	
	private String _requestType;
	
	public MessageLoggerService(String requestType) {
		if(StringUtils.isEmptyOrWhitespaceOnly(requestType)) {
			throw new IllegalArgumentException("a requestType is required");
		}
		_requestType = requestType;
	}
	 
	
	/**
	 * Performs the following tasks: logs the user's upload to the filesystem, logs the failed response message to the filesystem
	 * (if the request failed), and logs a statistic message to the upload logger. 
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to log files and stats about an upload");
		
		if(awRequest.getUser().isLoggedIn()) { // avoid filling the filesystem up with junk from non-logged in users
			logUploadToFilesystem(awRequest);
		}
		
		logUploadStats(awRequest);
		
		_logger.info("finished with logging files and stats about an upload");
	}
	
	private void logUploadStats(AwRequest awRequest) {
		JSONArray data = awRequest.getJsonDataAsJsonArray();
		String totalNumberOfMessages = "unknown";
		String numberOfDuplicates = "unknown";
		String sessionId = awRequest.getSessionId();
		
		if(null != data) {
			totalNumberOfMessages = String.valueOf(((JSONArray) data).length());
			
			List<Integer> duplicateIndexList = awRequest.getDuplicateIndexList();
			
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				numberOfDuplicates = String.valueOf(duplicateIndexList.size()); 
			} else {
				numberOfDuplicates = "0";
			}
		}	
		
		long processingTime = System.currentTimeMillis() - awRequest.getStartTime();
		
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
		builder.append(" isLoggedIn=" + awRequest.getUser().isLoggedIn());
		builder.append(" sessionId=" + sessionId);
		builder.append(" requestType=" + _requestType);
		builder.append(" numberOfRecords=" + totalNumberOfMessages);
		builder.append(" numberOfDuplicates=" + numberOfDuplicates);
		builder.append(" proccessingTimeMillis=" + processingTime);
		
		_uploadLogger.info(builder.toString());
	}

	private void logUploadToFilesystem(AwRequest awRequest) {
		String sessionId = awRequest.getSessionId();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String userName = awRequest.getUser().getUserName(); 
		
		String fileName = userName + "-" + sdf.format(new Date());
		
		String catalinaBase = System.getProperty("catalina.base"); // TODO need a system prop called upload-logging-directory or something like that
		
		JSONArray data = awRequest.getJsonDataAsJsonArray(); 
		PrintWriter printWriter = null;
		
		try {
			
			// First log the upload
			
			String uploadFileName = catalinaBase + "/logs/uploads/" + fileName  + "-upload.json";
			printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(uploadFileName))));
			printWriter.write("{\"sessionId\":\"" + sessionId + "\"}");
			
			if(null != data) {
				printWriter.write(data.toString());
			} else {
				String jsonString = awRequest.getJsonDataAsString();
				if(null != jsonString) {
					printWriter.write(jsonString);
				} else {
					printWriter.write("no data");
				}
			}
			close(printWriter);
		
			// Now log the failed request
			
			if(awRequest.isFailedRequest()) {
				
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-failed-upload-response.json"))));
				String failedMessage = awRequest.getFailedRequestErrorMessage();
				
				if(null != failedMessage) {
					
					JSONObject jsonObject = null;
					
					try {
						
						jsonObject = new JSONObject(failedMessage);
						// Dump out the request params
						jsonObject.put("session_id", sessionId);
						jsonObject.put("request_type", _requestType);
						jsonObject.put("user", awRequest.getUser().getUserName());
						jsonObject.put("client", awRequest.getClient());
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
			
			// Finally, log duplicates if there are any
			
			List<Integer> duplicateIndexList = awRequest.getDuplicateIndexList();
						
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				
				int size = duplicateIndexList.size();
//				int lastDuplicateIndex = -1;
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/logs/uploads/" + fileName  + "-upload-duplicates.json"))));
				printWriter.write("{\"sessionId\":\"" + sessionId + "\"}");
				
				for(int i = 0; i < size; i++) {
					
					int duplicateIndex = duplicateIndexList.get(i);
//					
//					// if("prompt".equals(awRequest.getRequestType())) {
//					if(awRequest instanceof SurveyUploadAwRequest) {
//
//						if(lastDuplicateIndex != duplicateIndex) {
//							List<Integer> list = duplicatePromptResponseMap.get(duplicateIndex);
//							JSONObject outputObject = new JSONObject();
//							JSONArray jsonArray = new JSONArray(list);
//							JSONObject dataPacket = JsonUtils.getJsonObjectFromJsonArray((JSONArray) data, duplicateIndex);
//							
//							try {
//								outputObject.put("duplicatePromptIds", jsonArray);
//								outputObject.put("dataPacket", dataPacket);
//							} catch (JSONException jsone) {
//								throw new IllegalStateException("attempt to add duplicateConfigIds array to data JSON Object for" +
//										" duplicate logging caused JSONException: " + jsone.getMessage());
//							}
//							lastDuplicateIndex = duplicateIndex;
//							printWriter.write(outputObject.toString());
//							printWriter.write("\n");
//					    }
//						
//					} else { // it was a mobility upload
						
						printWriter.write(JsonUtils.getJsonObjectFromJsonArray((JSONArray) data, duplicateIndex).toString());
						printWriter.write("\n");
//					}
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
