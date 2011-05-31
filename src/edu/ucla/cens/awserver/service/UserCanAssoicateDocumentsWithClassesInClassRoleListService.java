package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that the user has sufficient permissions in each of the classes in
 * the class-role list to associate a document with that class.
 * 
 * @author John Jenkins
 */
public class UserCanAssoicateDocumentsWithClassesInClassRoleListService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserCanAssoicateDocumentsWithClassesInClassRoleListService.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					sufficient permissions to associate a document with 
	 * 					some class in the list.
	 * 
	 * @param required Whether or not this validation is required.
	 * 
	 * @throws IllegalArgumentException Thrown if there is a problem with any
	 * 									of the parameters that prevent this
	 * 									service from being constructed.
	 */
	public UserCanAssoicateDocumentsWithClassesInClassRoleListService(AwRequestAnnotator annotator, String key, boolean required) throws IllegalArgumentException {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}
	
	/**
	 * Ensures that the list of classes and roles exist if required and then
	 * checks that the user has sufficient permissions in each of the classes
	 * to associate a document with it.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of classes and roles.
		String classRoleList;
		try {
			classRoleList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key '" + _key + "' in toProcess map.");
			}
			else {
				return;
			}
		}
		
		_logger.info("Checking that the user is allowed to associate documents with each of the classes.");
		
		// Check that the user is allowed to create documents in each of those
		// classes.
		String[] classRoleArray = classRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classRoleArray.length; i++) {
			String[] classRole = classRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			if((! awRequest.getUser().isPrivilegedInClass(classRole[0])) &&
			   (! awRequest.getUser().isRestrictedInClass(classRole[0]))) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "The user has insufficient privileges to create a new document in the class: " + classRole[0]);
			}
		}
	}
}