package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.EmaVizQueryAwRequest;

/**
 * Transformer for creating an AwRequest for EMA visualization.
 * 
 * @author selsky
 */
public class EmaVizAwRequestCreator implements AwRequestCreator {
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public EmaVizAwRequestCreator() {
		
	}
	
	/**
	 *  Pulls the s (startDate) parameter and the e (endDate) parameter out of the request and places them in a new AwRequest.
	 *  Pulls the userName out of the HttpSession and places in in the AwRequest.
	 *  Validation of the data is performed within a controller.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String userName = (String) session.getAttribute("userName");
		UserImpl user = new UserImpl();
		user.setUserName(userName);
		
		String startDate = request.getParameter("s");
		String endDate = request.getParameter("e");
		
		AwRequest awRequest = new EmaVizQueryAwRequest();
		awRequest.setUser(user);
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		
		return awRequest;
	}
}
