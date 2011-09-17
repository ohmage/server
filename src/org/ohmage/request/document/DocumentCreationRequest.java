package org.ohmage.request.document;

import java.util.ArrayList;
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
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.DocumentServices;
import org.ohmage.service.UserCampaignDocumentServices;
import org.ohmage.service.UserClassDocumentServices;
import org.ohmage.validator.CampaignDocumentValidators;
import org.ohmage.validator.ClassDocumentValidators;
import org.ohmage.validator.DocumentValidators;

/**
 * <p>Creates a document creation request. The document must be associated with
 * at least one campaign or class upon creation, therefore either
 * {@value org.ohmage.request.InputKeys#DOCUMENT_CAMPAIGN_ROLE_LIST} or
 * {@value org.ohmage.request.InputKeys#DOCUMENT_CLASS_ROLE_LIST} must be 
 * present.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT}</td>
 *     <td>The contents of the new document.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_NAME}</td>
 *     <td>The name of the new document including its extension.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The initial privacy state of the document.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_CAMPAIGN_ROLE_LIST}</td>
 *     <td>A list of campaign ID and document role pairs. The pairs should be
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and each
 *       campaign ID should be separated from its associated document role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>Either this or 
 *       {@value org.ohmage.request.InputKeys#DOCUMENT_CLASS_ROLE_LIST} or both
 *       must be present.</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_CLASS_ROLE_LIST}</td>
 *     <td>A list of class ID and document role pairs. The pairs should be
 *       separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and each
 *       class ID should be separated from its associated document role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>Either this or 
 *       {@value org.ohmage.request.InputKeys#DOCUMENT_CAMPAIGN_ROLE_LIST} or
 *       both must be present.</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of the class.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class DocumentCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentCreationRequest.class);
	
	private static final String KEY_DOCUMENT_ID = "document_id";
	
	private final byte[] document;
	
	private final String name;
	private final String description;
	private final DocumentPrivacyStateCache.PrivacyState privacyState;

	private final Map<String, DocumentRoleCache.Role> campaignRoleMap;
	private final Map<String, DocumentRoleCache.Role> classRoleMap;
	
	private String documentId;
	
	/**
	 * Creates a new document creation request from the information in the
	 * HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest containing the specific 
	 * 					  information.
	 */
	public DocumentCreationRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a new document creation request.");
		
		byte[] tempDocument = null;
		String tempName = null;
		DocumentPrivacyStateCache.PrivacyState tempPrivacyState = null;
		String tempDescription = null;
		Map<String, DocumentRoleCache.Role> tempCampaignRoleList = null;
		Map<String, DocumentRoleCache.Role> tempClassRoleList = null;
		
		try {
			tempDocument = getMultipartValue(httpRequest, InputKeys.DOCUMENT);
			if(tempDocument == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_CONTENTS, "The document's contents are missing: " + InputKeys.DOCUMENT);
				throw new ValidationException("The document's contents were missing.");
			}
			
			tempName = DocumentValidators.validateName(this, httpRequest.getParameter(InputKeys.DOCUMENT_NAME));
			if(tempName == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_NAME, "The document's name is missing: " + InputKeys.DOCUMENT_NAME);
				throw new ValidationException("The document's name is missing: " + InputKeys.DOCUMENT_NAME);
			}
			else if(httpRequest.getParameterValues(InputKeys.DOCUMENT_NAME).length > 1) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_NAME, "Multiple document names were found.");
				throw new ValidationException("The document's name is missing.");
			}
			
			tempPrivacyState = DocumentValidators.validatePrivacyState(this, httpRequest.getParameter(InputKeys.PRIVACY_STATE));
			if(tempPrivacyState == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_PRIVACY_STATE, "The document's privacy state is missing: " + InputKeys.PRIVACY_STATE);
				throw new ValidationException("The document's privacy state is missing: " + InputKeys.PRIVACY_STATE);
			}
			else if(httpRequest.getParameterValues(InputKeys.PRIVACY_STATE).length > 1) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_PRIVACY_STATE, "Multiple privacy state parameters were found.");
				throw new ValidationException("Multiple privacy state parameters were found.");
			}
			
			tempCampaignRoleList = CampaignDocumentValidators.validateCampaignIdAndDocumentRoleList(this, httpRequest.getParameter(InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST));
			if((tempCampaignRoleList != null) && (httpRequest.getParameterValues(InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "Multiple document, campaign role lists were found.");
				throw new ValidationException("Multiple document, campaign role lists were found.");
			}
			
			tempClassRoleList = ClassDocumentValidators.validateClassIdAndDocumentRoleList(this, httpRequest.getParameter(InputKeys.DOCUMENT_CLASS_ROLE_LIST));
			if((tempClassRoleList != null) && (httpRequest.getParameterValues(InputKeys.DOCUMENT_CLASS_ROLE_LIST).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "Multiple document, class role lists were found.");
				throw new ValidationException("Multiple document, class role lists were found.");
			}
			
			if(((tempCampaignRoleList == null) || (tempCampaignRoleList.size() == 0)) &&
			   ((tempClassRoleList == null) || (tempClassRoleList.size() == 0))) {
				setFailed(ErrorCodes.DOCUMENT_MISSING_CAMPAIGN_AND_CLASS_ROLE_LISTS, "You must provide an initial campaign-role and/or class-role list.");
				throw new ValidationException("You must provide an initial campaign-role and/or class-role list.");
			}
			
			tempDescription = DocumentValidators.validateDescription(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
			if((tempDescription != null) && (httpRequest.getParameterValues(InputKeys.DESCRIPTION).length > 1)) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_DESCRIPTION, "Multiple document description parameters were found.");
				throw new ValidationException("Multiple document description parameters were found.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		document = tempDocument;
		name = tempName;
		description = tempDescription;
		privacyState = tempPrivacyState;
		campaignRoleMap = tempCampaignRoleList;
		classRoleMap = tempClassRoleList;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a document creation request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if(campaignRoleMap != null) {
				List<String> campaignIds = new ArrayList<String>(campaignRoleMap.keySet());
				
				LOGGER.info("Verifying that the campaigns in the campaign-role list exist.");
				CampaignServices.checkCampaignsExistence(this, campaignIds, true);
				
				LOGGER.info("Verifying that the user can associate documents with the campaigns in the campaign-role list.");
				UserCampaignDocumentServices.userCanAssociateDocumentsWithCampaigns(this, getUser().getUsername(), campaignIds);
			}
			
			if(classRoleMap != null) {
				List<String> classIds = new ArrayList<String>(classRoleMap.keySet());
				
				LOGGER.info("Verifying that the classes in the class-role list exist.");
				ClassServices.checkClassesExistence(this, classIds, true);
				
				LOGGER.info("Verifying that the user can associate documents with the classes in the class-role list.");
				UserClassDocumentServices.userCanAssociateDocumentsWithClasses(this, getUser().getUsername(), classIds);
			}
			
			LOGGER.info("Creating the document.");
			documentId = DocumentServices.createDocument(this, document, name, description, privacyState, campaignRoleMap, classRoleMap, getUser().getUsername());
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with a success or fail JSON message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		respond(httpRequest, httpResponse, KEY_DOCUMENT_ID, documentId);
	}
}