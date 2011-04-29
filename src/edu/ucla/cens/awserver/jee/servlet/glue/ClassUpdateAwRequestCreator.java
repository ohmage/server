package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ClassUpdateAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Creates the internal request used to update a class.
 * 
 * @author John Jenkins
 */
public class ClassUpdateAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ClassUpdateAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassUpdateAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * 
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating a new internal request object for updating a class.");
		
		String token = request.getParameter(InputKeys.AUTH_TOKEN);
		String classUrn = request.getParameter(InputKeys.CLASS_URN);
		String name = request.getParameter(InputKeys.CLASS_NAME);
		String description = request.getParameter(InputKeys.DESCRIPTION);
		String userListAdd = request.getParameter(InputKeys.USER_LIST_ADD);
		String userListRemove = request.getParameter(InputKeys.USER_LIST_REMOVE);
		String privilegedUserListAdd = request.getParameter(InputKeys.PRIVILEGED_USER_LIST_ADD);
		
		ClassUpdateAwRequest awRequest = new ClassUpdateAwRequest(classUrn, name, description, userListAdd, userListRemove, privilegedUserListAdd);
		awRequest.setUserToken(token);
		
		return awRequest;
	}

}
