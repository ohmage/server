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

import org.ohmage.util.StringUtils;

/**
 * Request class for deleting documents.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionAwRequest extends ResultListAwRequest {
	/**
	 * Validates that the document ID isn't null or whitespace and then adds it
	 * to the appropriate map.
	 * 
	 * @param documentId The ID for the document to be deleted.
	 */
	public DocumentDeletionAwRequest(String documentId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document's ID cannot be null or whitespace only: " + documentId);
		}
		
		addToValidate(InputKeys.DOCUMENT_ID, documentId, true);
	}
}
