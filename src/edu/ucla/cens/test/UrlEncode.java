package edu.ucla.cens.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Set;

/**
 * @author selsky
 */
public class UrlEncode {
	
	/**
	 * Given the name of a Java properties (XML) file and a key for a particular entry, URL encode (UTF-8) that entry. If no 
	 * key is given, every entry in the file is URL encoded. The encoded URLs are dumped to System.out.
	 * 
	 * @throws IOException if the input file cannot be found or problems occur when attempting to read it
	 */
	public static void main(String args[]) throws UnsupportedEncodingException, IOException {
		InputStream is = new FileInputStream(args[0]); 
		Properties props = new Properties();
		props.loadFromXML(is);
		
		if(args.length == 1) {
			Set<Object> keySet = props.keySet();
			
			for(Object key : keySet) {
				System.out.println(key + " " + URLEncoder.encode(((String) props.get(key)).trim(), "UTF-8"));
				System.out.println();
			}
		} else {
			if(null == props.get(args[1])) {
				System.out.println("nothing to do. no entry found for key: " + args[1]);
			} else {
				System.out.println(URLEncoder.encode(((String) props.get(args[1])).trim(), "UTF-8"));
			}
		}
	}
}


