package edu.ucla.cens.genjson;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Procedural class for generating test JSON messages.
 * 
 * @author selsky
 */
public class GenerateJson {

	/**
	 * Given a number of messages, a message type and a file name, generate messages and place them in the output file.
	 * 
	 * TODO add pretty-print and debug args
	 * 
	 * @throws IOException if any errors occur writing the output
	 */
	public static void main(String args[]) throws IOException, JSONException {
		if(args.length < 3) {
			System.out.println("Incorrect number of arguments.\n Run with help as the first argument to get a usage example.");
			System.exit(1);
		}
		
		if("help".equals(args[0])) {
			System.out.println(helpText);
			System.exit(0);
		}
		
		int numberOfMessages = 0;
		String messageType = null;
		String fileName = null;
		
		try {
			
			numberOfMessages = Integer.parseInt(args[0]);
			
		} catch (NumberFormatException nfe) {
			
			throw new IllegalArgumentException("The first argument (if it is not the string \"help\"), must be an integer", nfe);
		}
		
		messageType = args[1];
		
		if(! validMessageType(messageType)) {
			
			throw new IllegalArgumentException("incorrect/unknown message type: " + messageType); 
		}
		
		fileName = args[2];
		
		JsonMessageCreator jsonMessageCreator = JsonMessageCreatorFactory.make(messageType);
		JSONArray jsonArray = jsonMessageCreator.createMessage(numberOfMessages);
		
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		
		System.out.println(jsonArray.toString(4));
		
		String jsonString = jsonArray.toString();
		
		int strLen = jsonString.length();
		
		int start = 0;
		int chunkSize = 1024;
		
		while(start < strLen) {
			
			int amountLeft = strLen - start;
			int amountToWrite = chunkSize > amountLeft ? amountLeft : chunkSize; 
			
			out.write(jsonString, start, amountToWrite);
			start += chunkSize;
		}
		
		out.write("\n");
		out.flush();
		out.close();
	}
	
	private static boolean validMessageType(String messageType) {
		
		return "mobility:mode_only".equals(messageType) 
			|| "mobility:mode_features".equals(messageType)
			|| "prompt:0".equals(messageType)
			|| "prompt:1".equals(messageType)
			|| "prompt:2".equals(messageType)
		    || "prompt:3".equals(messageType);
	}
	
	private static String helpText = "Given a number of messages, a message type and a file name, generate JSON messages and\n" +
			                         "place them in the output file.\n" +
			                         "Usage:\n" +
			                         "    java edu.ucla.cens.genjson.GenerateJson ";
	
}
