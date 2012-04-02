/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Document;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
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
 *     <td>false</td>
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
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_NAME_SEARCH}</td>
 *     <td>A space-separated, double-quote-respecting, search term to limit the
 *       results to only those documents whose name matches this term.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DOCUMENT_DESCRIPTION_SEARCH}</td>
 *     <td>A space-separated, double-quote-respecting, search term to limit the
 *       results to only those documents whose description matches this term.
 *       </td>
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
	private final Collection<String> classIds;
	private final Collection<String> nameTokens;
	private final Collection<String> descriptionTokens;
	
	private List<Document> result;
	
	/**
	 * Creates a new document read request from the information in the 
	 * HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public DocumentReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		Boolean tempPersonalDocuments = null;
		List<String> tempCampaignIds = null;
		Set<String> tempClassIds = null;
		Set<String> tempNameTokens = null;
		Set<String> tempDescriptionTokens = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a document read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, 
						"Multiple personal documents parameters were given: " +
							InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
				}
				else if(t.length == 1) {
					tempPersonalDocuments = 
							DocumentValidators.validatePersonalDocuments(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"Multiple campaign ID lists were given: " +
								InputKeys.CAMPAIGN_URN_LIST);
				}
				else if(t.length == 1) {
					tempCampaignIds = 
							CampaignValidators.validateCampaignIds(t[0]);
				}
				
				t = getParameterValues(InputKeys.CLASS_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Multiple class ID lists were given: " +
								InputKeys.CLASS_URN_LIST);
				}
				else if(t.length == 1) {
					tempClassIds = ClassValidators.validateClassIdList(t[0]);
				}
				
				t = getParameterValues(InputKeys.DOCUMENT_NAME_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.DOCUMENT_INVALID_NAME,
							"Multiple name search strings were given: " +
								InputKeys.DOCUMENT_NAME_SEARCH);
				}
				else if(t.length == 1) {
					tempNameTokens = 
							DocumentValidators.validateNameSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.DOCUMENT_DESCRIPTION_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.DOCUMENT_INVALID_DESCRIPTION,
							"Multiple description search strings were given; " +
								InputKeys.DOCUMENT_DESCRIPTION_SEARCH);
				}
				else if(t.length == 1) {
					tempDescriptionTokens =
							DocumentValidators.validateDescriptionSearch(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		personalDocuments = tempPersonalDocuments;
		campaignIds = tempCampaignIds;
		classIds = tempClassIds;
		nameTokens = tempNameTokens;
		descriptionTokens = tempDescriptionTokens;
		
		result = new ArrayList<Document>(0);
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
			LOGGER.info("Gathering the document information.");
			result = 
					UserDocumentServices.instance().getDocumentInformation(
							getUser().getUsername(), 
							personalDocuments, 
							campaignIds, 
							classIds,
							nameTokens,
							descriptionTokens);
			
			LOGGER.info("Found " + result.size() + " documents.");
		}
		catch(ServiceException e) {
			e.failRequest(this);
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
		for(Document documentInformation : result) {
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
