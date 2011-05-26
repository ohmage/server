package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that the user has all of a set of roles in each of the classes in a
 * class list.
 * 
 * @author John Jenkins
 */
public class UserHasRolesInAllClassesInListService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserHasRolesInAllClassesInListService.class);
	
	private List<String> _roles;
	private boolean _required;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					one of the roles in one of the classes.
	 * 
	 * @param roles The roles for the user to have in each of the classes.
	 * 
	 * @param required Whether or not this service is required.
	 */
	public UserHasRolesInAllClassesInListService(AwRequestAnnotator annotator, List<String> roles, boolean required) {
		super(annotator);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles cannot be null or empty.");
		}
		
		_roles = roles;
		_required = required;
	}
	
	/**
	 * Validates that the user has all of the specified roles in all of the
	 * classes in the class list. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String classIdList;
		try {
			classIdList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new IllegalArgumentException("Missing required key: " + InputKeys.CLASS_URN_LIST);
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the user has the specified roles for all the classes in the class list.");
		
		String[] classIdArray = classIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classIdArray.length; i++) {
			for(String role : _roles) {
				if(! awRequest.getUser().hasRoleInClass(classIdArray[i], role)) {
					getAnnotator().annotate(awRequest, "The user doesn't have the role '" + role + "' in class: " + classIdArray[i]);
					awRequest.setFailedRequest(true);
					return;
				}
			}
		}
	}
}