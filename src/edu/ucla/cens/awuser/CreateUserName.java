package edu.ucla.cens.awuser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class to create user names. 
 * 
 * @author selsky
 */
public class CreateUserName {
	
	/**
	 * Given a file name containing a newline-separated list of words, return a dot-separated concatenation of two randomly chosen
	 * words from the file. 
	 * 
	 * @throws IOException if the first argument represents a file that does not exist or if there are any problems reading the
	 * input file 
	 * @throws IllegalArgumentException if the input file is empty
	 * @throws IllegalArgumentException if the filename argument is missing from the command line
	 */
	public static void main(String args[]) throws IOException {
        if(args.length == 0 || args[0].equals("help")) {
        	System.out.println(_helpText);
        	System.exit(0);
        }
		
		String fileName = args[0];
		BufferedReader in = null; 
		List<String> words = new ArrayList<String>();
		
		try {
			in = new BufferedReader(new FileReader(fileName));
			String word = in.readLine();
			
			if(null == word) {
				throw new IllegalArgumentException("there are no words in the file");
			} else if(word.length() < 4 || word.length() > 6) {
				throw new IllegalArgumentException("invalid dictionary file. The first line is : " + word);
			}
			
			while(null != word) { // Lazily read the whole thing into memory
	                    	 	  // The default file contains 9189 words
				
				words.add(word);
				word = in.readLine();
				
			}
			
			int size = words.size();
			Random random = new Random();
			
			int indexA = Math.abs(random.nextInt() % size);
			int indexB = Math.abs(random.nextInt() % size);
			
			System.out.println("The new user name is: " + words.get(indexA) + "." + words.get(indexB));
		}
		finally {
			
			if(in != null) {
			
				in.close();
				
			}
		}
	}	
	
	private static String _helpText = "Given a file name containing a newline-separated list of words, return a\n" +
	                            	  "dot-separated concatenation of two randomly chosen words from the file.\n\n" +
	                            	  "Usage:\n\n" +
	                            	  "    java -classpath CLASS_DIR edu.ucla.cens.awuser.CreateUserName [help] fileName\n" +
	                            	  "E.g. java -classpath classes edu.ucla.cens.awuser.CreateUserName data/username-words.txt\n";
}
