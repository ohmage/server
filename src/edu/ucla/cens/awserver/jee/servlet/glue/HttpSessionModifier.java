package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * A post-processor for mapping data from an AwRequest into an HttpRequest. Helps to decouple the AW world from the Java EE world.
 * 
 * @author selsky
 */
public interface HttpSessionModifier {

	public void modifySession(AwRequest awRequest, HttpSession httpSession);
	
}
