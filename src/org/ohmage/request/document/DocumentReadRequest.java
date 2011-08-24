package org.ohmage.request.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserCampaignDocumentServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassDocumentServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserDocumentServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.DocumentValidators;

/**
 * <p>Creates a new class. The requester must be an admin.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_PERSONAL_DOCUMENTS}</td>
 *     <td>Either "true" or "false" representing whether or not documents that
 *       are specific to the requesting user should be returned.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN_LIST}</td>
 *     <td>A list of campaign IDs where each ID is separated by a
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}. The 
 *       requesting user must belong to all of the campaigns in some capacity,
 *       and all documents visible to the requesting user in those campaigns
 *       will be returned.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of class IDs where each ID is separated by a 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}. The
 *       requesting user must belong to all of the classes in some capacity, 
 *       and all documents visible to the requesting user in those classes will
 *       be returned.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class DocumentReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentReadRequest.class);
	
	private final Boolean personalDocuments;
	private final List<String> campaignIds;
	private final List<String> classIds;
	
	private List<DocumentInformation> result;
	
	/**
	 * Creates a new document read request from the information in the 
	 * HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public DocumentReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a document read request.");
		
		Boolean tempPersonalDocuments = null;
		List<String> tempCampaignIds = null;
		List<String> tempClassIds = null;
		
		try {
			tempPersonalDocuments = DocumentValidators.validatePersonalDocuments(this, httpRequest.getParameter(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS));
			if(tempPersonalDocuments == null) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, "Missing required key: " + InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
				throw new ValidationException("Missing required key: " + InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
			}
			else if(httpRequest.getParameterValues(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS).length > 1) {
				setFailed(ErrorCodes.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, "Multiple personal documents parameters were given.");
				throw new ValidationException("Multiple personal documents parameters were given.");
			}
			
			tempCampaignIds = CampaignValidators.validateCampaignIds(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST));
			if((tempCampaignIds != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign ID lists were given.");
				throw new ValidationException("Multiple campaign ID lists were given.");
			}
			
			tempClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tempClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID lists were given.");
				throw new ValidationException("Multiple class ID lists were given.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		personalDocuments = tempPersonalDocuments;
		campaignIds = tempCampaignIds;
		classIds = tempClassIds;
		
		result = new ArrayList<DocumentInformation>(0);
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the document read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if(campaignIds != null) {
				LOGGER.info("Verifying that the campaigns in the campaign list exist and that the user belongs.");
				UserCampaignServices.campaignsExistAndUserBelongs(this, campaignIds, getUser().getUsername());
			}
			
			if(classIds != null) {
				LOGGER.info("Verifying that the classes in the class list exist and that the user belongs.");
				UserClassServices.classesExistAndUserBelongs(this, classIds, getUser().getUsername());
			}
			
			Set<String> documentIds = new HashSet<String>();
			
			if(personalDocuments) {
				LOGGER.info("Gathering information about the documents that are specific to this user.");
				documentIds.addAll(UserDocumentServices.getDocumentsSpecificToUser(this, getUser().getUsername())); 
			}
			
			if(campaignIds != null) {
				LOGGER.info("Gathering information about the documents that are visible to this user in the parameterized campaigns.");
				documentIds.addAll(UserCampaignDocumentServices.getVisibleDocumentsSpecificToCampaigns(this, getUser().getUsername(), campaignIds));
			}
		
			if(classIds != null) {
				LOGGER.info("Gathering information about the documents that are visible to this user in the parameterized classes.");
				documentIds.addAll(UserClassDocumentServices.getVisibleDocumentsSpecificToClasses(this, getUser().getUsername(), classIds));
			}
			
			LOGGER.info("Gathering the specific information about each of the documents.");
			result = UserDocumentServices.getDocumentInformationForDocumentsWithUser(this, getUser().getUsername(), documentIds);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Creates a JSONObject that represents all of the information about all of
	 * the classes. Then it responds with that information.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		JSONObject jsonResult = new JSONObject();
		for(DocumentInformation documentInformation : result) {
			try {
				jsonResult.put(documentInformation.getDocumentId(), documentInformation.toJsonObject());
			}
			catch(JSONException e) {
				LOGGER.error("Error building the result JSONObject.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, jsonResult);
	}
}