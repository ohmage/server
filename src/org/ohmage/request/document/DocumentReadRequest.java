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
 * </table>
 * 
 * @author John Jenkins
 */
public class DocumentReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(DocumentReadRequest.class);
	
	private final Boolean personalDocuments;
	private final List<String> campaignIds;
	private final Collection<String> classIds;
	
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
		
		LOGGER.info("Creating a document read request.");
		
		Boolean tempPersonalDocuments = null;
		List<String> tempCampaignIds = null;
		Set<String> tempClassIds = null;
		
		try {
			tempPersonalDocuments = DocumentValidators.validatePersonalDocuments(httpRequest.getParameter(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS));
			if((tempPersonalDocuments != null) && (httpRequest.getParameterValues(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS).length > 1)) {
				setFailed(ErrorCode.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, "Multiple personal documents parameters were given.");
				throw new ValidationException("Multiple personal documents parameters were given.");
			}
			
			tempCampaignIds = CampaignValidators.validateCampaignIds(httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST));
			if((tempCampaignIds != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN_LIST).length > 1)) {
				setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "Multiple campaign ID lists were given.");
				throw new ValidationException("Multiple campaign ID lists were given.");
			}
			
			tempClassIds = ClassValidators.validateClassIdList(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tempClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
				setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists were given.");
				throw new ValidationException("Multiple class ID lists were given.");
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			LOGGER.info(e.toString());
		}
		
		personalDocuments = tempPersonalDocuments;
		campaignIds = tempCampaignIds;
		classIds = tempClassIds;
		
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
							classIds);
			
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
