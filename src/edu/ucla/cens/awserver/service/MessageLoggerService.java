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
		JSONArray jsonArray = (JSONArray) awRequest.getAttribute("jsonData");
		List<Integer> duplicateIndexList = (List<Integer>) awRequest.getAttribute("duplicateIndexList");
		
		int totalNumberOfMessages = jsonArray.length();
		int numberOfDuplicates = 0;
		if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
			numberOfDuplicates = duplicateIndexList.size(); 
		}
				
		long processingTime = System.currentTimeMillis() - (Long) awRequest.getAttribute("startTime");
		
		StringBuilder builder = new StringBuilder();
		
		if( awRequest.isFailedRequest()) {
			builder.append("failed_upload");
		} else {
			builder.append("successful_upload");
		}
		
		builder.append(" user=" + awRequest.getUser().getUserName());
		builder.append(" requestType=" + (String) awRequest.getAttribute("requestType"));
		builder.append(" numberOfRecords=" + totalNumberOfMessages);
		builder.append(" numberOfDuplicates=" + numberOfDuplicates);
		builder.append(" proccessingTimeMillis=" + processingTime);
		
		_uploadLogger.info(builder.toString());
	}

	private void logUploadToFilesystem(AwRequest awRequest) {
		// Persist the devices's upload to the filesystem 		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String fileName = awRequest.getUser().getUserName() + "-" + sdf.format(new Date());
		String catalinaBase = System.getProperty("catalina.base"); // need a system prop called upload-logging-directory or something like that

		JSONArray jsonArray = (JSONArray) awRequest.getAttribute("jsonData");
		
		try {
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/uploads/" + fileName  + "-upload.json")))); 
			printWriter.write(jsonArray.toString());
			close(printWriter);
		
			if(awRequest.isFailedRequest()) {
				
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/uploads/" + fileName  + "-upload-response.json")))); 
				printWriter.write(awRequest.getFailedRequestErrorMessage());
				close(printWriter);
			}
			
			List<Integer> duplicateIndexList = (List<Integer>) awRequest.getAttribute("duplicateIndexList");
			if(null != duplicateIndexList && duplicateIndexList.size() > 0) {
				
				printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/uploads/" + fileName  + "-upload-duplicates.json"))));
				
				for(Integer index : duplicateIndexList) {
					printWriter.write(JsonUtils.getJsonObjectFromJsonArray(jsonArray, index).toString());
				}
				
				close(printWriter);
			}
		}
		catch(IOException ioe) {
	
			_logger.warn("caught IOException when logger upload data to the filesystem. " + ioe.getMessage());
			throw new ServiceException(ioe);
		}
	}

	private void close(PrintWriter writer) throws IOException {
		writer.flush();
		writer.close();
		writer = null;	
	}
}
