package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.datatransfer.AwRequestImpl;

/**
 * The default AwRequestTransformer implementation. In the future if we need different types of these, a Factory should be created. 
 * 
 * @author selsky
 */
public class AwRequestCreatorImpl implements AwRequestCreator {
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public AwRequestCreatorImpl() {
		
	}
	
	/**
	 * Returns a new instance of AwRequestImpl. No mapping is performed from the HttpServletRequest.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		return new AwRequestImpl();		
	}

}
