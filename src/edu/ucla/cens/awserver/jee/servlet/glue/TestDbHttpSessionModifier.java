package edu.ucla.cens.awserver.jee.servlet.glue;

import java.util.List;

import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Handles copying data from an AwRequest into the HttpSession for the test db function.
 * 
 * @author selsky
 */
public class TestDbHttpSessionModifier implements HttpSessionModifier {

	/**
	 * Places the <code>results</code> List property from the AwRequest into the HttpSession with the name <code>
	 * testResultList</code>.  
	 */
	public void modifySession(AwRequest awRequest, HttpSession httpSession) {
		List<?> results = (List<?>) awRequest.getAttribute("results");
	    httpSession.setAttribute("testResultList", results);
	}

}
