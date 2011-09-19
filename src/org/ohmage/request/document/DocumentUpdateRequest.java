package org.ohmage.request.document;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignDocumentServices;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassDocumentServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.DocumentServices;
import org.ohmage.service.UserCampaignDocumentServices;
import org.ohmage.service.UserClassDocumentServices;
import org.ohmage.service.UserDocumentServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.CampaignDocumentValidators;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassDocumentValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.DocumentValidators;
import org.ohmage.validator.UserDocumentValidators;
import org.ohmage.validator.UserValidators;

/**
 * <p>Updates an existing document. To update a document the user must have the
 * owner or writer role through a direct relationship with the document or
 * through a class or campaign. Also, if the document is shared with a campaign
 * and the user is a supervisor in that campaign or it is shared with a class
 * and the user is privileged in that class, they will be allowed to edit the
 * document.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_ID}</td>
 *     <td>The unique identifier for the document to be updated.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT}</td>
 *     <td>New contents for the document.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_NAME}</td>
 *     <td>A new name for the document.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>A new description for the document.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The new privacy state of the document.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_ROLE_LIST_ADD}</td>
 *     <td>A list of campaign ID and document role pairs. The pairs should be
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and each
 *       campaign ID should be separated from its associated document role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_LIST_REMOVE}</td>
 *     <td>A list of campaign IDs for campaigns that should no longer be 
 *       associated with the document. The campaign IDs should be separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_ROLE_LIST_ADD}</td>
 *     <td>A list of class ID and document role pairs. The pairs should be
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and each
 *       class ID should be separated from its associated document role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_LIST_REMOVE}</td>
 *     <td>A list of class IDs for classes that should no longer be 
 *       associated with the document. The class IDs should be separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_ADD}</td>
 *     <td>A list of username and document role pairs. The pairs should be
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and each
 *       username should be separated from its associated document role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>A list of usernames for users that should no longer be associated 
 *       with the document. The usernames should be separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class DocumentUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentUpdateRequest.class);
	
	private final String documentId;
	
	private final byte[] newContents;
	
	private final String newName;
	private final String newDescription;
	private final DocumentPrivacyStateCache.PrivacyState newPrivacyState;
	
	private final Map<String, DocumentRoleCache.Role> campaignAndRolesToAdd;
	private final List<String> campaignsToRemove;
	
	private final Map<String, DocumentRoleCache.Role> classAndRolesToAdd;
	private final List<String> classesToRemove;
	
	private final Map<String, DocumentRoleCache.Role> userAndRolesToAdd;
	private final List<String> usersToRemove;
	
	/**
	 * Creates a new document update request from the information in the
	 * HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest containing the specific 
	 * 					  information.
	 */
	public DocumentUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		String tDocumentId = null;
		
		byte[] tNewContents = null;
		
		String tNewName = null;
		String tNewDescription = null;
		DocumentPrivacyStateCache.PrivacyState tNewPrivacyState = null;
		
		Map<String, DocumentRoleCache.Role> tCampaignAndRolesToAdd = null;
		List<String> tCampaignsToRemove = null;
		
		Map<String, DocumentRoleCache.Role> tClassAndRolesToAdd = null;
		List<String> tClassesToRemove = null;
		
		Map<String, DocumentRoleCache.Role> tUserAndRolesToAdd = null;
		List<String> tUsersToRemove = null;
		
		try {
			tDocumentId = DocumentValidators.validateDocumentId(this, httpRequest.getParameter(InputKeys.DOCUMENT_ID));
			if(tDocumentId == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "The required document ID is missing.");
				throw new ValidationException("The required document ID is missing.");
			}
			else if(httpRequest.getParameterValues(InputKeys.DOCUMENT_ID).length > 1) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "Multiple document ID parameters were given.");
				throw new ValidationException("Multiple document ID parameters were given.");
			}
			
			tNewContents = getMultipartValue(httpRequest, InputKeys.DOCUMENT);
			
			tNewName = DocumentValidators.validateName(this, httpRequest.getParameter(InputKeys.DOCUMENT_NAME));
			if((tNewName != null) && (httpRequest.getParameterValues(InputKeys.DOCUMENT_NAME).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_NAME, "Mutiple document name parameters were given.");
				throw new ValidationException("Mutiple document name parameters were given.");
			}
			
			tNewDescription = DocumentValidators.validateDescription(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
			if((tNewDescription != null) && (httpRequest.getParameterValues(InputKeys.DESCRIPTION).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_DESCRIPTION, "Mutiple description parameters were given.");
				throw new ValidationException("Mutiple description parameters were given.");
			}
			
			tNewPrivacyState = DocumentValidators.validatePrivacyState(this, httpRequest.getParameter(InputKeys.PRIVACY_STATE));
			if((tNewPrivacyState != null) && (httpRequest.getParameterValues(InputKeys.PRIVACY_STATE).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_PRIVACY_STATE, "Mutiple privacy state parameters were given.");
				throw new ValidationException("Mutiple privacy state parameters were given.");
			}
			
			tCampaignAndRolesToAdd = CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(this, httpRequest.getParameter(InputKeys.CAMPAIGN_ROLE_LIST_ADD));
			if((tCampaignAndRolesToAdd != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_ROLE_LIST_ADD).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Mutiple campaign ID, document role list parameters were given.");
				throw new ValidationException("Mutiple campaign ID, document role list parameters were given.");
			}
			
			tCampaignsToRemove = CampaignValidators.validateCampaignIds(this, httpRequest.getParameter(InputKeys.CAMPAIGN_LIST_REMOVE));
			if((tCampaignsToRemove != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_LIST_REMOVE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Mutiple campaign ID list parameters were given.");
				throw new ValidationException("Mutiple campaign ID list parameters were given.");
			}
			
			tClassAndRolesToAdd = ClassDocumentValidators.validateClassIdAndDocumentRoleList(this, httpRequest.getParameter(InputKeys.CLASS_ROLE_LIST_ADD));
			if((tClassAndRolesToAdd != null) && (httpRequest.getParameterValues(InputKeys.CLASS_ROLE_LIST_ADD).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Mutiple class ID, document role list parameters were given.");
				throw new ValidationException("Mutiple class ID, document role list parameters were given.");
			}
			
			tClassesToRemove = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_LIST_REMOVE));
			if((tClassesToRemove != null) && (httpRequest.getParameterValues(InputKeys.CLASS_LIST_REMOVE).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Mutiple class ID list parameters were given.");
				throw new ValidationException("Mutiple class ID list parameters were given.");
			}
			
			tUserAndRolesToAdd = UserDocumentValidators.validateUsernameAndDocumentRoleList(this, httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD));
			if((tUserAndRolesToAdd != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_ADD).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Mutiple username, document role list parameters were given.");
				throw new ValidationException("Mutiple username, document role list parameters were given.");
			}
			
			tUsersToRemove = UserValidators.validateUsernames(this, httpRequest.getParameter(InputKeys.USER_LIST_REMOVE));
			if((tUsersToRemove != null) && (httpRequest.getParameterValues(InputKeys.USER_LIST_REMOVE).length > 1)) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Mutiple username list parameters were given.");
				throw new ValidationException("Mutiple username list parameters were given.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		documentId = tDocumentId;
		
		newContents = tNewContents;
		newName = tNewName;
		newDescription = tNewDescription;
		newPrivacyState = tNewPrivacyState;
		
		campaignAndRolesToAdd = tCampaignAndRolesToAdd;
		campaignsToRemove = tCampaignsToRemove;
		
		classAndRolesToAdd = tClassAndRolesToAdd;
		classesToRemove = tClassesToRemove;
		
		userAndRolesToAdd = tUserAndRolesToAdd;
		usersToRemove = tUsersToRemove;
		
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the document update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the document exists.");
			DocumentServices.ensureDocumentExistence(this, documentId);
			
			LOGGER.info("Verifying that the user can modify the document.");
			UserDocumentServices.userCanModifyDocument(this, getUser().getUsername(), documentId);
			
			LOGGER.info("Getting the user's highest role for the document.");
			DocumentRoleCache.Role highestRole = UserDocumentServices.getHighestDocumentRoleForUserForDocument(this, getUser().getUsername(), documentId);
			
			if(campaignAndRolesToAdd != null) {
				LOGGER.info("Verifying that the campaigns in the campaign-role list exist.");
				CampaignServices.checkCampaignsExistence(this, campaignAndRolesToAdd.keySet(), true);
				
				LOGGER.info("Verifying that the user can associate the document with the campaigns in the campaign-role list.");
				UserCampaignDocumentServices.userCanAssociateDocumentsWithCampaigns(this, getUser().getUsername(), campaignAndRolesToAdd.keySet());
				
				LOGGER.info("Verifying that the user is not attempting to give more permissions to a campaign than they have.");
				DocumentServices.ensureRoleNotLessThanRoles(this, highestRole, campaignAndRolesToAdd.values());
			}
			
			if(campaignsToRemove != null) {
				LOGGER.info("Verifying that the campaigns in the campaign list exist.");
				CampaignServices.checkCampaignsExistence(this, campaignsToRemove, true);
				
				LOGGER.info("Verifying that the user has enough permissions in the campaigns to disassociate them from the document.");
				UserCampaignDocumentServices.userCanDisassociateDocumentsFromCampaigns(this, getUser().getUsername(), campaignsToRemove);
				
				LOGGER.info("Verifying that the user is not attempting to revoke more permissions from campaigns than they have.");
				CampaignDocumentServices.ensureRoleHighEnoughToDisassociateDocumentFromCampaigns(this, highestRole, campaignsToRemove, documentId);
			}
			
			if(classAndRolesToAdd != null) {
				LOGGER.info("Verifying that the classes in the class-role list exist.");
				ClassServices.checkClassesExistence(this, classAndRolesToAdd.keySet(), true);
				
				LOGGER.info("Verifying that the user can associate the document with the classes in the class-role list.");
				UserClassDocumentServices.userCanAssociateDocumentsWithClasses(this, getUser().getUsername(), classAndRolesToAdd.keySet());
				
				LOGGER.info("Verifying that the user is not attempting to give more permissions to a class than they have.");
				DocumentServices.ensureRoleNotLessThanRoles(this, highestRole, classAndRolesToAdd.values());
			}
			
			if(classesToRemove != null) {
				LOGGER.info("Verifying that the classes in the class list exist.");
				ClassServices.checkClassesExistence(this, classesToRemove, true);
				
				LOGGER.info("Verifying that the user has enough permissions in the classes to disassociate them from the document.");
				UserClassDocumentServices.userCanDisassociateDocumentsWithClasses(this, getUser().getUsername(), classesToRemove);
				
				LOGGER.info("Verifying that the user is not attempting to revoke more permissions from classes than they have.");
				ClassDocumentServices.ensureRoleHighEnoughToDisassociateDocumentFromClasses(this, highestRole, classesToRemove, documentId);
			}
			
			if(userAndRolesToAdd != null) {
				LOGGER.info("Verifying that the users in the user-role list exist.");
				UserServices.verifyUsersExist(this, userAndRolesToAdd.keySet(), true);
				
				LOGGER.info("Verifying that the user is not attempting to give more permissions to a user than they have.");
				DocumentServices.ensureRoleNotLessThanRoles(this, highestRole, userAndRolesToAdd.values());
			}
			
			if(usersToRemove != null) {
				LOGGER.info("Verifying that the user is not attempting to revoke more permissions from users than they have.");
				UserDocumentServices.ensureRoleHighEnoughToDisassociateDocumentFromOtherUsers(this, highestRole, usersToRemove, documentId);
			}
			
			LOGGER.info("Updating the document.");
			DocumentServices.updateDocument(this, documentId, newContents, newName, newDescription, newPrivacyState, 
					campaignAndRolesToAdd, campaignsToRemove, classAndRolesToAdd, classesToRemove, userAndRolesToAdd, usersToRemove);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user with a either success or failure and a failure 
	 * message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}