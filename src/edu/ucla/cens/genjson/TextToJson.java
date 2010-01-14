package edu.ucla.cens.genjson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility for converting Strings of JSON to JSON objects for pretty printing and debugging via the command line.
 * 
 * @author selsky
 */
public class TextToJson {

	/**
	 * Given an input file, attempt to convert its contents to pretty-printed JSON and push the JSON to System.out.
	 * 
	 * @throws JSONException if the string in the input file represents malformed JSON
	 * @throws IOException if the provided file name does not exist or cannot be read or any other IO-related problems occur
	 * @throws IllegalArgumentException if an invalid type is passed in as the first argument (only <code>object</code> and 
	 * <code>array</code> are accepted).
	 */
	public static void main(String args[]) throws JSONException, IOException {
		if(args.length < 2) {
			System.out.println("incorrect number of arguments");
			System.out.println(_helpText);
			System.exit(1);
		}
		
		if("help".equals(args[0])) {
			System.out.println(_helpText);
			System.exit(0);
		}
		
		String type = args[0];
		if(! ("array".equals(type) || "object".equals(type))) {
			throw new IllegalArgumentException("invalid json type argument. you provided: " + type);
		}
		
		String fileName = args[1];
		StringBuilder builder = new StringBuilder();
		BufferedReader in = null;
		Writer out = null;
		
		try {
		
			in = new BufferedReader(new FileReader(fileName));
			out = new BufferedWriter(new OutputStreamWriter(System.out));
			String line = in.readLine();
			
			while(null != line) { 
				builder.append(line);
				line = in.readLine();
			}
			
			if("array".equals(type)) {
				
				JSONArray jsonArray = new JSONArray(builder.toString());
				out.write(jsonArray.toString(4));
			
				
			} else if("object".equals(type)) {
				
				JSONObject jsonObject = new JSONObject(builder.toString());
				out.write(jsonObject.toString(4));
				
			}
			
			out.flush();
		} 
		
		finally {
			
			if(in != null) {
				in.close();
			}
		
			if(out != null) {
				out.close();
			}
			
		}
	}
	
	private static String _helpText = "Given a JSON type (object or array) and an input file name, attempt to convert the file's\n" +
			                          "contents to pretty-printed JSON and push the JSON to System.out.\n" +
			                          "  Usage:\n" +
			                          "      java edu.ucla.cens.genjson.TextToJson <array>|<object> fileName";
	
}
