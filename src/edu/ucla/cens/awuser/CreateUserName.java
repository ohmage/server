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
	 * @throws IOException if the first argument represents a file that does not exist
	 * @throws IOException if there are any problems reading the input file
	 * @throws IllegalArgumentException if the input file is empty
	 * @throws IllegalArgumentException if the filename argument is missing from the command line
	 */
	public static void main(String args[]) throws IOException {
		
		if(args.length < 1) {
			throw new IllegalArgumentException("a filename of a file containing the wordlist is required");
		}
		
		String fileName = args[0];
		BufferedReader in = new BufferedReader(new FileReader(fileName)); // this will throw an IOException if the file does not exist
		List<String> words = new ArrayList<String>();
		
		String word = in.readLine();
		
		if(null == word) {
			throw new IllegalArgumentException("there are no words in the file");
		}
		
		while(null != word) { // Lazily read the whole thing into memory
                    	 	  // The default file contains 9189 words
			
			words.add(word);
			word = in.readLine();
			
		}
		
		in.close();
		
		int size = words.size();
		Random random = new Random();
		
		int indexA = Math.abs(random.nextInt() % size);
		int indexB = Math.abs(random.nextInt() % size);
		
		System.out.println("The new user name is: " + words.get(indexA) + "." + words.get(indexB));
		
	}	
}
