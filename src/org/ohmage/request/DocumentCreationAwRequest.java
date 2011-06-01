/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.request;

import org.andwellness.utils.StringUtils;

/**
 * Creates a request for document creation.
 * 
 * @author John Jenkins
 */
public class DocumentCreationAwRequest extends ResultListAwRequest {
	/**
	 * Builds the document creation request. The following parameters are
	 * required:
	 * 
	 *   urn
	 *   name
	 *   document
	 *   privacyState
	 * 
	 * @param urn The proposed URN for the document.
	 * 
	 * @param name The name of the document.
	 * 
	 * @param document The document itself.
	 * 
	 * @param privacyState The initial privacy state for this document.
	 * 
	 * @param description An optional description for this document.
	 * 
	 * @throws IllegalArgumentException Thrown if there is a problem creating
	 * 									the request due to invalid parameters.
	 */
	public DocumentCreationAwRequest(String name, String document, String privacyState, 
			String description, String campaignUrnRoleList, String classUrnRoleList) throws IllegalArgumentException {
		super();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The document's name cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(document)) {
			throw new IllegalArgumentException("The document cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			throw new IllegalArgumentException("The document's initial privacy state cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignUrnRoleList) && StringUtils.isEmptyOrWhitespaceOnly(classUrnRoleList)) {
			throw new IllegalArgumentException("The request must contain a list of campaigns and their roles, a list of classes and their roles, or both.");
		}
		
		addToValidate(InputKeys.DOCUMENT_NAME, name, true);
		addToValidate(InputKeys.DOCUMENT, document, true);
		addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
		addToValidate(InputKeys.DESCRIPTION, description, true);
		addToValidate(InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST, campaignUrnRoleList, true);
		addToValidate(InputKeys.DOCUMENT_CLASS_ROLE_LIST, classUrnRoleList, true);
	}
}
