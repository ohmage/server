package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.EmaVizQueryAwRequest;

/**
 * Transformer for creating an AwRequest for EMA visualization.
 * 
 * @author selsky
 */
public class EmaQueryAwRequestCreator implements AwRequestCreator {
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public EmaQueryAwRequestCreator() {
		
	}
	
	/**
	 *  Pulls the s (startDate) parameter, the e (endDate) parameter, and the u (userName) parameter out of the request and places
	 *  them in a new AwRequest.
	 *  Pulls the userName out of the HttpSession and places in in the AwRequest.
	 *  Strict validation of the data is performed within a controller.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		HttpSession session = request.getSession();
		User user = new UserImpl((User) session.getAttribute("user"));
		
		String startDate = request.getParameter("s");
		String endDate = request.getParameter("e");
		String userNameRequestParam = request.getParameter("u"); // researchers are allowed to pass an optional user name
		
		AwRequest awRequest = new EmaVizQueryAwRequest();
		awRequest.setUser(user);
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		
		return awRequest;
	}
}
