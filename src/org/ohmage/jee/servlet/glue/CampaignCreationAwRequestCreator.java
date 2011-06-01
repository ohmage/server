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
import org.ohmage.request.CampaignCreationAwRequest;
import org.ohmage.request.InputKeys;


/**
 * Creates a new CampaignCreationAwRequest object for handling the rest of the
 * request.
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(CampaignCreationAwRequestCreator.class);

	/**
	 * Default constructor.
	 */
	public CampaignCreationAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * Gets the CampaignCreationAwRequest object out of the request where it
	 * was stashed during HTTP validation to prevent us from parsing the XML
	 * twice.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new request object for creating a new campaign.");

		CampaignCreationAwRequest awRequest;
		try {
			awRequest = (CampaignCreationAwRequest) request.getAttribute("awRequest");
		}
		catch(ClassCastException e) {
			throw new IllegalStateException("Invalid awRequest object in HTTPServlet. Must be CampaignCreationAwRequest.");
		}
		if(awRequest == null) {
			throw new IllegalStateException("Missing awRequest in HTTPServlet - Did the HTTPValidator run?");
		}
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return awRequest;
	}
}
