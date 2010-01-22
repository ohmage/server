package edu.ucla.cens.awserver.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Service for augmenting an upload request with details helpful for logging.
 * 
 * @author selsky
 */
public class JsonMessageSourceInfoService implements Service {
	private static Logger _logger = Logger.getLogger("uploadLogger");
	private static Logger _logger2 = Logger.getLogger(JsonMessageSourceInfoService.class);
	
	public void execute(AwRequest awRequest) {
		// create a file name for saving the user's uploaded data
		// need both input and output file names using the same filename prefix (so the error reporting can also be logged and
		// joined to the input)
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String fileName = awRequest.getUser().getUserName() + "-" + sdf.format(new Date()) + "-upload.json";
		String catalinaBase = System.getProperty("catalina.base"); // need a system prop called upload-logging-directory or something like that
		                                                           // or just add a setter here
		_logger.info(catalinaBase);
		
		try {
		
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(catalinaBase + "/uploads/" + fileName)))); // "$CATALINA_BASE/uploads/"" 
			printWriter.write("hello");
			printWriter.close();
			
		} catch(IOException ioe) {
			throw new IllegalStateException(ioe);
		}
		
	}

}
