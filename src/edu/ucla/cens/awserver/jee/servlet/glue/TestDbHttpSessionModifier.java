package edu.ucla.cens.awserver.jee.servlet.glue;

import java.util.List;

import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Handles copying data for the test db function from an AwRequest into an HttpServletRequest for rendering by a JSP.
 * 
 * @author selsky
 */
public class TestDbHttpSessionModifier implements HttpSessionModifier {

	/**
	 * Places the <code>results</code> List property from the AwRequest into the HttpServletRequest with the name <code>
	 * testResultList</code>.  
	 */
	public void modifySession(AwRequest awRequest, HttpSession httpSession) {
		List<?> results = (List<?>) awRequest.getPayload().get("results");
	    httpSession.setAttribute("testResultList", results);
	}

}
