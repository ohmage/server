package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that a user is not attempting to add another entity with higher 
 * permissions than their own.
 * 
 * @author John Jenkins
 */
public class UserCanAddDocumentRolesService extends AbstractAnnotatingService {
	private static final Logger _logger = Logger.getLogger(UserCanAddDocumentRolesService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with should the user be 
	 * 					attempting to add another entity with higher 
	 * 					permissions than their own.
	 */
	public UserCanAddDocumentRolesService(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * Gets the highest role that the user is attempting to associate with any
	 * other entity and then checks that value against the users own role to 
	 * ensure that the user is not adding an entity with higher permissions 
	 * than they have.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that the user is not attempting to add another entity with more permissions than they have themselves.");
		
		String highestRoleBeingAdded = "";
		
		// Get the highest role from campaign-role add pairs.
		String campaignRoleListAdd;
		try {
			campaignRoleListAdd = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_ROLE_LIST_ADD);
			
			if(! "".equals(campaignRoleListAdd)) {
				String[] campaignsAndRoles = campaignRoleListAdd.split(InputKeys.LIST_ITEM_SEPARATOR);
				
				for(int i = 0; i < campaignsAndRoles.length; i++) {
					String[] campaignAndRole = campaignsAndRoles[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);

					highestRoleBeingAdded = greaterRole(highestRoleBeingAdded, campaignAndRole[1]);
				}
			}
		}
		catch(IllegalArgumentException e) {
			// The user is not attempting to add any new campaign permissions.
		}
		
		// Get the highest role from class-role add pairs.
		String classRoleListAdd;
		try {
			classRoleListAdd = (String) awRequest.getToProcessValue(InputKeys.CLASS_ROLE_LIST_ADD);
			
			if(! "".equals(classRoleListAdd)) {
				String[] classesAndRoles = classRoleListAdd.split(InputKeys.LIST_ITEM_SEPARATOR);
				
				for(int i = 0; i < classesAndRoles.length; i++) {
					String[] classAndRole = classesAndRoles[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
					
					highestRoleBeingAdded = greaterRole(highestRoleBeingAdded, classAndRole[1]);
				}
			}
		}
		catch(IllegalArgumentException e) {
			// The user is not attempting to add any new campaign permissions.
		}

		// Get the highest role from user-role add pairs.
		String userRoleListAdd;
		try {
			userRoleListAdd = (String) awRequest.getToProcessValue(InputKeys.USER_ROLE_LIST_ADD);
			
			if(! "".equals(userRoleListAdd)) {
				String[] usersAndRoles = userRoleListAdd.split(InputKeys.LIST_ITEM_SEPARATOR);
				
				for(int i = 0; i < usersAndRoles.length; i++) {
					String[] userAndRole = usersAndRoles[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
					
					highestRoleBeingAdded = greaterRole(highestRoleBeingAdded, userAndRole[1]);
				}
			}
		}
		catch(IllegalArgumentException e) {
			// The user is not attempting to add any new campaign permissions.
		}
		
		// If the highest role being added is non-existant then none of the 
		// role lists were non-empty, so we can just quit.
		if("".equals(highestRoleBeingAdded)) {
			return;
		}
		
		String documentRole = awRequest.getUser().getDocumentRole();
		if(DocumentRoleCache.ROLE_OWNER.equals(documentRole)) {
			// If they own it they can do anything.
			return;
		}
		else if(DocumentRoleCache.ROLE_OWNER.equals(highestRoleBeingAdded)) {
			getAnnotator().annotate(awRequest, "The requester is only a writer, but they are trying to allocate ownership permissions.");
			awRequest.setFailedRequest(true);
		}
	}
	
	/**
	 * Bases the roles off of the DocumentRoleCache constants and returns the
	 * "greater" role based on the two input values.
	 * 
	 * @param firstRole The first role to check against.
	 * 
	 * @param secondRole The second role to check against.
	 * 
	 * @return The "greater" of the two roles. "Greater" here is being defined 
	 * 	       as the most privileged.
	 */
	private String greaterRole(String firstRole, String secondRole) {
		if(DocumentRoleCache.ROLE_OWNER.equals(firstRole) || DocumentRoleCache.ROLE_OWNER.equals(secondRole)) {
			return DocumentRoleCache.ROLE_OWNER;
		}
		else if(DocumentRoleCache.ROLE_WRITER.equals(firstRole) || DocumentRoleCache.ROLE_WRITER.equals(secondRole)) {
			return DocumentRoleCache.ROLE_WRITER;
		}
		else if(DocumentRoleCache.ROLE_READER.equals(firstRole) || DocumentRoleCache.ROLE_READER.equals(secondRole)) {
			return DocumentRoleCache.ROLE_READER;
		}
		else {
			return "";
		}
	}
}