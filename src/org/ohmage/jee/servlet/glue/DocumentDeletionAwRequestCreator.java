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
package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DocumentDeletionAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Creates the document deletion request.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(DocumentDeletionAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentDeletionAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates and returns the request object.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating the document deletion request.");
		
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		String documentId = httpRequest.getParameter(InputKeys.DOCUMENT_ID);
		
		DocumentDeletionAwRequest documentDeletionRequest;
		try {
			documentDeletionRequest = new DocumentDeletionAwRequest(documentId);
			documentDeletionRequest.setUserToken(token);
		}
		catch(IllegalArgumentException e) {
			_logger.info("The document ID is invalid.");
			throw e;
		}
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return documentDeletionRequest;
	}
}
