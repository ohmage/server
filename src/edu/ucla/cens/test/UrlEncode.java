package edu.ucla.cens.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * @author selsky
 */
public class UrlEncode {
	
	/**
	 * Given the name of a Java properties (XML) file and a key for a particular entry, URL encode (UTF-8) that entry.
	 */
	public static void main(String args[]) throws UnsupportedEncodingException, IOException {
		InputStream is = new FileInputStream(args[0]); 
		Properties props = new Properties();
		props.loadFromXML(is);
		
		System.out.println(URLEncoder.encode(((String) props.get(args[1])).trim(), "UTF-8"));
	}
}


