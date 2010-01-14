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
	 * @throws IOException if any errors occur writing the output
	 * @throws JSONException if any errors occur in creating and processing JSON (most likely due to a logical error)
	 */
	public static void main(String args[]) throws IOException, JSONException {
		if(args.length < 3) {
			System.out.println("Error: incorrect number of arguments.");
			System.out.println(_helpText);
			System.exit(1);
		}
		
		if("help".equals(args[0])) {
			System.out.println(_helpText);
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
	
	private static String _helpText = "\nGiven a number of messages, a message type and a file name, generate JSON messages and\n" +
			                         "place them in the output file.\n" +
			                         "The allowed message types are: mobility:mode_only, mobility:mode_features, prompt:0,\n" +
			                         "prompt:1, prompt:2, prompt:3.\n\n" +
			                         "Usage:\n" +
			                         "    java edu.ucla.cens.genjson.GenerateJson 10 mobility:mode_only out.txt\n\n" +
			                         "If you would like to pretty-print the JSON output, check out edu.ucla.cens.genjson.TextToJson.";
	
}
