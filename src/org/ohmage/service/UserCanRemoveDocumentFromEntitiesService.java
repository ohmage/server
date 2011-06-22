package org.ohmage.service;

import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.EntityAndRole;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Verifies that the requester is either an owner of this document or that they
 * no entity that they are trying to dissassociate is.
 * 
 * @author John Jenkins
 */
public class UserCanRemoveDocumentFromEntitiesService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(UserCanRemoveDocumentFromEntitiesService.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator with which to respond if the user doesn't
	 * 					have sufficient privileges to dissassociate an entity
	 * 					with the document.
	 * 
	 * @param dao The DAO to use to get a list of entities and their role with
	 * 			  this document.
	 * 
	 * @param key The key to use to get the entity list String from the 
	 * 			  request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public UserCanRemoveDocumentFromEntitiesService(AwRequestAnnotator annotator, Dao dao, String key, boolean required) {
		super(dao, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		else if(dao == null) {
			throw new IllegalArgumentException("The DAO to get the campaigns and document roles cannot be null.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * First, checks if the required parameter exists. If not and it's not
	 * required, it returns. If not and it is required, it returns an error.
	 * Then, it checks if the requester has the highest permission (owner). If
	 * so, it returns. If not, it gets all the entities and their roles based
	 * on the DAO that are associated with this document and checks that none
	 * of them have the role owner. If the requester can edit the document, 
	 * then they must have at least the writer role. Therefore, the only role
	 * that an entity can have that the requester cannot remove is the role of
	 * owner.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the entity list and as a String.
		String entityListString;
		try {
			entityListString = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new IllegalArgumentException("Missing required key: " + _key);
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the user isn't removing entity's associations with documents where the entity is more privileged than the user.");
		
		String requesterDocumentRole = awRequest.getUser().getDocumentRole();
		if(DocumentRoleCache.ROLE_OWNER.equals(requesterDocumentRole)) {
			// If the requester is an owner then quit.
			return;
		}
		
		_logger.debug("requesterDocumentRole: " + requesterDocumentRole);
		
		// Get the entities' IDs and the entities' document roles.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		List<?> campaignAndRoleList = awRequest.getResultList();
		
		// For each of the entities, check that their role is not owner as that
		// is the only document permission that an entity can have that the
		// requester doesn't already have.
		String[] entityArray = entityListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < entityArray.length; i++) {
			ListIterator<?> campaignAndRoleListIter = campaignAndRoleList.listIterator();
			
			// For this entity, search through all entity-roles to find this
			// entity's role.
			while(campaignAndRoleListIter.hasNext()) {
				EntityAndRole currCampaignAndRole = (EntityAndRole) campaignAndRoleListIter.next();
				
				// If the entity matches ensure that the role isn't owner. If
				// it is, annotate the issue. If not, break this inner loop.
				if(currCampaignAndRole.getEntityId().equals(entityArray[i])) {
					if(DocumentRoleCache.ROLE_OWNER.equals(currCampaignAndRole.getRole())) {
						getAnnotator().annotate(awRequest, "The user has insufficient permissions to remove this entity from the document.");
						awRequest.setFailedRequest(true);
						return;
					}
					else {
						break;
					}
				}
			}
		}
	}
}