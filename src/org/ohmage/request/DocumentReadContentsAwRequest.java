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
 * Request for reading the contents of a document.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsAwRequest extends ResultListAwRequest {
	public static final String KEY_DOCUMENT_FILE = "document_read_docuemnt_key_document_file";
	
	/**
	 * Creates a request to read the contents of a document.
	 * 
	 * @param documentId An identifier for the document that is being read.
	 * 
	 * @throws IllegalArgumentException Thrown if the document ID is null or
	 * 									whitespace only.
	 */
	public DocumentReadContentsAwRequest(String documentId) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document ID must be non-null and not whitespace.");
		}
		
		addToValidate(InputKeys.DOCUMENT_ID, documentId, true);
	}
}
